package io.github.moulberry.notenoughupdates.miscgui;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.SpecialColour;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GuiPriceGraph extends GuiScreen {

	private static final Gson GSON = new GsonBuilder().create();
	private static final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
	private final ResourceLocation TEXTURE;
	private static final int X_SIZE = 364;
	private static final int Y_SIZE = 215;
	private Data dataPoints;
	private float highestValue;
	private long firstTime;
	private long lastTime;
	private Float lowestValue = null;
	private String itemName;
	private final String itemId;
	private int guiLeft;
	private int guiTop;
	private ItemStack itemStack = null;
	private boolean loaded = false;
	/**
	 * 0 = hour
	 * 1 = day
	 * 2 = week
	 * 3 = all
	 * 4 = custom
	 **/
	private int mode = NotEnoughUpdates.INSTANCE.config.ahGraph.defaultMode;
	private long customStart = 0;
	private long customEnd = 0;
	private boolean customSelecting = false;

	public GuiPriceGraph(String itemId) {
		switch (NotEnoughUpdates.INSTANCE.config.ahGraph.graphStyle) {
			case 1:
				TEXTURE = new ResourceLocation("notenoughupdates:price_graph_gui/price_information_gui_dark.png");
				break;
			case 2:
				TEXTURE = new ResourceLocation("notenoughupdates:price_graph_gui/price_information_gui_phqdark.png");
				break;
			case 3:
				TEXTURE = new ResourceLocation("notenoughupdates:price_graph_gui/price_information_gui_fsr.png");
				break;
			default:
				TEXTURE = new ResourceLocation("notenoughupdates:price_graph_gui/price_information_gui.png");
				break;
		}
		this.itemId = itemId;
		if (NotEnoughUpdates.INSTANCE.manager.getItemInformation().containsKey(itemId)) {
			JsonObject itemInfo = NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(itemId);
			itemName = itemInfo.get("displayname").getAsString();
			itemStack = NotEnoughUpdates.INSTANCE.manager.jsonToStack(itemInfo);
		}
		loadData();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		guiLeft = (width - X_SIZE) / 2;
		guiTop = (height - Y_SIZE) / 2;

		Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
		GlStateManager.color(1, 1, 1, 1);
		Utils.drawTexturedRect(guiLeft, guiTop, X_SIZE, Y_SIZE,
			0, X_SIZE / 512f, 0, Y_SIZE / 512f, GL11.GL_NEAREST
		);
		Utils.drawTexturedRect(guiLeft + 245, guiTop + 17, 16, 16,
			0, 16 / 512f, (mode == 0 ? 215 : 231) / 512f, (mode == 0 ? 231 : 247) / 512f, GL11.GL_NEAREST
		);
		Utils.drawTexturedRect(guiLeft + 263, guiTop + 17, 16, 16,
			16 / 512f, 32 / 512f, (mode == 1 ? 215 : 231) / 512f, (mode == 1 ? 231 : 247) / 512f, GL11.GL_NEAREST
		);
		Utils.drawTexturedRect(guiLeft + 281, guiTop + 17, 16, 16,
			32 / 512f, 48 / 512f, (mode == 2 ? 215 : 231) / 512f, (mode == 2 ? 231 : 247) / 512f, GL11.GL_NEAREST
		);
		Utils.drawTexturedRect(guiLeft + 299, guiTop + 17, 16, 16,
			48 / 512f, 64 / 512f, (mode == 3 ? 215 : 231) / 512f, (mode == 3 ? 231 : 247) / 512f, GL11.GL_NEAREST
		);

		if (itemName != null && itemStack != null) {
			Utils.drawItemStack(itemStack, guiLeft + 16, guiTop + 11);
			Utils.drawStringScaledMax(itemName, Minecraft.getMinecraft().fontRendererObj, guiLeft + 35, guiTop + 13, false,
				0xffffff, 1.77f, 208
			);
		}

		if (!loaded)
			Utils.drawStringCentered("Loading...", Minecraft.getMinecraft().fontRendererObj,
				guiLeft + 166, guiTop + 116, false, 0xffffff00
			);
		else if (dataPoints == null || dataPoints.get() == null || dataPoints.get().size() <= 1)
			Utils.drawStringCentered("No data found.", Minecraft.getMinecraft().fontRendererObj,
				guiLeft + 166, guiTop + 116, false, 0xffff0000
			);
		else {

			int graphColor = SpecialColour.specialToChromaRGB(NotEnoughUpdates.INSTANCE.config.ahGraph.graphColor);
			int graphColor2 = SpecialColour.specialToChromaRGB(NotEnoughUpdates.INSTANCE.config.ahGraph.graphColor2);
			Integer lowestDist = null;
			Long lowestDistTime = null;
			HashMap<Integer, Integer> secondLineData = new HashMap<>();
			for (int i = (dataPoints.isBz() ? 1 : 0); i >= 0; i--) {
				Utils.drawGradientRect(0, guiLeft + 17, guiTop + 35, guiLeft + 315, guiTop + 198,
					changeAlpha(i == 0 ? graphColor : graphColor2, 120), changeAlpha(i == 0 ? graphColor : graphColor2, 10)
				);
				Integer prevX = null;
				Integer prevY = null;
				for (Long time : dataPoints.get().keySet()) {
					float price = dataPoints.isBz()
						? i == 0 ? dataPoints.bz.get(time).b : dataPoints.bz.get(time).s
						: dataPoints.ah.get(time);
					int xPos = (int) map(time, firstTime, lastTime, guiLeft + 17, guiLeft + 315);
					int yPos = (int) map(price, highestValue + 10d, lowestValue - 10d, guiTop + 35, guiTop + 198);
					if (prevX != null) {
						Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
						GlStateManager.color(1, 1, 1, 1);
						Utils.drawTexturedQuad(
							prevX,
							prevY,
							xPos,
							yPos,
							xPos,
							guiTop + 35,
							prevX,
							guiTop + 35,
							18 / 512f,
							19 / 512f,
							36 / 512f,
							37 / 512f,
							GL11.GL_NEAREST
						);
						if (i == 0) {
							Utils.drawLine(prevX, prevY + 0.5f, xPos, yPos + 0.5f, 2, graphColor);
							if (dataPoints.isBz())
								Utils.drawLine(
									prevX,
									secondLineData.get(prevX) + 0.5f,
									xPos,
									secondLineData.get(xPos) + 0.5f,
									2,
									graphColor2
								);
						}
					}
					if (i == 1)
						secondLineData.put(xPos, yPos);
					if (mouseX >= guiLeft + 17 && mouseX <= guiLeft + 315 && mouseY >= guiTop + 35 && mouseY <= guiTop + 198) {
						int dist = Math.abs(mouseX - xPos);
						if (lowestDist == null || dist < lowestDist) {
							lowestDist = dist;
							lowestDistTime = time;
						}
					}
					prevX = xPos;
					prevY = yPos;
				}
			}
			boolean showDays = lastTime - firstTime > 86400;
			int prevNum = showDays ? Date.from(Instant.ofEpochSecond(firstTime)).getDate() : Date.from(Instant.ofEpochSecond(
				firstTime)).getHours();
			long prevXPos = -100;
			for (long time = firstTime; time <= lastTime; time += showDays ? 3600 : 60) {
				int num = showDays
					? Date.from(Instant.ofEpochSecond(time)).getDate()
					: Date.from(Instant.ofEpochSecond(time)).getHours();
				if (num != prevNum) {
					int xPos = (int) map(time, firstTime, lastTime, guiLeft + 17, guiLeft + 315);
					if (Math.abs(prevXPos - xPos) > 30) {
						Utils.drawStringCentered(String.valueOf(num), Minecraft.getMinecraft().fontRendererObj,
							xPos, guiTop + 206, false, 0x8b8b8b
						);
						prevXPos = xPos;
					}
					prevNum = num;
				}
			}
			for (int i = 0; i <= 6; i++) {
				long price = (long) map(i, 0, 6, highestValue, lowestValue);
				String formattedPrice = formatPrice(price);
				Utils.drawStringF(formattedPrice, Minecraft.getMinecraft().fontRendererObj, guiLeft + 320,
					(float) map(i, 0, 6, guiTop + 35, guiTop + 198)
						- Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT / 2f,
					false, 0x8b8b8b
				);
			}
			if (customSelecting) {
				Utils.drawDottedLine(customStart, guiTop + 36, customStart, guiTop + 197, 2, 10, 0xFFc6c6c6);
				Utils.drawDottedLine(customEnd, guiTop + 36, customEnd, guiTop + 197, 2, 10, 0xFFc6c6c6);
				Utils.drawDottedLine(customStart, guiTop + 36, customEnd, guiTop + 36, 2, 10, 0xFFc6c6c6);
				Utils.drawDottedLine(customStart, guiTop + 197, customEnd, guiTop + 197, 2, 10, 0xFFc6c6c6);
			}
			if (lowestDist != null && !customSelecting) {
				float price = dataPoints.isBz() ? dataPoints.bz.get(lowestDistTime).b : dataPoints.ah.get(lowestDistTime);
				Float price2 = dataPoints.isBz() ? dataPoints.bz.get(lowestDistTime).s : null;
				int xPos = (int) map(lowestDistTime, firstTime, lastTime, guiLeft + 17, guiLeft + 315);
				int yPos = (int) map(price, highestValue + 10d, lowestValue - 10d, guiTop + 35, guiTop + 198);
				int yPos2 = price2 != null
					? (int) map(price2, highestValue + 10d, lowestValue - 10d, guiTop + 35, guiTop + 198)
					: 0;

				Utils.drawLine(xPos, guiTop + 35, xPos, guiTop + 198, 2, 0x4D8b8b8b);
				Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
				GlStateManager.color(1, 1, 1, 1);
				Utils.drawTexturedRect(xPos - 2.5f, yPos - 2.5f, 5, 5,
					0, 5 / 512f, 247 / 512f, 252 / 512f, GL11.GL_NEAREST
				);
				if (price2 != null) {
					Utils.drawTexturedRect(xPos - 2.5f, yPos2 - 2.5f, 5, 5,
						0, 5 / 512f, 247 / 512f, 252 / 512f, GL11.GL_NEAREST
					);
				}

				Date date = Date.from(Instant.ofEpochSecond(lowestDistTime));
				SimpleDateFormat displayFormat = new SimpleDateFormat("'§b'd MMMMM yyyy '§eat§b' HH:mm");
				NumberFormat nf = NumberFormat.getInstance();
				ArrayList<String> text = new ArrayList<>();
				text.add(displayFormat.format(date));
				if (dataPoints.isBz()) {
					text.add(EnumChatFormatting.YELLOW + "" + EnumChatFormatting.BOLD + "Bazaar Insta-Buy: " +
						EnumChatFormatting.GOLD + EnumChatFormatting.BOLD + nf.format(price));
					text.add(EnumChatFormatting.YELLOW + "" + EnumChatFormatting.BOLD + "Bazaar Insta-Sell: " +
						EnumChatFormatting.GOLD + EnumChatFormatting.BOLD + nf.format(price2));
				} else {
					text.add(EnumChatFormatting.YELLOW + "" + EnumChatFormatting.BOLD + "Lowest BIN: " +
						EnumChatFormatting.GOLD + EnumChatFormatting.BOLD + nf.format(price));
				}
				drawHoveringText(text, xPos, yPos);
			}
		}

		if (mouseY >= guiTop + 17 && mouseY <= guiTop + 35 && mouseX >= guiLeft + 244 && mouseX <= guiLeft + 316) {
			int index = (mouseX - guiLeft - 245) / 18;
			switch (index) {
				case 0:
					Gui.drawRect(guiLeft + 245, guiTop + 17, guiLeft + 261, guiTop + 33, 0x80ffffff);
					drawHoveringText(Collections.singletonList("Show 1 Hour"), mouseX, mouseY);
					break;
				case 1:
					Gui.drawRect(guiLeft + 263, guiTop + 17, guiLeft + 279, guiTop + 33, 0x80ffffff);
					drawHoveringText(Collections.singletonList("Show 1 Day"), mouseX, mouseY);
					break;
				case 2:
					Gui.drawRect(guiLeft + 281, guiTop + 17, guiLeft + 297, guiTop + 33, 0x80ffffff);
					drawHoveringText(Collections.singletonList("Show 1 Week"), mouseX, mouseY);
					break;
				case 3:
					Gui.drawRect(guiLeft + 299, guiTop + 17, guiLeft + 315, guiTop + 33, 0x80ffffff);
					drawHoveringText(Collections.singletonList("Show All"), mouseX, mouseY);
					break;
			}
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (mouseY >= guiTop + 17 && mouseY <= guiTop + 35 && mouseX >= guiLeft + 244 && mouseX <= guiLeft + 316) {
			mode = (mouseX - guiLeft - 245) / 18;
			loadData();
		} else if (mouseY >= guiTop + 35 && mouseY <= guiTop + 198 && mouseX >= guiLeft + 17 && mouseX <= guiLeft + 315) {
			customSelecting = true;
			customStart = mouseX;
			customEnd = mouseX;
		}
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		if (customSelecting) {
			customSelecting = false;
			customStart = (int) map(customStart, guiLeft + 17, guiLeft + 315, firstTime, lastTime);
			customEnd = (int) map(mouseX, guiLeft + 17, guiLeft + 315, firstTime, lastTime);
			if (customStart > customEnd) {
				long temp = customStart;
				customStart = customEnd;
				customEnd = temp;
			}
			if (customEnd - customStart != 0) {
				mode = 4;
				loadData();
			}
		}
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		if (customSelecting) {
			customEnd = mouseX < guiLeft + 18 ? guiLeft + 18 : Math.min(mouseX, guiLeft + 314);
		}
	}

	private void loadData() {
		dataPoints = null;
		loaded = false;
		new Thread(() -> {
			File dir = new File("config/notenoughupdates/prices");
			if (!dir.exists()) {
				loaded = true;
				return;
			}
			File[] files = dir.listFiles();
			Data data = new Data();
			if (files == null) return;
			for (File file : files) {
				if (!file.getName().endsWith(".gz")) continue;
				HashMap<String, Data> data2 = load(file);
				if (data2 == null || !data2.containsKey(itemId)) continue;
				if (data2.get(itemId).isBz()) {
					if (data.bz == null) data.bz = data2.get(itemId).bz;
					else data.bz.putAll(data2.get(itemId).bz);
				} else if (data.ah == null) data.ah = data2.get(itemId).ah;
				else data.ah.putAll(data2.get(itemId).ah);
			}
			if (data.get() != null && !data.get().isEmpty()) {
				if (mode < 3)
					data = new Data(
						new TreeMap<>(data.get().entrySet().stream()
															.filter(e -> e.getKey() > System.currentTimeMillis() / 1000 -
																(mode == 0 ? 3600 : mode == 1 ? 86400 : 604800))
															.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))),
						data.isBz()
					);
				else if (mode == 4)
					data = new Data(
						new TreeMap<>(data.get().entrySet().stream()
															.filter(e -> e.getKey() >= customStart && e.getKey() <= customEnd)
															.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))),
						data.isBz()
					);
				if (data.get() == null || data.get().isEmpty()) {
					loaded = true;
					return;
				}
				dataPoints = trimData(data);
				firstTime = dataPoints.get().firstKey();
				lastTime = dataPoints.get().lastKey();
				highestValue = 0;
				lowestValue = null;
				for (long key : dataPoints.get().keySet()) {
					float value1 = dataPoints.isBz() ? dataPoints.bz.get(key).b : dataPoints.ah.get(key);
					Float value2 = dataPoints.isBz() ? dataPoints.bz.get(key).s : null;
					if (value1 > highestValue) {
						highestValue = value1;
					}
					if (value2 != null && value2 > highestValue) {
						highestValue = value2;
					}
					if (lowestValue == null || value1 < lowestValue) {
						lowestValue = value1;
					}
					if (value2 != null && value2 < lowestValue) {
						lowestValue = value2;
					}
				}
			}
			loaded = true;
		}).start();
	}

	public static void addToCache(JsonObject items, boolean bazaar) {
		if (!NotEnoughUpdates.INSTANCE.config.ahGraph.graphEnabled) return;
		try {
			File dir = new File("config/notenoughupdates/prices");
			if (!dir.exists() && !dir.mkdir()) return;
			File[] files = dir.listFiles();
			if (files != null)
				for (File file : files) {
					if (!file.getName().endsWith(".gz")) continue;
					if (file.lastModified() <
						System.currentTimeMillis() - NotEnoughUpdates.INSTANCE.config.ahGraph.dataRetention * 86400000L)
						file.delete();
				}
			Date date = new Date();
			Long epochSecond = date.toInstant().getEpochSecond();
			File file = new File(dir, "prices_" + format.format(date) + ".gz");
			HashMap<String, Data> prices = new HashMap<>();
			if (file.exists())
				prices = load(file);
			if (prices == null) {
				file.delete();
				return;
			}
			for (Map.Entry<String, JsonElement> item : items.entrySet()) {
				if (prices.containsKey(item.getKey())) {
					if (bazaar && item.getValue().getAsJsonObject().has("curr_buy") && item.getValue().getAsJsonObject().has(
						"curr_sell"))
						prices.get(item.getKey()).bz.put(epochSecond, new BzData(
							item.getValue().getAsJsonObject().get("curr_buy").getAsFloat(),
							item.getValue().getAsJsonObject().get("curr_sell").getAsFloat()
						));
					else if (!bazaar)
						prices.get(item.getKey()).ah.put(epochSecond, item.getValue().getAsBigDecimal().intValue());
				} else {
					TreeMap<Long, Object> mapData = new TreeMap<>();
					if (bazaar && item.getValue().getAsJsonObject().has("curr_buy") && item.getValue().getAsJsonObject().has(
						"curr_sell"))
						mapData.put(epochSecond, new BzData(
							item.getValue().getAsJsonObject().get("curr_buy").getAsFloat(),
							item.getValue().getAsJsonObject().get("curr_sell").getAsFloat()
						));
					else if (!bazaar)
						mapData.put(epochSecond, item.getValue().getAsLong());
					prices.put(item.getKey(), new Data(mapData, bazaar));
				}
			}
			//noinspection ResultOfMethodCallIgnored
			file.createNewFile();
			try (
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new GZIPOutputStream(new FileOutputStream(file)),
					StandardCharsets.UTF_8
				))
			) {
				writer.write(GSON.toJson(prices));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Data trimData(Data data) {
		long first = data.get().firstKey();
		long last = data.get().lastKey();
		Data trimmed = new Data();
		if (data.isBz())
			trimmed.bz = new TreeMap<>();
		else
			trimmed.ah = new TreeMap<>();
		int zones = NotEnoughUpdates.INSTANCE.config.ahGraph.graphZones;
		Long[] dataArray = !data.isBz() ? data.ah.keySet().toArray(new Long[0]) : data.bz.keySet().toArray(new Long[0]);
		int prev = 0;
		for (int i = 0; i < zones; i++) {
			long lowest = (long) map(i, 0, zones, first, last);
			long highest = (long) map(i + 1, 0, zones, first, last);
			int amount = 0;
			double sumBuy = 0;
			double sumSell = 0;
			for (int l = prev; l < dataArray.length; l++) {
				if (dataArray[l] >= lowest && dataArray[l] <= highest) {
					amount++;
					sumBuy += data.isBz() ? data.bz.get(dataArray[l]).b : data.ah.get(dataArray[l]);
					if (data.isBz()) sumSell += data.bz.get(dataArray[l]).s;
					prev = l + 1;
				} else if (dataArray[l] > highest)
					break;
			}
			if (amount > 0) {
				if (data.isBz())
					trimmed.bz.put(lowest, new BzData((float) (sumBuy / amount), (float) (sumSell / amount)));
				else
					trimmed.ah.put(lowest, (int) (sumBuy / amount));
			}
		}
		return trimmed;
	}

	private static HashMap<String, Data> load(File file) {
		Type type = new TypeToken<HashMap<String, Data>>() {
		}.getType();
		if (file.exists()) {
			try (
				BufferedReader reader = new BufferedReader(new InputStreamReader(
					new GZIPInputStream(new FileInputStream(file)),
					StandardCharsets.UTF_8
				))
			) {
				return GSON.fromJson(reader, type);
			} catch (Exception ignored) {
			}
		}
		return null;
	}

	private static double map(double x, double in_min, double in_max, double out_min, double out_max) {
		return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}

	private static String formatPrice(long price) {
		DecimalFormat df = new DecimalFormat("#.00");
		if (price >= 1000000000) {
			return df.format(price / 1000000000f) + "B";
		} else if (price >= 1000000) {
			return df.format(price / 1000000f) + "M";
		} else if (price >= 1000) {
			return df.format(price / 1000f) + "K";
		}
		return String.valueOf(price);
	}

	private int changeAlpha(int origColor, int alpha) {
		origColor = origColor & 0x00ffffff; //drop the previous alpha value
		return (alpha << 24) | origColor; //add the one the user inputted
	}
}

class Data {
	public TreeMap<Long, Integer> ah = null;
	public TreeMap<Long, BzData> bz = null;

	public Data() {
	}

	public Data(TreeMap<Long, ?> map, boolean bz) {
		if (bz)
			this.bz = (TreeMap<Long, BzData>) map;
		else
			this.ah = (TreeMap<Long, Integer>) map;
	}

	public TreeMap<Long, ?> get() {
		return !isBz() ? ah : bz;
	}

	public boolean isBz() {
		return bz != null && !bz.isEmpty();
	}
}

class BzData {
	float b;
	float s;

	public BzData(float b, float s) {
		this.b = b;
		this.s = s;
	}
}
