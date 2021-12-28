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
    private TreeMap<Long, Long> dataPoints;
    private long highestValue;
    private long firstTime;
    private long lastTime;
    private Long lowestValue = null;
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
    private int mode = 1;
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
                0, X_SIZE / 512f, 0, Y_SIZE / 512f, GL11.GL_NEAREST);
        Utils.drawTexturedRect(guiLeft + 245, guiTop + 17, 16, 16,
                0, 16 / 512f, (mode == 0 ? 215 : 231) / 512f, (mode == 0 ? 231 : 247) / 512f, GL11.GL_NEAREST);
        Utils.drawTexturedRect(guiLeft + 263, guiTop + 17, 16, 16,
                16 / 512f, 32 / 512f, (mode == 1 ? 215 : 231) / 512f, (mode == 1 ? 231 : 247) / 512f, GL11.GL_NEAREST);
        Utils.drawTexturedRect(guiLeft + 281, guiTop + 17, 16, 16,
                32 / 512f, 48 / 512f, (mode == 2 ? 215 : 231) / 512f, (mode == 2 ? 231 : 247) / 512f, GL11.GL_NEAREST);
        Utils.drawTexturedRect(guiLeft + 299, guiTop + 17, 16, 16,
                48 / 512f, 64 / 512f, (mode == 3 ? 215 : 231) / 512f, (mode == 3 ? 231 : 247) / 512f, GL11.GL_NEAREST);

        if (itemName != null && itemStack != null) {
            Utils.drawItemStack(itemStack, guiLeft + 16, guiTop + 11);
            Utils.drawStringScaledMax(itemName, Minecraft.getMinecraft().fontRendererObj, guiLeft + 35, guiTop + 13, false,
                    0xffffff, 1.77f, 208);
        }

        if (!loaded)
            Utils.drawStringCentered("Loading...", Minecraft.getMinecraft().fontRendererObj,
                    guiLeft + 166, guiTop + 116, false, 0xffffff00);
        else if (dataPoints == null || dataPoints.size() <= 1)
            Utils.drawStringCentered("No data found.", Minecraft.getMinecraft().fontRendererObj,
                    guiLeft + 166, guiTop + 116, false, 0xffff0000);
        else {

            int graphColor = SpecialColour.specialToChromaRGB(NotEnoughUpdates.INSTANCE.config.ahGraph.graphColor);
            Utils.drawGradientRect(0, guiLeft + 17, guiTop + 35, guiLeft + 315, guiTop + 198,
                    changeAlpha(graphColor, 120), changeAlpha(graphColor, 10));
            Integer prevX = null;
            Integer prevY = null;
            Integer lowestDist = null;
            Long lowestDistTime = null;
            for (Long time : dataPoints.keySet()) {
                Long price = dataPoints.get(time);
                int xPos = (int) map(time, firstTime, lastTime, guiLeft + 17, guiLeft + 315);
                int yPos = (int) map(price, highestValue + 10, lowestValue - 10, guiTop + 35, guiTop + 198);
                if (prevX != null) {
                    Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
                    GlStateManager.color(1, 1, 1, 1);
                    Utils.drawTexturedQuad(prevX, prevY, xPos, yPos, xPos, guiTop + 35, prevX, guiTop + 35, 18 / 512f, 19 / 512f,
                            36 / 512f, 37 / 512f, GL11.GL_NEAREST);
                    Utils.drawLine(prevX, prevY + 0.5f, xPos, yPos + 0.5f, 2, graphColor);
                }
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
            boolean showDays = lastTime - firstTime > 86400;
            int prevNum = showDays ? Date.from(Instant.ofEpochSecond(firstTime)).getDate() : Date.from(Instant.ofEpochSecond(firstTime)).getHours();
            long prevXPos = -100;
            for (long time = firstTime; time <= lastTime; time += showDays ? 3600 : 60) {
                int num = showDays ? Date.from(Instant.ofEpochSecond(time)).getDate() : Date.from(Instant.ofEpochSecond(time)).getHours();
                if (num != prevNum) {
                    int xPos = (int) map(time, firstTime, lastTime, guiLeft + 17, guiLeft + 315);
                    if (Math.abs(prevXPos - xPos) > 30) {
                        Utils.drawStringCentered(String.valueOf(num), Minecraft.getMinecraft().fontRendererObj,
                                xPos, guiTop + 206, false, 0x8b8b8b);
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
                        false, 0x8b8b8b);
            }
            if (customSelecting) {
                Utils.drawDottedLine(customStart, guiTop + 36, customStart, guiTop + 197, 2, 10, 0xFFc6c6c6);
                Utils.drawDottedLine(customEnd, guiTop + 36, customEnd, guiTop + 197, 2, 10, 0xFFc6c6c6);
                Utils.drawDottedLine(customStart, guiTop + 36, customEnd, guiTop + 36, 2, 10, 0xFFc6c6c6);
                Utils.drawDottedLine(customStart, guiTop + 197, customEnd, guiTop + 197, 2, 10, 0xFFc6c6c6);
            }
            if (lowestDist != null && !customSelecting) {
                Long price = dataPoints.get(lowestDistTime);
                int xPos = (int) map(lowestDistTime, firstTime, lastTime, guiLeft + 17, guiLeft + 315);
                int yPos = (int) map(price, highestValue + 10, lowestValue - 10, guiTop + 35, guiTop + 198);

                Utils.drawLine(xPos, guiTop + 35, xPos, guiTop + 198, 2, 0x4D8b8b8b);
                Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
                GlStateManager.color(1, 1, 1, 1);
                Utils.drawTexturedRect(xPos - 2.5f, yPos - 2.5f, 5, 5,
                        0, 5 / 512f, 247 / 512f, 252 / 512f, GL11.GL_NEAREST);

                Date date = Date.from(Instant.ofEpochSecond(lowestDistTime));
                SimpleDateFormat displayFormat = new SimpleDateFormat("'§b'd MMMMM yyyy '§eat§b' HH:mm");
                NumberFormat nf = NumberFormat.getInstance();
                drawHoveringText(new ArrayList<String>() {{
                    add(displayFormat.format(date));
                    add(EnumChatFormatting.YELLOW + "" + EnumChatFormatting.BOLD + "Lowest BIN: " +
                            EnumChatFormatting.GOLD + EnumChatFormatting.BOLD + nf.format(price));
                }}, xPos, yPos);
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
            HashMap<Long, Long> data = new HashMap<>();
            assert files != null;
            for (File file : files) {
                if (!file.getName().endsWith(".gz")) continue;
                HashMap<String, HashMap<Long, Long>> data2 = load(file);
                if (data2 == null || !data2.containsKey(itemId)) continue;
                data.putAll(data2.get(itemId));
            }
            if (!data.isEmpty()) {
                if (mode < 3)
                    data = new HashMap<>(data.entrySet().stream()
                            .filter(e -> e.getKey() > System.currentTimeMillis() / 1000 - (mode == 0 ? 3600 : mode == 1 ? 86400 : 604800))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                else if (mode == 4)
                    data = new HashMap<>(data.entrySet().stream()
                            .filter(e -> e.getKey() >= customStart && e.getKey() <= customEnd)
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                if (data.isEmpty()) {
                    loaded = true;
                    return;
                }
                dataPoints = trimData(new TreeMap<>(data));
                firstTime = dataPoints.firstKey();
                lastTime = dataPoints.lastKey();
                highestValue = 0;
                lowestValue = null;
                for (long value : dataPoints.values()) {
                    if (value > highestValue) {
                        highestValue = value;
                    }
                    if (lowestValue == null || value < lowestValue) {
                        lowestValue = value;
                    }
                }
            }
            loaded = true;
        }).start();
    }

    public static void addToCache(JsonObject items) {
        if (!NotEnoughUpdates.INSTANCE.config.ahGraph.graphEnabled) return;
        try {
            File dir = new File("config/notenoughupdates/prices");
            if (!dir.exists() && !dir.mkdir()) return;
            File[] files = dir.listFiles();
            if (files != null)
                for (File file : files) {
                    if (!file.getName().endsWith(".gz")) continue;
                    if (file.lastModified() < System.currentTimeMillis() - NotEnoughUpdates.INSTANCE.config.ahGraph.dataRetention * 86400000L)
                        file.delete();
                }
            Date date = new Date();
            Long epochSecond = date.toInstant().getEpochSecond();
            File file = new File(dir, "prices_" + format.format(date) + ".gz");
            HashMap<String, HashMap<Long, Long>> prices = new HashMap<>();
            if (file.exists())
                prices = load(file);
            if (prices == null) return;
            for (Map.Entry<String, JsonElement> item : items.entrySet()) {
                if (prices.containsKey(item.getKey())) {
                    prices.get(item.getKey()).put(epochSecond, item.getValue().getAsLong());
                } else {
                    HashMap<Long, Long> mapData = new HashMap<>();
                    mapData.put(epochSecond, item.getValue().getAsLong());
                    prices.put(item.getKey(), mapData);
                }
            }
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file)), StandardCharsets.UTF_8))) {
                writer.write(GSON.toJson(prices));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TreeMap<Long, Long> trimData(TreeMap<Long, Long> data) {
        long first = data.firstKey();
        long last = data.lastKey();
        TreeMap<Long, Long> trimmed = new TreeMap<>();
        int zones = NotEnoughUpdates.INSTANCE.config.ahGraph.graphZones;
        Long[] dataArray = data.keySet().toArray(new Long[0]);
        int prev = 0;
        for (int i = 0; i < zones; i++) {
            long lowest = (long) map(i, 0, zones, first, last);
            long highest = (long) map(i + 1, 0, zones, first, last);
            int amount = 0;
            long sum = 0;
            for (int l = prev; l < dataArray.length; l++) {
                if (dataArray[l] >= lowest && dataArray[l] <= highest) {
                    amount++;
                    sum += data.get(dataArray[l]);
                    prev = l + 1;
                } else if (dataArray[l] > highest)
                    break;
            }
            if (amount > 0)
                trimmed.put((lowest + highest) / 2, sum / amount);
        }
        return trimmed;
    }


    private static HashMap<String, HashMap<Long, Long>> load(File file) {
        Type type = new TypeToken<HashMap<String, HashMap<Long, Long>>>() {
        }.getType();
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)), StandardCharsets.UTF_8))) {
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
