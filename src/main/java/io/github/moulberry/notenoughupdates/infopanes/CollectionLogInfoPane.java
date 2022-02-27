package io.github.moulberry.notenoughupdates.infopanes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.NEUOverlay;
import io.github.moulberry.notenoughupdates.util.NEUResourceManager;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.moulberry.notenoughupdates.util.GuiTextures.item_mask;

public class CollectionLogInfoPane extends ScrollableInfoPane {
	private final String mobRegex = ".*?((_MONSTER)|(_ANIMAL)|(_MINIBOSS)|(_BOSS)|(_SC))$";
	private final String petRegex = ".*?;[0-4]$";

	TreeSet<String> items = new TreeSet<>(getItemComparator());

	private int buttonHover = -1;

	private int previousAcquiredCount = 0;
	private int previousScroll = 0;
	private int previousX = 0;
	private int previousFilter = 0;

	private final long lastUpdate = 0;

	private static final int FILTER_ALL = 0;
	private static final int FILTER_WEAPON = 1;
	private static final int FILTER_ARMOR = 2;
	private static final int FILTER_ACCESSORY = 3;
	private static final int FILTER_PET = 4;
	private static final int FILTER_DUNGEON = 5;
	private static final int FILTER_SLAYER_ZOMBIE = 6;
	private static final int FILTER_SLAYER_WOLF = 7;
	private static final int FILTER_SLAYER_SPIDER = 8;
	private int filterMode = FILTER_ALL;
	private final String[] filterPrettyNames = new String[]{
		"ALL", "WEAPON", "ARMOR",
		"ACCESSORY", "PET", "DUNGEON", "ZOMBIE SLAYER", "WOLF SLAYER", "SPIDER SLAYER"
	};

	private Framebuffer itemFramebuffer = null;
	private Framebuffer itemBGFramebuffer = null;
	private Framebuffer itemFramebufferGrayscale = null;
	private Shader grayscaleShader = null;

	private final int updateCounter = 0;

	public CollectionLogInfoPane(NEUOverlay overlay, NEUManager manager) {
		super(overlay, manager);
		refreshItems();
	}

	private boolean loreContains(JsonArray lore, String str) {
		for (int i = 0; i < lore.size(); i++) {
			String line = lore.get(i).getAsString();
			if (line.contains(str)) return true;
		}
		return false;
	}

	private void refreshItems() {
		items.clear();
		for (String internalname : manager.getItemInformation().keySet()) {
			if (!manager.auctionManager.isVanillaItem(internalname) && !internalname.matches(mobRegex)) {
				JsonObject item = manager.getItemInformation().get(internalname);
				JsonArray lore = manager.getItemInformation().get(internalname).get("lore").getAsJsonArray();
				switch (filterMode) {
					case FILTER_WEAPON:
						if (overlay.checkItemType(lore, "SWORD", "BOW", "WAND") < 0) continue;
						break;
					case FILTER_ARMOR:
						if (overlay.checkItemType(lore, "HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS") < 0) continue;
						break;
					case FILTER_ACCESSORY:
						if (overlay.checkItemType(lore, "ACCESSORY") < 0) continue;
						break;
					case FILTER_PET:
						if (!internalname.matches(petRegex) || !item.get("displayname").getAsString().contains("["))
							continue;
						break;
					case FILTER_DUNGEON:
						if (Utils.checkItemType(lore, true, "DUNGEON") < 0) continue;
						break;
					case FILTER_SLAYER_ZOMBIE:
						if (!item.has("slayer_req") || !item.get("slayer_req").getAsString().startsWith("ZOMBIE"))
							continue;
						break;
					case FILTER_SLAYER_WOLF:
						if (!item.has("slayer_req") || !item.get("slayer_req").getAsString().startsWith("WOLF"))
							continue;
						break;
					case FILTER_SLAYER_SPIDER:
						if (!item.has("slayer_req") || !item.get("slayer_req").getAsString().startsWith("SPIDER"))
							continue;
						break;
				}
				items.add(internalname);
			}
		}
	}

	private Map<String, ArrayList<String>> getAcquiredItems() {
		return null;//manager.config.collectionLog.value;
	}

	private Comparator<String> getItemComparator() {
		return (o1, o2) -> {
			float cost1 = manager.auctionManager.getLowestBin(o1);
			float cost2 = manager.auctionManager.getLowestBin(o2);

			if (cost1 == -1) cost1 = manager.auctionManager.getCraftCost(o1).craftCost;
			if (cost2 == -1) cost2 = manager.auctionManager.getCraftCost(o2).craftCost;

			if (cost1 < cost2) return 1;
			if (cost1 > cost2) return -1;

			return o1.compareTo(o2);
		};
	}

	public void mouseInput(int width, int height, int mouseX, int mouseY, boolean mouseDown) {
		super.mouseInput(width, height, mouseX, mouseY, mouseDown);
		if (mouseDown) {
			if (buttonHover == 0) {
				if (Mouse.getEventButton() == 0) {
					filterMode++;
					if (filterMode >= filterPrettyNames.length) {
						filterMode = 0;
					}
				} else if (Mouse.getEventButton() == 1) {
					filterMode--;
					if (filterMode < 0) {
						filterMode = filterPrettyNames.length - 1;
					}
				}
			}
			refreshItems();
		}
	}

	public void render(
		int width,
		int height,
		Color bg,
		Color fg,
		ScaledResolution scaledresolution,
		int mouseX,
		int mouseY
	) {
		int paneWidth = (int) (width / 3 * overlay.getWidthMult());
		int rightSide = (int) (width * overlay.getInfoPaneOffsetFactor());
		int leftSide = rightSide - paneWidth;
		int padding = overlay.getBoxPadding();

		renderDefaultBackground(width, height, bg);

		renderControls(height, padding, leftSide + padding, rightSide - padding, 20, fg);
		renderCollectionLog(fg, width, height, leftSide + padding, rightSide - padding, padding + 25, height - padding);
	}

	private float getCompletedness() {
		int total = items.size();
		int own = 0;
		for (String item : items) {
			if (getAcquiredItems() != null &&
				getAcquiredItems().containsKey(manager.getCurrentProfile()) &&
				getAcquiredItems().get(manager.getCurrentProfile()).contains(item)) {
				own++;
			}

		}
		return own / (float) total;
	}

	private final EnumChatFormatting[] rainbow = new EnumChatFormatting[]{
		EnumChatFormatting.RED,
		EnumChatFormatting.GOLD,
		EnumChatFormatting.YELLOW,
		EnumChatFormatting.GREEN,
		EnumChatFormatting.AQUA,
		EnumChatFormatting.LIGHT_PURPLE,
		EnumChatFormatting.DARK_PURPLE
	};

	private String getCompletednessString() {
		float completedness = getCompletedness();
		String text = (int) (completedness * 100) + "% Complete";
		if (completedness >= 1) {
			StringBuilder rainbowText = new StringBuilder();
			for (int i = 0; i < text.length(); i++) {
				char c = text.charAt(i);
				int index = (int) (i - System.currentTimeMillis() / 100) % rainbow.length;
				if (index < 0) index += rainbow.length;
				rainbowText.append(rainbow[index]).append(c);
			}
			text = rainbowText.toString();
		}
		return text;
	}

	private void renderControls(int height, int top, int left, int right, int ySize, Color fg) {
		ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());

		int mouseX = Mouse.getX() / scaledresolution.getScaleFactor();
		int mouseY = height - Mouse.getY() / scaledresolution.getScaleFactor();

		buttonHover = -1;

		int totalAvailable = right - left;
		int controlPadding = 3;
		String[] controls = new String[]{
			"Filter: " + filterPrettyNames[filterMode],
			getCompletednessString()
		};
		int numControls = controls.length;
		int available = totalAvailable - (numControls - 1) * controlPadding;
		int controlSize = available / numControls;
		int extraPadding = (available % controlSize) / 2;

		for (int i = 0; i < numControls; i++) {
			int width = controlSize + controlPadding;
			int x = left + extraPadding + i * width;

			if (mouseX > x && mouseX < x + controlSize) {
				if (mouseY > top && mouseY < top + ySize) {
					buttonHover = i;
				}
			}

			drawRect(x, top, x + controlSize, top + ySize,
				new Color(177, 177, 177).getRGB()
			);
			drawRect(x + 1, top + 1, x + controlSize, top + ySize,
				new Color(50, 50, 50).getRGB()
			);
			drawRect(x + 1, top + 1, x + controlSize - 1, top + ySize - 1, fg.getRGB());
			Utils.drawStringCenteredScaledMaxWidth(controls[i], Minecraft.getMinecraft().fontRendererObj,
				x + width / 2f, top + ySize / 2f, true, controlSize - 4, Color.WHITE.getRGB()
			);
		}
	}

	public int getCurrentAcquiredCount() {
		if (getAcquiredItems() == null) return 0;
		if (!getAcquiredItems().containsKey(manager.getCurrentProfile())) return 0;
		return getAcquiredItems().get(manager.getCurrentProfile()).size();
	}

	private void renderCollectionLog(Color fg, int width, int height, int left, int right, int top, int bottom) {
		ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());

		int mouseX = Mouse.getX() / scaledresolution.getScaleFactor();
		int mouseY = height - Mouse.getY() / scaledresolution.getScaleFactor();

		if (itemFramebuffer != null && grayscaleShader != null &&
			(itemFramebuffer.framebufferWidth != width || itemFramebuffer.framebufferHeight != height)) {
			grayscaleShader.setProjectionMatrix(Utils.createProjectionMatrix(
				width * scaledresolution.getScaleFactor(), height * scaledresolution.getScaleFactor()));
		}

		itemFramebuffer = checkFramebufferSizes(itemFramebuffer, width, height,
			scaledresolution.getScaleFactor()
		);
		itemBGFramebuffer = checkFramebufferSizes(itemBGFramebuffer, width, height,
			scaledresolution.getScaleFactor()
		);
		itemFramebufferGrayscale = checkFramebufferSizes(itemFramebufferGrayscale, width, height,
			scaledresolution.getScaleFactor()
		);

        /*if(!manager.config.cacheRenderedItempane.value || previousAcquiredCount != getCurrentAcquiredCount() ||
                previousScroll != scrollHeight.getValue() || previousX != left || previousFilter != filterMode ||
                System.currentTimeMillis() - lastUpdate > 5000) {
            lastUpdate = System.currentTimeMillis();
            renderItemsToImage(itemFramebuffer, fg, left+5, right, top+1, bottom);
            renderItemBGToImage(itemBGFramebuffer, fg, left+5, right, top+1, bottom);
        }*/
		previousAcquiredCount = getCurrentAcquiredCount();
		previousScroll = scrollHeight.getValue();
		previousX = left;
		previousFilter = filterMode;

		Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
		renderFromImage(itemBGFramebuffer, width, height, left, right, top, bottom);
		renderFromImage(itemFramebuffer, width, height, left, right, top, bottom);

		if (grayscaleShader == null) {
			try {
				grayscaleShader = new Shader(new NEUResourceManager(Minecraft.getMinecraft().getResourceManager()),
					"grayscale",
					itemFramebuffer, itemFramebufferGrayscale
				);
				grayscaleShader.setProjectionMatrix(Utils.createProjectionMatrix(
					width * scaledresolution.getScaleFactor(), height * scaledresolution.getScaleFactor()));
			} catch (Exception e) {
				return;
			}
		}

		GL11.glPushMatrix();
		grayscaleShader.loadShader(0);
		GlStateManager.enableDepth();
		GL11.glPopMatrix();

		Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);

		itemFramebufferGrayscale.bindFramebufferTexture();

		AtomicReference<ItemStack> tooltipToDisplay = new AtomicReference<>(null);

		AtomicBoolean isTop = new AtomicBoolean(false);
		AtomicInteger lowestY = new AtomicInteger(-1);

		String[] items = getItemList();
		GlStateManager.color(1f, 1f, 1f, 1f);
		iterateItemSlots(new ItemSlotConsumer() {
			@Override
			public void consume(int x, int y, int id) {
				if (id < items.length) {
					String internalname = items[id];
					if (id == 0) isTop.set(true);

					int leftI = x - 1;
					int rightI = x + 17;
					int topI = y - 1;
					int bottomI = y + 17;

					lowestY.set(Math.max(bottomI, lowestY.get()));

					if (mouseX > leftI && mouseX < rightI) {
						if (mouseY > topI && mouseY < bottomI) {
							tooltipToDisplay.set(manager.jsonToStack(manager.getItemInformation().get(internalname), true));
						}
					}

					if (getAcquiredItems() != null &&
						getAcquiredItems().containsKey(manager.getCurrentProfile()) &&
						getAcquiredItems().get(manager.getCurrentProfile()).contains(internalname)) {
						return;
					}

					topI = Math.max(topI, top);
					bottomI = Math.min(bottomI, bottom);

					Utils.drawTexturedRect(leftI, topI, rightI - leftI, bottomI - topI,
						leftI / (float) width, rightI / (float) width,
						(height - topI) / (float) height, (height - bottomI) / (float) height
					);
				}
			}
		}, left + 5, right, top + 1, bottom);

		if (!isTop.get()) {
			if (lowestY.get() == -1) {
				scrollHeight.setValue(0);
			} else {
				int dist = bottom - lowestY.get() - 10;
				if (dist > 0) {
					scrollHeight.setValue(scrollHeight.getValue() - dist);
				}
			}
		}

		itemFramebufferGrayscale.unbindFramebufferTexture();

		ItemStack displayStack = tooltipToDisplay.get();
		if (displayStack != null) {
			List<String> text = displayStack.getTooltip(Minecraft.getMinecraft().thePlayer, true);
			Utils.drawHoveringText(text, mouseX, mouseY, width, height, -1, Minecraft.getMinecraft().fontRendererObj);
		}
	}

	private String[] getItemList() {
		String[] items_arr = new String[items.size()];
		int i = 0;
		for (String internalname : items) {
			items_arr[i++] = internalname;
		}
		return items_arr;
	}

	private int limCol(int col) {
		return Math.min(255, Math.max(0, col));
	}

	private void renderItems(int left, int right, int top, int bottom) {
		String[] items = getItemList();
		iterateItemSlots(new ItemSlotConsumer() {
			public void consume(int x, int y, int id) {
				if (id < items.length) {
					String internalname = items[id];

					ItemStack stack = manager.jsonToStack(manager.getItemInformation().get(internalname));
					Utils.drawItemStack(stack, x, y);
				}
			}
		}, left, right, top, bottom);
	}

	private void renderItemBackgrounds(Color fg, int left, int right, int top, int bottom) {
		Color fgCustomOpacity =
			null;//new Color(SpecialColour.specialToChromaRGB(manager.config.itemBackgroundColour.value), true);
		Color fgGold = null;//new Color(SpecialColour.specialToChromaRGB(manager.config.itemFavouriteColour.value), true);

		String[] items = getItemList();
		iterateItemSlots(new ItemSlotConsumer() {
			public void consume(int x, int y, int id) {
				if (id < items.length) {
					String internalname = items[id];

					Color color = fgCustomOpacity;
					if (getAcquiredItems() != null &&
						getAcquiredItems().containsKey(manager.getCurrentProfile()) &&
						getAcquiredItems().get(manager.getCurrentProfile()).contains(internalname)) {
						color = fgGold;
					}

					Minecraft.getMinecraft().getTextureManager().bindTexture(item_mask);
                    /*if(manager.config.itemStyle.value) {
                        GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f,
                                color.getBlue() / 255f, color.getAlpha() / 255f);
                        Utils.drawTexturedRect(x - 1, y - 1, overlay.ITEM_SIZE + 2, overlay.ITEM_SIZE + 2, GL11.GL_NEAREST);
                    } else {
                        drawRect(x-1, y-1, x+overlay.ITEM_SIZE+1, y+overlay.ITEM_SIZE+1, color.getRGB());
                    }*/
					GlStateManager.bindTexture(0);
				}
			}
		}, left, right, top, bottom);
	}

	/**
	 * Checks whether the screen size has changed, if so it reconstructs the itemPane framebuffer and marks that the
	 * itemPane should be redrawn.
	 */
	private Framebuffer checkFramebufferSizes(Framebuffer framebuffer, int width, int height, int scaleFactor) {
		int sw = width * scaleFactor;
		int sh = height * scaleFactor;

		if (framebuffer == null || framebuffer.framebufferWidth != sw || framebuffer.framebufferHeight != sh) {
			if (framebuffer == null) {
				framebuffer = new Framebuffer(sw, sh, true);
			} else {
				framebuffer.createBindFramebuffer(sw, sh);
			}
			framebuffer.setFramebufferFilter(GL11.GL_NEAREST);
		}
		return framebuffer;
	}

	private void renderItemsToImage(Framebuffer framebuffer, Color fg, int left, int right, int top, int bottom) {
		GL11.glPushMatrix();
		framebuffer.framebufferClear();
		framebuffer.bindFramebuffer(false);

		renderItems(left, right, top, bottom);

		framebuffer.unbindFramebuffer();
		Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
		GL11.glPopMatrix();
	}

	private void renderItemBGToImage(Framebuffer framebuffer, Color fg, int left, int right, int top, int bottom) {
		GL11.glPushMatrix();
		framebuffer.framebufferClear();
		framebuffer.bindFramebuffer(false);

		renderItemBackgrounds(fg, left, right, top, bottom);

		framebuffer.unbindFramebuffer();
		Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
		GL11.glPopMatrix();
	}

	private void renderFromImage(
		Framebuffer framebuffer,
		int width,
		int height,
		int left,
		int right,
		int top,
		int bottom
	) {
		framebuffer.bindFramebufferTexture();
		GlStateManager.color(1f, 1f, 1f, 1f);
		Utils.drawTexturedRect(left, top, right - left, bottom - top,
			left / (float) width, right / (float) width,
			(height - top) / (float) height, (height - bottom) / (float) height
		);
		framebuffer.unbindFramebufferTexture();
	}

	private abstract static class ItemSlotConsumer {
		public abstract void consume(int x, int y, int id);
	}

	public void iterateItemSlots(ItemSlotConsumer itemSlotConsumer, int left, int right, int top, int bottom) {
		int scrolledTop = top - scrollHeight.getValue();

		int id = 0;
		int extraSize = NEUOverlay.ITEM_SIZE + NEUOverlay.ITEM_PADDING;
		for (int y = scrolledTop; y < bottom; y += extraSize) {
			for (int x = left; x < right - extraSize; x += extraSize) {
				if (y > top - extraSize) {
					itemSlotConsumer.consume(x, y, id);
				}
				if (++id >= items.size()) {
					return;
				}
			}
		}
	}

	public boolean keyboardInput() {
		return false;
	}
}
