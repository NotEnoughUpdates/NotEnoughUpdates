/*
 * Copyright (C) 2022-2023 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moulberry.notenoughupdates.miscgui;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.BackgroundBlur;
import io.github.moulberry.notenoughupdates.core.ChromaColour;
import io.github.moulberry.notenoughupdates.core.GlScissorStack;
import io.github.moulberry.notenoughupdates.core.GuiElement;
import io.github.moulberry.notenoughupdates.core.GuiElementTextField;
import io.github.moulberry.notenoughupdates.core.config.KeybindHelper;
import io.github.moulberry.notenoughupdates.core.util.lerp.LerpingInteger;
import io.github.moulberry.notenoughupdates.miscfeatures.BetterContainers;
import io.github.moulberry.notenoughupdates.miscfeatures.SlotLocking;
import io.github.moulberry.notenoughupdates.miscfeatures.StorageManager;
import io.github.moulberry.notenoughupdates.mixins.AccessorGuiContainer;
import io.github.moulberry.notenoughupdates.util.NotificationHandler;
import io.github.moulberry.notenoughupdates.util.SpecialColour;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.vector.Vector2f;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StorageOverlay extends GuiElement {
	public static final ResourceLocation[] STORAGE_PREVIEW_TEXTURES = new ResourceLocation[4];
	private static final ResourceLocation[] STORAGE_TEXTURES = new ResourceLocation[4];
	private static final ResourceLocation STORAGE_ICONS_TEXTURE = new ResourceLocation(
		"notenoughupdates:storage_gui/storage_icons.png");
	private static final ResourceLocation STORAGE_PANE_CTM_TEXTURE = new ResourceLocation(
		"notenoughupdates:storage_gui/storage_gui_pane_ctm.png");
	private static final ResourceLocation[] NOT_RICKROLL_SEQ = new ResourceLocation[19];
	private static final StorageOverlay INSTANCE = new StorageOverlay();
	private static final String CHROMA_STR = "230:255:255:0:0";
	private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
	private static final NEUManager manager = NotEnoughUpdates.INSTANCE.manager;

	static {
		for (int i = 0; i < STORAGE_TEXTURES.length; i++) {
			STORAGE_TEXTURES[i] = new ResourceLocation("notenoughupdates:storage_gui/storage_gui_" + i + ".png");
		}
		for (int i = 0; i < STORAGE_PREVIEW_TEXTURES.length; i++) {
			STORAGE_PREVIEW_TEXTURES[i] = new ResourceLocation("notenoughupdates:storage_gui/storage_preview_" + i + ".png");
		}

		for (int i = 0; i < NOT_RICKROLL_SEQ.length; i++) {
			NOT_RICKROLL_SEQ[i] = new ResourceLocation("notenoughupdates:storage_gui/we_do_a_little_rolling/" + i + ".jpg");
		}
	}

	private final Set<Vector2f> enchantGlintRenderLocations = new HashSet<>();
	private final GuiElementTextField searchBar = new GuiElementTextField("", 88, 10,
		GuiElementTextField.SCALE_TEXT | GuiElementTextField.DISABLE_BG
	);
	private final GuiElementTextField renameStorageField = new GuiElementTextField("", 100, 13,
		GuiElementTextField.COLOUR
	);
	private final int[][] isPaneCaches = new int[40][];
	private final int[][] ctmIndexCaches = new int[40][];
	private final LerpingInteger scroll = new LerpingInteger(0, 200);
	private Framebuffer framebuffer = null;
	private int editingNameId = -1;
	private int guiLeft;
	private int guiTop;
	private boolean fastRender = false;
	private int rollIndex = 0;
	private long millisAccumRoll = 0;
	private long lastMillis = 0;
	private int scrollVelocity = 0;
	private long lastScroll = 0;
	private int desiredHeightSwitch = -1;
	private int desiredHeightMX = -1;
	private int desiredHeightMY = -1;
	private boolean dirty = false;
	private boolean allowTypingInSearchBar = true;
	private int scrollGrabOffset = -1;

	public static StorageOverlay getInstance() {
		return INSTANCE;
	}

	private static boolean shouldConnect(int paneIndex1, int paneIndex2) {
		if (paneIndex1 == 16 || paneIndex2 == 16) return false;
		if (paneIndex1 < 1 || paneIndex2 < 1) return false;
		return paneIndex1 == paneIndex2;

	}

	public static int getCTMIndex(StorageManager.StoragePage page, int index, int[] isPaneCache, int[] ctmIndexCache) {
		if (page.items[index] == null) {
			ctmIndexCache[index] = -1;
			return -1;
		}

		int paneType = getPaneType(page.items[index], index, isPaneCache);

		int upIndex = index - 9;
		int leftIndex = index % 9 > 0 ? index - 1 : -1;
		int rightIndex = index % 9 < 8 ? index + 1 : -1;
		int downIndex = index + 9;
		int upleftIndex = index % 9 > 0 ? index - 10 : -1;
		int uprightIndex = index % 9 < 8 ? index - 8 : -1;
		int downleftIndex = index % 9 > 0 ? index + 8 : -1;
		int downrightIndex = index % 9 < 8 ? index + 10 : -1;

		boolean up = upIndex >= 0 && upIndex < isPaneCache.length && shouldConnect(getPaneType(
			page.items[upIndex],
			upIndex,
			isPaneCache
		), paneType);
		boolean left = leftIndex >= 0 && leftIndex < isPaneCache.length && shouldConnect(getPaneType(
			page.items[leftIndex],
			leftIndex,
			isPaneCache
		), paneType);
		boolean down = downIndex >= 0 && downIndex < isPaneCache.length && shouldConnect(getPaneType(
			page.items[downIndex],
			downIndex,
			isPaneCache
		), paneType);
		boolean right = rightIndex >= 0 && rightIndex < isPaneCache.length && shouldConnect(getPaneType(
			page.items[rightIndex],
			rightIndex,
			isPaneCache
		), paneType);
		boolean upleft = upleftIndex >= 0 && upleftIndex < isPaneCache.length && shouldConnect(getPaneType(
			page.items[upleftIndex],
			upleftIndex,
			isPaneCache
		), paneType);
		boolean upright = uprightIndex >= 0 && uprightIndex < isPaneCache.length && shouldConnect(getPaneType(
			page.items[uprightIndex],
			uprightIndex,
			isPaneCache
		), paneType);
		boolean downleft = downleftIndex >= 0 && downleftIndex < isPaneCache.length && shouldConnect(getPaneType(
			page.items[downleftIndex],
			downleftIndex,
			isPaneCache
		), paneType);
		boolean downright = downrightIndex >= 0 && downrightIndex < isPaneCache.length &&
			shouldConnect(getPaneType(page.items[downrightIndex], downrightIndex, isPaneCache), paneType);

		int ctmIndex = BetterContainers.getCTMIndex(up, right, down, left, upleft, upright, downright, downleft);
		ctmIndexCache[index] = ctmIndex;
		return ctmIndex;
	}

	public static int getRGBFromPane(int paneType) {
		int rgb = -1;
		EnumChatFormatting formatting = EnumChatFormatting.WHITE;
		switch (paneType) {
			case 0:
				formatting = EnumChatFormatting.WHITE;
				break;
			case 1:
				formatting = EnumChatFormatting.GOLD;
				break;
			case 2:
				formatting = EnumChatFormatting.LIGHT_PURPLE;
				break;
			case 3:
				formatting = EnumChatFormatting.BLUE;
				break;
			case 4:
				formatting = EnumChatFormatting.YELLOW;
				break;
			case 5:
				formatting = EnumChatFormatting.GREEN;
				break;
			case 6:
				rgb = 0xfff03c96;
				break;
			case 7:
				formatting = EnumChatFormatting.DARK_GRAY;
				break;
			case 8:
				formatting = EnumChatFormatting.GRAY;
				break;
			case 9:
				formatting = EnumChatFormatting.DARK_AQUA;
				break;
			case 10:
				formatting = EnumChatFormatting.DARK_PURPLE;
				break;
			case 11:
				formatting = EnumChatFormatting.DARK_BLUE;
				break;
			case 12:
				rgb = 0xffA0522D;
				break;
			case 13:
				formatting = EnumChatFormatting.DARK_GREEN;
				break;
			case 14:
				formatting = EnumChatFormatting.DARK_RED;
				break;
			case 15:
				rgb = 0x00000000;
				break;
			case 16:
				rgb = SpecialColour.specialToChromaRGB(CHROMA_STR);
				break;
		}
		if (rgb != -1) return rgb;
		return 0xff000000 | Minecraft.getMinecraft().fontRendererObj.getColorCode(formatting.toString().charAt(1));
	}

	public static int getPaneType(ItemStack stack, int index, int[] cache) {
		if (cache != null && cache[index] != 0) return cache[index];

		if (NotEnoughUpdates.INSTANCE.config.storageGUI.fancyPanes == 2) {
			if (cache != null) cache[index] = -1;
			return -1;
		}

		if (stack != null &&
			(stack.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane) || stack.getItem() == Item.getItemFromBlock(
				Blocks.glass_pane))) {
			String internalName = manager.createItemResolutionQuery().withItemStack(stack).resolveInternalName();
			if (internalName != null) {
				if (internalName.startsWith("STAINED_GLASS_PANE")) {
					if (cache != null) cache[index] = stack.getItemDamage() + 1;
					return stack.getItemDamage() + 1;
				} else if (internalName.startsWith("THIN_GLASS")) {
					if (cache != null) cache[index] = 17;
					return 17;
				}
			}
		}
		if (cache != null) cache[index] = -1;
		return -1;
	}

	private int getMaximumScroll() {
		synchronized (StorageManager.getInstance().storageConfig.displayToStorageIdMapRender) {

			int maxH = 0;

			for (int i = 0; i < 3; i++) {
				int lastDisplayId = StorageManager.getInstance().storageConfig.displayToStorageIdMapRender.size() - 1;
				int coords = (int) Math.ceil(lastDisplayId / 3f) * 3 + 1 + i;

				int h = getPageCoords(coords).y + scroll.getValue() - getStorageViewSize() - 14;

				if (h > maxH) maxH = h;
			}

			return maxH;
		}
	}

	public void markDirty() {
		dirty = true;
	}

	private void scrollToY(int y) {
		int target = y;
		if (target < 0) target = 0;

		int maxY = getMaximumScroll();
		if (target > maxY) target = maxY;

		float factor = (scroll.getValue() - target) / (float) (scroll.getValue() - y + 1E-5);

		scroll.setTarget(target);
		scroll.setTimeToReachTarget(Math.min(200, Math.max(20, (int) (200 * factor))));
		scroll.resetTimer();
	}

	public void scrollToStorage(int displayId, boolean forceScroll) {
		if (displayId < 0) return;

		int y = getPageCoords(displayId).y - 17;
		if (y < 3) {
			scrollToY(y + scroll.getValue());
		} else {
			int storageViewSize = getStorageViewSize();
			int y2 = getPageCoords(displayId + 3).y - 17 - storageViewSize;
			if (y2 > 3) {
				if (forceScroll) {
					scrollToY(y + scroll.getValue());
				} else {
					scrollToY(y2 + scroll.getValue());
				}
			}
		}
	}

	private int getStorageViewSize() {
		return NotEnoughUpdates.INSTANCE.config.storageGUI.storageHeight;
	}

	private int getScrollBarHeight() {
		return getStorageViewSize() - 21;
	}

	@Override
	public void render() {
		if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) return;

		GuiChest guiChest = (GuiChest) Minecraft.getMinecraft().currentScreen;
		ContainerChest containerChest = (ContainerChest) guiChest.inventorySlots;

		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		int width = scaledResolution.getScaledWidth();
		int height = scaledResolution.getScaledHeight();
		int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
		int mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;
		FontRenderer fontRendererObj = Minecraft.getMinecraft().fontRendererObj;

		scroll.tick();

		int displayStyle = NotEnoughUpdates.INSTANCE.config.storageGUI.displayStyle;
		ResourceLocation storageTexture = STORAGE_TEXTURES[displayStyle];
		ResourceLocation storagePreviewTexture = STORAGE_PREVIEW_TEXTURES[displayStyle];
		int textColour = 0x404040;
		int searchTextColour = 0xe0e0e0;
		if (displayStyle == 2) {
			textColour = 0x000000;
			searchTextColour = 0xa0a0a0;
		} else if (displayStyle == 3) {
			textColour = 0xFBCC6C;
		} else if (displayStyle == 0) {
			textColour = 0x909090;
			searchTextColour = 0xa0a0a0;
		}

		if (NotEnoughUpdates.INSTANCE.config.storageGUI.useCustomTextColour) {
			textColour = ChromaColour.specialToChromaRGB(NotEnoughUpdates.INSTANCE.config.storageGUI.customTextColour);
		}

		long currentTime = System.currentTimeMillis();
		if (lastMillis > 0) {
			long deltaTime = currentTime - lastMillis;

			millisAccumRoll += deltaTime;
			rollIndex += millisAccumRoll / 100;
			millisAccumRoll %= 100;
		}

		lastMillis = currentTime;
		rollIndex %= NOT_RICKROLL_SEQ.length * 2;

		ItemStack stackOnMouse = Minecraft.getMinecraft().thePlayer.inventory.getItemStack();
		if (stackOnMouse != null) {
			String stackDisplay = Utils.cleanColour(stackOnMouse.getDisplayName());
			if (stackDisplay.startsWith("Backpack Slot ") || stackDisplay.startsWith("Empty Backpack Slot ") ||
				stackDisplay.startsWith("Ender Chest Page ") || stackDisplay.startsWith("Locked Backpack Slot ")) {
				stackOnMouse = null;
			}
		}

		List<String> tooltipToDisplay = null;
		int slotPreview = -1;

		int storageViewSize = getStorageViewSize();

		int sizeX = 540;
		int sizeY = 100 + storageViewSize;
		int searchNobX = 18;

		int itemHoverX = -1;
		int itemHoverY = -1;

		guiLeft = width / 2 - (sizeX - searchNobX) / 2;
		guiTop = height / 2 - sizeY / 2;

		if (displayStyle == 0) {
			BackgroundBlur.renderBlurredBackground(7, width, height, guiLeft, guiTop, sizeX, storageViewSize);
			BackgroundBlur.renderBlurredBackground(
				7,
				width,
				height,
				guiLeft + 5,
				guiTop + storageViewSize,
				sizeX - searchNobX - 10,
				sizeY - storageViewSize - 4
			);
		}

		Utils.drawGradientRect(0, 0, width, height, 0xc0101010, 0xd0101010);

		GL11.glPushMatrix();
		GlStateManager.translate(guiLeft, guiTop, 0);

		boolean hoveringOtherBackpack = false;

		//Gui
		Minecraft.getMinecraft().getTextureManager().bindTexture(storageTexture);
		GlStateManager.color(1, 1, 1, 1);
		Utils.drawTexturedRect(0, 0, sizeX, 10, 0, sizeX / 600f, 0, 10 / 400f, GL11.GL_NEAREST);
		Utils.drawTexturedRect(0, 10, sizeX, storageViewSize - 20, 0, sizeX / 600f, 10 / 400f, 94 / 400f, GL11.GL_NEAREST);
		Utils.drawTexturedRect(
			0,
			storageViewSize - 10,
			sizeX,
			110,
			0,
			sizeX / 600f,
			94 / 400f,
			204 / 400f,
			GL11.GL_NEAREST
		);

		int maxScroll = getMaximumScroll();
		if (scroll.getValue() > maxScroll) {
			scroll.setValue(maxScroll);
		}
		if (scroll.getValue() < 0) {
			scroll.setValue(0);
		}

		//Scroll bar
		int scrollBarY = Math.round(getScrollBarHeight() * scroll.getValue() / (float) maxScroll);
		float uMin = scrollGrabOffset >= 0 ? 12 / 600f : 0;
		Utils.drawTexturedRect(
			520,
			8 + scrollBarY,
			12,
			15,
			uMin,
			uMin + 12 / 600f,
			250 / 400f,
			265 / 400f,
			GL11.GL_NEAREST
		);

		int currentPage = StorageManager.getInstance().getCurrentPageId();

		boolean mouseInsideStorages = mouseY > guiTop + 3 && mouseY < guiTop + 3 + storageViewSize;

		//Storages
		boolean doItemRender = true;
		boolean doRenderFramebuffer = false;
		int startY = getPageCoords(0).y;
		if (OpenGlHelper.isFramebufferEnabled()) {
			int h;
			synchronized (StorageManager.getInstance().storageConfig.displayToStorageIdMapRender) {
				int lastDisplayId = StorageManager.getInstance().storageConfig.displayToStorageIdMapRender.size() - 1;
				int coords = (int) Math.ceil(lastDisplayId / 3f) * 3 + 3;

				h = getPageCoords(coords).y + scroll.getValue();
			}
			int w = sizeX;

			//Render from framebuffer
			if (framebuffer != null) {
				GlScissorStack.push(0, guiTop + 3, width, guiTop + 3 + storageViewSize, scaledResolution);
				GlStateManager.enableDepth();
				GlStateManager.translate(0, startY, 107.0001f);
				framebuffer.bindFramebufferTexture();

				GlStateManager.color(1, 1, 1, 1);

				GlStateManager.enableAlpha();
				GlStateManager.alphaFunc(GL11.GL_GREATER, 0F);
				Utils.drawTexturedRect(0, 0, w, h, 0, 1, 1, 0, GL11.GL_NEAREST);
				GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);

				renderEnchOverlay(enchantGlintRenderLocations);

				GlStateManager.translate(0, -startY, -107.0001f);
				GlScissorStack.pop(scaledResolution);
			}

			if (dirty || framebuffer == null) {
				dirty = false;

				int fw = w * scaledResolution.getScaleFactor();
				int fh = h * scaledResolution.getScaleFactor();

				if (framebuffer == null) {
					framebuffer = new Framebuffer(fw, fh, true);
				} else if (framebuffer.framebufferWidth != fw || framebuffer.framebufferHeight != fh) {
					framebuffer.createBindFramebuffer(fw, fh);
				}
				framebuffer.framebufferClear();
				framebuffer.bindFramebuffer(true);

				GlStateManager.matrixMode(GL11.GL_PROJECTION);
				GlStateManager.loadIdentity();
				GlStateManager.ortho(0.0D, w, h, 0.0D, 1000.0D, 3000.0D);
				GlStateManager.matrixMode(GL11.GL_MODELVIEW);

				GlStateManager.pushMatrix();
				GlStateManager.translate(-guiLeft, -guiTop - startY, 0);

				doRenderFramebuffer = true;
			} else {
				doItemRender = false;
			}
		}

		if (doItemRender) {
			enchantGlintRenderLocations.clear();
			for (Map.Entry<Integer, Integer> entry : StorageManager.getInstance().storageConfig.displayToStorageIdMapRender.entrySet()) {
				int displayId = entry.getKey();
				int storageId = entry.getValue();

				IntPair coords = getPageCoords(displayId);
				int storageX = coords.x;
				int storageY = coords.y;

				if (!doRenderFramebuffer) {
					if (coords.y - 11 > 3 + storageViewSize || coords.y + 90 < 3) continue;
				}

				StorageManager.StoragePage page = StorageManager.getInstance().getPage(storageId, false);
				if (page != null && page.rows > 0) {
					int rows = page.rows;

					isPaneCaches[storageId] = new int[page.rows * 9];
					ctmIndexCaches[storageId] = new int[page.rows * 9];
					int[] isPaneCache = isPaneCaches[storageId];
					int[] ctmIndexCache = ctmIndexCaches[storageId];

					for (int k = 0; k < rows * 9; k++) {
						ItemStack stack;

						if (storageId == currentPage) {
							stack = containerChest.getSlot(k + 9).getStack();
						} else {
							stack = page.items[k];
						}

						int itemX = storageX + 1 + 18 * (k % 9);
						int itemY = storageY + 1 + 18 * (k / 9);

						//Render fancy glass
						if (stack != null) {
							int paneType = getPaneType(stack, k, isPaneCache);
							if (paneType > 0) {
								GlStateManager.disableAlpha();
								Gui.drawRect(itemX - 1, itemY - 1, itemX + 17, itemY + 17, 0x01000000);
								GlStateManager.enableAlpha();

								int ctmIndex = getCTMIndex(page, k, isPaneCache, ctmIndexCache);
								int startCTMX = (ctmIndex % 12) * 19;
								int startCTMY = (ctmIndex / 12) * 19;

								ctmIndexCache[k] = ctmIndex;

								if (paneType != 17) {
									int rgb = getRGBFromPane(paneType - 1);
									{
										int a = (rgb >> 24) & 0xFF;
										int r = (rgb >> 16) & 0xFF;
										int g = (rgb >> 8) & 0xFF;
										int b = rgb & 0xFF;
										Minecraft.getMinecraft().getTextureManager().bindTexture(STORAGE_PANE_CTM_TEXTURE);
										GlStateManager.color(r / 255f, g / 255f, b / 255f, a / 255f);
										Utils.drawTexturedRect(
											itemX - 1,
											itemY - 1,
											18,
											18,
											startCTMX / 227f,
											(startCTMX + 18) / 227f,
											startCTMY / 75f,
											(startCTMY + 18) / 75f,
											GL11.GL_NEAREST
										);
									}

									/*int[] colours = new int[9];

									for (int xi = -1; xi <= 1; xi++) {
										for (int yi = -1; yi <= 1; yi++) {
											List<Integer> indexes = new ArrayList<>();
											List<Integer> coloursList = new ArrayList<>();
											coloursList.add(rgb);

											if (xi != 0) {
												indexes.add(k + xi);
											}
											if (yi != 0) {
												indexes.add(k + yi * 9);
											}
											if (xi != 0 && yi != 0) {
												indexes.add(k + yi * 9 + xi);
											}
											for (int index : indexes) {
												if (index >= 0 && index < rows * 9) {
													int paneTypeI = getPaneType(page.items[index], index, isPaneCache);
													if (shouldConnect(paneType, paneTypeI)) {
														coloursList.add(getRGBFromPane(paneTypeI - 1));
													}
												}
											}
											Vector4f cv = new Vector4f();
											for (int colour : coloursList) {
												float a = (colour >> 24) & 0xFF;
												float r = (colour >> 16) & 0xFF;
												float g = (colour >> 8) & 0xFF;
												float b = colour & 0xFF;
												cv.x += a / coloursList.size();
												cv.y += r / coloursList.size();
												cv.z += g / coloursList.size();
												cv.w += b / coloursList.size();
											}
											int finalCol = (((int) cv.x) << 24) | (((int) cv.y) << 16) | (((int) cv.z) << 8) | ((int) cv.w);
											colours[(xi + 1) + (yi + 1) * 3] = finalCol;
										}
									}
									int[] colours4 = new int[16];

									for (int x = 0; x < 4; x++) {
										for (int y = 0; y < 4; y++) {
											int ya = y < 2 ? y : y - 1;
											int xa = x < 2 ? x : x - 1;
											colours4[x + y * 4] = colours[xa + ya * 3];
										}
									}

									GlStateManager.pushMatrix();
									GlStateManager.translate(itemX - 1, itemY - 1, 0);
									Tessellator tessellator = Tessellator.getInstance();
									WorldRenderer worldrenderer = tessellator.getWorldRenderer();
									worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
									float uMinCTM = startCTMX / 227f;
									float uMaxCTM = (startCTMX + 18) / 227f;
									float vMinCTM = startCTMY / 75f;
									float vMaxCTM = (startCTMY + 18) / 75f;
									for (int xi = -1; xi <= 1; xi++) {
										for (int yi = -1; yi <= 1; yi++) {
											float x = xi == -1 ? 0 : xi == 0 ? 1 : 17;
											float y = yi == -1 ? 0 : yi == 0 ? 1 : 17;
											float w = xi == 0 ? 16 : 1;
											float h = yi == 0 ? 16 : 1;

											int col1 = colours4[(xi + 1) + (yi + 1) * 4];
											int col2 = colours4[(xi + 2) + (yi + 1) * 4];
											int col3 = colours4[(xi + 1) + (yi + 2) * 4];
											int col4 = colours4[(xi + 2) + (yi + 2) * 4];

											worldrenderer
												.pos(x, y + h, 0.0D)
												.tex(uMinCTM + (uMaxCTM - uMinCTM) * x / 18f, vMinCTM + (vMaxCTM - vMinCTM) * (y + h) / 18f)
												.color((col3 >> 16) & 0xFF, (col3 >> 8) & 0xFF, col3 & 0xFF, (col3 >> 24) & 0xFF).endVertex();
											worldrenderer
												.pos(x + w, y + h, 0.0D)
												.tex(
													uMinCTM + (uMaxCTM - uMinCTM) * (x + w) / 18f,
													vMinCTM + (vMaxCTM - vMinCTM) * (y + h) / 18f
												)
												.color((col4 >> 16) & 0xFF, (col4 >> 8) & 0xFF, col4 & 0xFF, (col4 >> 24) & 0xFF).endVertex();
											worldrenderer
												.pos(x + w, y, 0.0D)
												.tex(uMinCTM + (uMaxCTM - uMinCTM) * (x + w) / 18f, vMinCTM + (vMaxCTM - vMinCTM) * y / 18f)
												.color((col2 >> 16) & 0xFF, (col2 >> 8) & 0xFF, col2 & 0xFF, (col2 >> 24) & 0xFF).endVertex();
											worldrenderer
												.pos(x, y, 0.0D)
												.tex(uMinCTM + (uMaxCTM - uMinCTM) * x / 18f, vMinCTM + (vMaxCTM - vMinCTM) * y / 18f)
												.color((col1 >> 16) & 0xFF, (col1 >> 8) & 0xFF, col1 & 0xFF, (col1 >> 24) & 0xFF).endVertex();
										}
									}
									GlStateManager.disableDepth();
									GlStateManager.color(1, 1, 1, 1);
									GlStateManager.shadeModel(GL11.GL_SMOOTH);
									tessellator.draw();
									GlStateManager.shadeModel(GL11.GL_FLAT);
									GlStateManager.enableDepth();
									GlStateManager.popMatrix();*/

									RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
									itemRender.renderItemOverlayIntoGUI(
										Minecraft.getMinecraft().fontRendererObj,
										stack,
										itemX,
										itemY,
										null
									);
									GlStateManager.disableLighting();
								}

								page.shouldDarkenIfNotSelected[k] = false;
								continue;
							}
						}
						page.shouldDarkenIfNotSelected[k] = true;

						//Render item
						GlStateManager.translate(0, 0, 20);
						if (doRenderFramebuffer) {
							GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
							GL14.glBlendFuncSeparate(GL11.GL_ONE, GL11.GL_ZERO, GL11.GL_ONE, GL11.GL_ZERO);

							if (storageId == currentPage) {
								Utils.hasEffectOverride = true;
								GlStateManager.translate(storageX - 7, storageY - 17 - 18, 0);
								((AccessorGuiContainer) guiChest).doDrawSlot(containerChest.getSlot(k + 9));
								GlStateManager.translate(-storageX + 7, -storageY + 17 + 18, 0);
								Utils.hasEffectOverride = false;
							} else {
								Utils.drawItemStackWithoutGlint(stack, itemX, itemY);
							}

							GL14.glBlendFuncSeparate(770, 771, 1, 0);

							if (stack != null && (stack.hasEffect() || stack.getItem() == Items.enchanted_book)) {
								enchantGlintRenderLocations.add(new Vector2f(itemX, itemY - startY));
							}
						} else if (storageId == currentPage) {
							Utils.hasEffectOverride = true;
							GlStateManager.translate(storageX - 7, storageY - 17 - 18, 0);
							((AccessorGuiContainer) guiChest).doDrawSlot(containerChest.getSlot(k + 9));
							GlStateManager.translate(-storageX + 7, -storageY + 17 + 18, 0);
							Utils.hasEffectOverride = false;
						} else {
							Utils.drawItemStack(stack, itemX, itemY);
						}
						GlStateManager.disableLighting();
						GlStateManager.translate(0, 0, -20);
					}

					GlStateManager.disableLighting();
					GlStateManager.enableDepth();
				}
			}
		}

		if (OpenGlHelper.isFramebufferEnabled() && doRenderFramebuffer) {
			GlStateManager.popMatrix();
			Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);

			GlStateManager.matrixMode(GL11.GL_PROJECTION);
			GlStateManager.loadIdentity();
			GlStateManager.ortho(0.0D, scaledResolution.getScaledWidth_double(), scaledResolution.getScaledHeight_double(),
				0.0D, 1000.0D, 3000.0D
			);
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		}

		GlScissorStack.push(0, guiTop + 3, width, guiTop + 3 + storageViewSize, scaledResolution);
		for (Map.Entry<Integer, Integer> entry : StorageManager.getInstance().storageConfig.displayToStorageIdMapRender.entrySet()) {
			int displayId = entry.getKey();
			int storageId = entry.getValue();

			IntPair coords = getPageCoords(displayId);
			int storageX = coords.x;
			int storageY = coords.y;

			if (coords.y - 11 > 3 + storageViewSize || coords.y + 90 < 3) continue;

			StorageManager.StoragePage page = StorageManager.getInstance().getPage(storageId, false);

			if (editingNameId == storageId) {
				int len = fontRendererObj.getStringWidth(renameStorageField.getTextDisplay()) + 10;
				renameStorageField.setSize(len, 12);
				renameStorageField.render(storageX, storageY - 13);
			} else {
				String pageTitle;
				if (page != null && page.customTitle != null && !page.customTitle.isEmpty()) {
					pageTitle = Utils.chromaStringByColourCode(page.customTitle);
				} else if (entry.getValue() < 9) {
					pageTitle = "Ender Chest Page " + (entry.getValue() + 1);
				} else {
					pageTitle = "Backpack Slot " + (storageId - 8);
				}
				int titleLen = fontRendererObj.getStringWidth(pageTitle);

				if (mouseX >= guiLeft + storageX && mouseX <= guiLeft + storageX + titleLen + 15 &&
					mouseY >= guiTop + storageY - 14 && mouseY <= guiTop + storageY + 1) {
					pageTitle += " \u270E";
				}
				fontRendererObj.drawString(pageTitle, storageX, storageY - 11, textColour);
			}

			if (page == null) {
				Minecraft.getMinecraft().getTextureManager().bindTexture(storageTexture);
				GlStateManager.color(1, 1, 1, 1);
				int h = 18 * 3;

				Utils.drawTexturedRect(
					storageX,
					storageY,
					162,
					h,
					0,
					162 / 600f,
					265 / 400f,
					(265 + h) / 400f,
					GL11.GL_NEAREST
				);

				Gui.drawRect(storageX, storageY, storageX + 162, storageY + h, 0x80000000);

				if (storageId < 9) {
					Utils.drawStringCenteredScaledMaxWidth("Locked Page",
						storageX + 81, storageY + h / 2, true, 150, 0xd94c00
					);
				} else {
					Utils.drawStringCenteredScaledMaxWidth("Empty Backpack Slot",
						storageX + 81, storageY + h / 2, true, 150, 0xd94c00
					);
				}
			} else if (page.rows <= 0) {
				Minecraft.getMinecraft().getTextureManager().bindTexture(storageTexture);
				GlStateManager.color(1, 1, 1, 1);
				int h = 18 * 3;

				Utils.drawTexturedRect(
					storageX,
					storageY,
					162,
					h,
					0,
					162 / 600f,
					265 / 400f,
					(265 + h) / 400f,
					GL11.GL_NEAREST
				);

				Gui.drawRect(storageX, storageY, storageX + 162, storageY + h, 0x80000000);

				Utils.drawStringCenteredScaledMaxWidth("Click to load items",
					storageX + 81, storageY + h / 2, true, 150, 0xffdf00
				);
			} else {
				int rows = page.rows;

				int storageW = 162;
				int storageH = 18 * rows;

				GlStateManager.enableDepth();

				boolean[] shouldLimitBorder = new boolean[rows * 9];
				boolean hasCaches = isPaneCaches[storageId] != null && isPaneCaches[storageId].length == rows * 9 &&
					ctmIndexCaches[storageId] != null && ctmIndexCaches[storageId].length == rows * 9;

				//Render item connections
				for (int k = 0; k < rows * 9; k++) {
					ItemStack stack = page.items[k];

					if (stack != null && hasCaches) {
						int itemX = storageX + 1 + 18 * (k % 9);
						int itemY = storageY + 1 + 18 * (k / 9);

						int[] isPaneCache = isPaneCaches[storageId];
						int[] ctmIndexCache = ctmIndexCaches[storageId];

						if (isPaneCache[k] == 17) {
							int ctmIndex = getCTMIndex(page, k, isPaneCache, ctmIndexCache);
							int startCTMX = (ctmIndex % 12) * 19;
							int startCTMY = (ctmIndex / 12) * 19;

							int rgb = getRGBFromPane(isPaneCache[k] - 1);
							int a = (rgb >> 24) & 0xFF;
							int r = (rgb >> 16) & 0xFF;
							int g = (rgb >> 8) & 0xFF;
							int b = rgb & 0xFF;
							Minecraft.getMinecraft().getTextureManager().bindTexture(STORAGE_PANE_CTM_TEXTURE);
							GlStateManager.color(r / 255f, g / 255f, b / 255f, a / 255f);
							GlStateManager.translate(0, 0, 110);
							Utils.drawTexturedRect(itemX - 1, itemY - 1, 18, 18,
								startCTMX / 227f, (startCTMX + 18) / 227f, startCTMY / 75f, (startCTMY + 18) / 75f, GL11.GL_NEAREST
							);
							GlStateManager.translate(0, 0, -110);

							RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
							itemRender.renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRendererObj, stack, itemX, itemY, null);
							GlStateManager.enableDepth();
						} else if (isPaneCache[k] < 0) {
							boolean hasConnection = false;

							int upIndex = k - 9;
							int leftIndex = k % 9 > 0 ? k - 1 : -1;
							int rightIndex = k % 9 < 8 ? k + 1 : -1;
							int downIndex = k + 9;

							int[] indexArr = {rightIndex, downIndex, leftIndex, upIndex};

							for (int j = 0; j < 4; j++) {
								int index = indexArr[j];
								int type = index >= 0 && index < isPaneCache.length
									? getPaneType(page.items[index], index, isPaneCache)
									: -1;
								if (type > 0) {
									int ctmIndex = getCTMIndex(page, index, isPaneCache, ctmIndexCache);
									if (ctmIndex < 0) continue;

									boolean renderConnection;
									boolean horizontal = ctmIndex == 1 || ctmIndex == 2 || ctmIndex == 3;
									boolean vertical = ctmIndex == 12 || ctmIndex == 24 || ctmIndex == 36;
									if ((k % 9 == 0 && index % 9 == 0) || (k % 9 == 8 && index % 9 == 8)) {
										renderConnection = horizontal || vertical;
									} else if (index == leftIndex || index == rightIndex) {
										renderConnection = horizontal;
									} else {
										renderConnection = vertical;
									}

									if (renderConnection) {
										shouldLimitBorder[k] = true;
										hasConnection = true;

										Minecraft.getMinecraft().getTextureManager().bindTexture(STORAGE_PANE_CTM_TEXTURE);
										int rgb = getRGBFromPane(type - 1);
										int a = (rgb >> 24) & 0xFF;
										int r = (rgb >> 16) & 0xFF;
										int g = (rgb >> 8) & 0xFF;
										int b = rgb & 0xFF;
										GlStateManager.color(r / 255f, g / 255f, b / 255f, a / 255f);

										GlStateManager.pushMatrix();
										GlStateManager.translate(itemX - 1 + 9, itemY - 1 + 9, 10);
										GlStateManager.rotate(j * 90, 0, 0, 1);
										GlStateManager.enableAlpha();
										GlStateManager.disableLighting();

										boolean horzFlip = false;
										boolean vertFlip = false;

										if (index == leftIndex) {
											vertFlip = true;
										} else if (index == downIndex) {
											vertFlip = true;
										}

										GlStateManager.enableDepth();
										Utils.drawTexturedRect(0, -9, 8, 18,
											!horzFlip ? 209 / 227f : 219 / 227f, horzFlip ? 227 / 227f : 217 / 227f,
											!vertFlip ? 57 / 75f : 75f / 75f, vertFlip ? 57 / 75f : 75f / 75f, GL11.GL_NEAREST
										);
										GlStateManager.translate(0, 0, 120);
										Utils.drawTexturedRect(8, -9, 10, 18,
											!horzFlip ? 217 / 227f : 209 / 227f, horzFlip ? 219 / 227f : 227 / 227f,
											!vertFlip ? 57 / 75f : 75f / 75f, vertFlip ? 57 / 75f : 75f / 75f, GL11.GL_NEAREST
										);
										GlStateManager.translate(0, 0, -120);

										GlStateManager.popMatrix();
									}
								}
							}

							if (hasConnection) {
								page.shouldDarkenIfNotSelected[k] = false;

								GlStateManager.disableAlpha();
								GlStateManager.translate(0, 0, 10);
								Gui.drawRect(itemX - 1, itemY - 1, itemX + 17, itemY + 17, 0x01000000);
								GlStateManager.translate(0, 0, -10);
								GlStateManager.enableAlpha();
							}
						}
					}
				}

				Minecraft.getMinecraft().getTextureManager().bindTexture(storageTexture);
				GlStateManager.color(1, 1, 1, 1);
				Utils.drawTexturedRect(
					storageX,
					storageY,
					storageW,
					storageH,
					0,
					162 / 600f,
					265 / 400f,
					(265 + storageH) / 400f,
					GL11.GL_NEAREST
				);

				boolean whiteOverlay = false;

				for (int k = 0; k < rows * 9; k++) {
					ItemStack stack = page.items[k];
					int itemX = storageX + 1 + 18 * (k % 9);
					int itemY = storageY + 1 + 18 * (k / 9);

					if (!searchBar.getText().isEmpty()) {
						if (stack == null || !manager.doesStackMatchSearch(stack, searchBar.getText())) {
							GlStateManager.disableDepth();
							Gui.drawRect(itemX, itemY, itemX + 16, itemY + 16, 0x80000000);
							GlStateManager.enableDepth();
						}
					}

					GlStateManager.disableLighting();

					if (mouseInsideStorages && mouseX >= guiLeft + itemX && mouseX < guiLeft + itemX + 18 &&
						mouseY >= guiTop + itemY && mouseY < guiTop + itemY + 18) {
						boolean allowHover = NotEnoughUpdates.INSTANCE.config.storageGUI.fancyPanes != 1 || !hasCaches ||
							isPaneCaches[storageId][k] <= 0;

						if (storageId != StorageManager.getInstance().getCurrentPageId()) {
							hoveringOtherBackpack = true;
							whiteOverlay = stackOnMouse == null;
						} else if (stack == null || allowHover) {
							itemHoverX = itemX;
							itemHoverY = itemY;
						}

						if (stack != null && allowHover) {
							tooltipToDisplay = stack.getTooltip(
								Minecraft.getMinecraft().thePlayer,
								Minecraft.getMinecraft().gameSettings.advancedItemTooltips
							);
						}
					}
				}

				GlStateManager.disableDepth();
				if (storageId == currentPage) {
					if (isPaneCaches[storageId] != null && isPaneCaches[storageId].length == rows * 9 &&
						ctmIndexCaches[storageId] != null && ctmIndexCaches[storageId].length == rows * 9) {
						int[] isPaneCache = isPaneCaches[storageId];

						int borderStartY = 0;
						int borderEndY = storageH;
						int borderStartX = 0;
						int borderEndX = storageW;

						boolean allChroma = true;
						for (int y = 0; y < page.rows; y++) {
							for (int x = 0; x < 9; x++) {
								int index = x + y * 9;
								if (isPaneCache[index] != 17) {
									allChroma = false;
									break;
								}
							}
						}

						out:
						for (int y = 0; y < page.rows; y++) {
							for (int x = 0; x < 9; x++) {
								int index = x + y * 9;
								if (isPaneCache[index] <= 0 && !shouldLimitBorder[index]) {
									borderStartY = y * 18;
									break out;
								}
							}
						}
						out:
						for (int y = page.rows - 1; y >= 0; y--) {
							for (int x = 0; x < 9; x++) {
								int index = x + y * 9;
								if (isPaneCache[index] <= 0 && !shouldLimitBorder[index]) {
									borderEndY = y * 18 + 18; //Bottom
									break out;
								}
							}
						}
						out:
						for (int x = 0; x < 9; x++) {
							for (int y = 0; y < page.rows; y++) {
								int index = x + y * 9;
								if (isPaneCache[index] <= 0 && !shouldLimitBorder[index]) {
									borderStartX = x * 18;
									break out;
								}
							}
						}
						out:
						for (int x = 8; x >= 0; x--) {
							for (int y = 0; y < page.rows; y++) {
								int index = x + y * 9;
								if (isPaneCache[index] <= 0 && !shouldLimitBorder[index]) {
									borderEndX = x * 18 + 18; //Bottom
									break out;
								}
							}
						}
						int borderColour =
							ChromaColour.specialToChromaRGB(NotEnoughUpdates.INSTANCE.config.storageGUI.selectedStorageColour);
						Gui.drawRect(
							storageX + borderStartX + 1,
							storageY + borderStartY,
							storageX + borderStartX,
							storageY + borderEndY,
							borderColour
						); //Left
						Gui.drawRect(
							storageX + borderEndX - 1,
							storageY + borderStartY,
							storageX + borderEndX,
							storageY + borderEndY,
							borderColour
						); //Right
						Gui.drawRect(
							storageX + borderStartX,
							storageY + borderStartY,
							storageX + borderEndX,
							storageY + borderStartY + 1,
							borderColour
						); //Top
						Gui.drawRect(
							storageX + borderStartX,
							storageY + borderEndY - 1,
							storageX + borderEndX,
							storageY + borderEndY,
							borderColour
						); //Bottom

						if (allChroma) {
							ResourceLocation loc;
							if (rollIndex < NOT_RICKROLL_SEQ.length) {
								loc = NOT_RICKROLL_SEQ[rollIndex];
							} else {
								loc = NOT_RICKROLL_SEQ[NOT_RICKROLL_SEQ.length * 2 - rollIndex - 1];
							}
							Minecraft.getMinecraft().getTextureManager().bindTexture(loc);
							GlStateManager.color(1, 1, 1, 1);
							Utils.drawTexturedRect(storageX, storageY, storageW, storageH, GL11.GL_LINEAR);
						}
					} else {
						int borderColour =
							ChromaColour.specialToChromaRGB(NotEnoughUpdates.INSTANCE.config.storageGUI.selectedStorageColour);
						Gui.drawRect(storageX + 1, storageY, storageX, storageY + storageH, borderColour); //Left
						Gui.drawRect(
							storageX + storageW - 1,
							storageY,
							storageX + storageW,
							storageY + storageH,
							borderColour
						); //Right
						Gui.drawRect(storageX, storageY - 1, storageX + storageW, storageY, borderColour); //Top
						Gui.drawRect(
							storageX,
							storageY + storageH - 1,
							storageX + storageW,
							storageY + storageH,
							borderColour
						); //Bottom
					}
				} else if (whiteOverlay) {
					Gui.drawRect(storageX, storageY, storageX + storageW, storageY + storageH, 0x80ffffff);
				} else {
					if (page.rows <= 0) {
						Gui.drawRect(storageX, storageY, storageX + storageW, storageY + storageH, 0x40000000);
					} else {
						for (int i = 0; i < page.rows * 9; i++) {
							if (page.items[i] == null || page.shouldDarkenIfNotSelected[i]) {
								int x = storageX + 18 * (i % 9);
								int y = storageY + 18 * (i / 9);
								Gui.drawRect(x, y, x + 18, y + 18, 0x40000000);
							}
						}
					}
				}

				GlStateManager.enableDepth();
			}
		}
		GlScissorStack.pop(scaledResolution);

		if (fastRender) {
			fontRendererObj.drawString(
				"Fast render and antialiasing do not work with Storage overlay.",
				sizeX / 2 - fontRendererObj.getStringWidth("Fast render and antialiasing do not work with Storage overlay.") / 2,
				-10,
				0xFFFF0000
			);
		}

		//Inventory Text
		fontRendererObj.drawString("Inventory", 180, storageViewSize + 6, textColour);
		searchBar.setCustomTextColour(searchTextColour);
		searchBar.render(252, storageViewSize + 5);

		//Player Inventory
		ItemStack[] playerItems = Minecraft.getMinecraft().thePlayer.inventory.mainInventory;
		int inventoryStartIndex = containerChest.getLowerChestInventory().getSizeInventory();
		GlStateManager.enableDepth();
		for (int i = 0; i < 9; i++) {
			int itemX = 181 + 18 * i;
			int itemY = storageViewSize + 76;

			GlStateManager.pushMatrix();
			GlStateManager.translate(181 - 8, storageViewSize + 18 - (inventoryStartIndex / 9 * 18 + 31), 0);
			((AccessorGuiContainer) guiChest).doDrawSlot(containerChest.inventorySlots.get(inventoryStartIndex + i));
			GlStateManager.popMatrix();

			if (!searchBar.getText().isEmpty()) {
				if (playerItems[i] == null || !manager.doesStackMatchSearch(
					playerItems[i],
					searchBar.getText()
				)) {
					GlStateManager.disableDepth();
					Gui.drawRect(itemX, itemY, itemX + 16, itemY + 16, 0x80000000);
					GlStateManager.enableDepth();
				}
			}

			if (mouseX >= guiLeft + itemX && mouseX < guiLeft + itemX + 18 && mouseY >= guiTop + itemY &&
				mouseY < guiTop + itemY + 18) {
				itemHoverX = itemX;
				itemHoverY = itemY;

				if (playerItems[i] != null) {
					tooltipToDisplay = playerItems[i].getTooltip(
						Minecraft.getMinecraft().thePlayer,
						Minecraft.getMinecraft().gameSettings.advancedItemTooltips
					);
				}
			}
		}
		for (int i = 0; i < 27; i++) {
			int itemX = 181 + 18 * (i % 9);
			int itemY = storageViewSize + 18 + 18 * (i / 9);

			//Utils.drawItemStack(playerItems[i+9], itemX, itemY);
			GlStateManager.pushMatrix();
			GlStateManager.translate(181 - 8, storageViewSize + 18 - (inventoryStartIndex / 9 * 18 + 31), 0);
			((AccessorGuiContainer) guiChest).doDrawSlot(containerChest.inventorySlots.get(inventoryStartIndex + 9 + i));
			GlStateManager.popMatrix();

			if (!searchBar.getText().isEmpty()) {
				if (playerItems[i + 9] == null || !manager.doesStackMatchSearch(
					playerItems[i + 9],
					searchBar.getText()
				)) {
					GlStateManager.disableDepth();
					Gui.drawRect(itemX, itemY, itemX + 16, itemY + 16, 0x80000000);
					GlStateManager.enableDepth();
				}
			}

			if (mouseX >= guiLeft + itemX && mouseX < guiLeft + itemX + 18 && mouseY >= guiTop + itemY &&
				mouseY < guiTop + itemY + 18) {
				itemHoverX = itemX;
				itemHoverY = itemY;

				if (playerItems[i + 9] != null) {
					tooltipToDisplay = playerItems[i + 9].getTooltip(
						Minecraft.getMinecraft().thePlayer,
						Minecraft.getMinecraft().gameSettings.advancedItemTooltips
					);
				}
			}
		}

		//Backpack Selector
		fontRendererObj.drawString("Ender Chest Pages", 9, storageViewSize + 12, textColour);
		fontRendererObj.drawString("Storage Pages", 9, storageViewSize + 44, textColour);
		if (StorageManager.getInstance().onStorageMenu) {
			for (int i = 0; i < 9; i++) {
				int itemX = 10 + i * 18;
				int itemY = storageViewSize + 24;
				ItemStack stack = containerChest.getLowerChestInventory().getStackInSlot(i + 9);
				Utils.drawItemStack(stack, itemX, itemY);

				if (mouseX >= guiLeft + itemX && mouseX < guiLeft + itemX + 18 && mouseY >= guiTop + itemY &&
					mouseY < guiTop + itemY + 18) {
					itemHoverX = itemX;
					itemHoverY = itemY;

					if (stack != null) {
						if (NotEnoughUpdates.INSTANCE.config.storageGUI.enderchestPreview) slotPreview = i;
						tooltipToDisplay = stack.getTooltip(
							Minecraft.getMinecraft().thePlayer,
							Minecraft.getMinecraft().gameSettings.advancedItemTooltips
						);
					}
				}
			}
			for (int i = 0; i < 18; i++) {
				int itemX = 10 + 18 * (i % 9);
				int itemY = storageViewSize + 56 + 18 * (i / 9);
				ItemStack stack = containerChest.getLowerChestInventory().getStackInSlot(i + 27);
				Utils.drawItemStack(stack, itemX, itemY);

				if (mouseX >= guiLeft + itemX && mouseX < guiLeft + itemX + 18 && mouseY >= guiTop + itemY &&
					mouseY < guiTop + itemY + 18) {
					itemHoverX = itemX;
					itemHoverY = itemY;

					if (stack != null) {
						if (NotEnoughUpdates.INSTANCE.config.storageGUI.backpackPreview)
							slotPreview = i + StorageManager.MAX_ENDER_CHEST_PAGES;
						tooltipToDisplay = stack.getTooltip(
							Minecraft.getMinecraft().thePlayer,
							Minecraft.getMinecraft().gameSettings.advancedItemTooltips
						);
					}
				}
			}
		} else {
			for (int i = 0; i < 9; i++) {
				StorageManager.StoragePage page = StorageManager.getInstance().getPage(i, false);
				int itemX = 10 + (i % 9) * 18;
				int itemY = storageViewSize + 24 + (i / 9) * 18;

				ItemStack stack;
				if (page != null && page.backpackDisplayStack != null) {
					stack = page.backpackDisplayStack;
				} else {
					stack = StorageManager.LOCKED_ENDERCHEST_STACK;
				}

				if (stack != null) {
					Utils.drawItemStack(stack, itemX, itemY);

					if (mouseX >= guiLeft + itemX && mouseX < guiLeft + itemX + 18 && mouseY >= guiTop + itemY &&
						mouseY < guiTop + itemY + 18) {
						itemHoverX = itemX;
						itemHoverY = itemY;
						if (NotEnoughUpdates.INSTANCE.config.storageGUI.enderchestPreview) slotPreview = i;
						tooltipToDisplay = stack.getTooltip(
							Minecraft.getMinecraft().thePlayer,
							Minecraft.getMinecraft().gameSettings.advancedItemTooltips
						);
					}
				}
			}
			for (int i = 0; i < 18; i++) {
				StorageManager.StoragePage page = StorageManager.getInstance().getPage(
					i + StorageManager.MAX_ENDER_CHEST_PAGES,
					false
				);
				int itemX = 10 + (i % 9) * 18;
				int itemY = storageViewSize + 56 + (i / 9) * 18;

				ItemStack stack;
				if (page != null && page.backpackDisplayStack != null) {
					stack = page.backpackDisplayStack;
				} else {
					stack = StorageManager.getInstance().getMissingBackpackStack(i);
				}

				if (stack != null) {
					Utils.drawItemStack(stack, itemX, itemY);

					if (mouseX >= guiLeft + itemX && mouseX < guiLeft + itemX + 18 && mouseY >= guiTop + itemY &&
						mouseY < guiTop + itemY + 18) {
						itemHoverX = itemX;
						itemHoverY = itemY;
						if (NotEnoughUpdates.INSTANCE.config.storageGUI.backpackPreview)
							slotPreview = i + StorageManager.MAX_ENDER_CHEST_PAGES;
						tooltipToDisplay = stack.getTooltip(
							Minecraft.getMinecraft().thePlayer,
							Minecraft.getMinecraft().gameSettings.advancedItemTooltips
						);

						if (!StorageManager.getInstance().onStorageMenu) {
							List<String> tooltip = new ArrayList<>();
							for (String line : tooltipToDisplay) {
								tooltip.add(line.replace("Right-click to remove", "Click \"Edit\" to manage"));
							}
							tooltipToDisplay = tooltip;
						}
					}
				}
			}
		}

		//Buttons
		Minecraft.getMinecraft().getTextureManager().bindTexture(STORAGE_ICONS_TEXTURE);
		GlStateManager.color(1, 1, 1, 1);
		for (int i = 0; i < 10; i++) {
			int buttonX = 388 + (i % 5) * 18;
			int buttonY = getStorageViewSize() + 35 + (i / 5) * 18;

			float minU = (i * 16) / 256f;
			float maxU = (i * 16 + 16) / 256f;

			int vIndex = 0;

			switch (i) {
				case 2:
					vIndex = NotEnoughUpdates.INSTANCE.config.storageGUI.displayStyle;
					break;
				case 3:
					vIndex = NotEnoughUpdates.INSTANCE.config.storageGUI.backpackPreview ? 1 : 0;
					break;
				case 4:
					vIndex = NotEnoughUpdates.INSTANCE.config.storageGUI.enderchestPreview ? 1 : 0;
					break;
				case 5:
					vIndex = NotEnoughUpdates.INSTANCE.config.storageGUI.masonryMode ? 1 : 0;
					break;
				case 6:
					vIndex = NotEnoughUpdates.INSTANCE.config.storageGUI.fancyPanes == 2
						? 0
						: NotEnoughUpdates.INSTANCE.config.storageGUI.fancyPanes + 1;
					break;
				case 7:
					vIndex = NotEnoughUpdates.INSTANCE.config.storageGUI.searchBarAutofocus ? 1 : 0;
					break;
				case 8:
					vIndex = NotEnoughUpdates.INSTANCE.config.storageGUI.showEnchantGlint ? 1 : 0;
					break;
			}

			Utils.drawTexturedRect(
				buttonX,
				buttonY,
				16,
				16,
				minU,
				maxU,
				(vIndex * 16) / 256f,
				(vIndex * 16 + 16) / 256f,
				GL11.GL_NEAREST
			);

			if (mouseX >= guiLeft + buttonX && mouseX < guiLeft + buttonX + 18 &&
				mouseY >= guiTop + buttonY && mouseY < guiTop + buttonY + 18) {
				switch (i) {
					case 0:
						tooltipToDisplay = createTooltip(
							"Enable GUI",
							0,
							"On",
							"Off"
						);
						break;
					case 1:
						int tooltipStorageHeight = desiredHeightSwitch != -1 ? desiredHeightSwitch :
							NotEnoughUpdates.INSTANCE.config.storageGUI.storageHeight;
						tooltipToDisplay = createTooltip(
							"Storage View Height",
							Math.round((tooltipStorageHeight - 104) / 52f),
							"Tiny",
							"Small",
							"Medium",
							"Large",
							"Huge"
						);
						if (desiredHeightSwitch != -1) {
							tooltipToDisplay.add("");
							tooltipToDisplay.add(EnumChatFormatting.YELLOW + "* Move mouse to apply changes *");
						}
						break;
					case 2:
						tooltipToDisplay = createTooltip(
							"Overlay Style",
							NotEnoughUpdates.INSTANCE.config.storageGUI.displayStyle,
							"Transparent",
							"Minecraft",
							"Dark",
							"Custom"
						);
						break;
					case 3:
						tooltipToDisplay = createTooltip(
							"Backpack Preview",
							NotEnoughUpdates.INSTANCE.config.storageGUI.backpackPreview ? 0 : 1,
							"On",
							"Off"
						);
						break;
					case 4:
						tooltipToDisplay = createTooltip(
							"Enderchest Preview",
							NotEnoughUpdates.INSTANCE.config.storageGUI.enderchestPreview ? 0 : 1,
							"On",
							"Off"
						);
						break;
					case 5:
						tooltipToDisplay = createTooltip(
							"Compact Vertically",
							NotEnoughUpdates.INSTANCE.config.storageGUI.masonryMode ? 0 : 1,
							"On",
							"Off"
						);
						break;
					case 6:
						tooltipToDisplay = createTooltip(
							"Fancy Glass Panes",
							NotEnoughUpdates.INSTANCE.config.storageGUI.fancyPanes,
							"On",
							"Locked",
							"Off"
						);
						tooltipToDisplay.add(1, "\u00a7eReplace the glass pane textures");
						tooltipToDisplay.add(2, "\u00a7ein your storage containers with");
						tooltipToDisplay.add(3, "\u00a7ea fancy connected texture");
						break;
					case 7:
						tooltipToDisplay = createTooltip(
							"Search Bar Autofocus",
							NotEnoughUpdates.INSTANCE.config.storageGUI.searchBarAutofocus ? 0 : 1,
							"On",
							"Off"
						);
						break;
					case 8:
						tooltipToDisplay = createTooltip(
							"Show Enchant Glint",
							NotEnoughUpdates.INSTANCE.config.storageGUI.showEnchantGlint ? 0 : 1,
							"On",
							"Off"
						);
						break;
					case 9:
						tooltipToDisplay = createTooltip(
							"Disable optifine CIT",
							!NotEnoughUpdates.INSTANCE.config.storageGUI.disableCIT ? 0 : 1,
							"CIT Enabled",
							"CIT Disabled"
						);
						break;
				}
			}
		}

		if (!StorageManager.getInstance().onStorageMenu) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(storageTexture);
			GlStateManager.color(1, 1, 1, 1);
			Utils.drawTexturedRect(
				171 - 36,
				41 + storageViewSize,
				36,
				14,
				24 / 600f,
				60 / 600f,
				251 / 400f,
				265 / 400f,
				GL11.GL_NEAREST
			);
		}

		if (itemHoverX >= 0 && itemHoverY >= 0) {
			GlStateManager.disableDepth();
			GlStateManager.colorMask(true, true, true, false);
			Gui.drawRect(itemHoverX, itemHoverY, itemHoverX + 16, itemHoverY + 16, 0x80ffffff);
			GlStateManager.colorMask(true, true, true, true);
			GlStateManager.enableDepth();
		}

		GlStateManager.popMatrix();
		GlStateManager.translate(0, 0, 300);
		allowTypingInSearchBar = false;
		if (stackOnMouse != null) {
			GlStateManager.enableDepth();
			if (hoveringOtherBackpack) {
				Utils.drawItemStack(new ItemStack(Item.getItemFromBlock(Blocks.barrier)), mouseX - 8, mouseY - 8);
			} else {
				Utils.drawItemStack(stackOnMouse, mouseX - 8, mouseY - 8);
			}
		} else if (slotPreview >= 0) {
			StorageManager.StoragePage page = StorageManager.getInstance().getPage(slotPreview, false);
			if (page != null && page.rows > 0) {
				int rows = page.rows;

				GlStateManager.translate(0, 0, 100);
				GlStateManager.disableDepth();
				BackgroundBlur.renderBlurredBackground(7, width, height, mouseX + 2, mouseY + 2, 172, 10 + 18 * rows);
				Utils.drawGradientRect(mouseX + 2, mouseY + 2, mouseX + 174, mouseY + 12 + 18 * rows, 0xc0101010, 0xd0101010);

				Minecraft.getMinecraft().getTextureManager().bindTexture(storagePreviewTexture);
				GlStateManager.color(1, 1, 1, 1);
				Utils.drawTexturedRect(mouseX, mouseY, 176, 7, 0, 1, 0, 7 / 32f, GL11.GL_NEAREST);
				for (int i = 0; i < rows; i++) {
					Utils.drawTexturedRect(mouseX, mouseY + 7 + 18 * i, 176, 18, 0, 1, 7 / 32f, 25 / 32f, GL11.GL_NEAREST);
				}
				Utils.drawTexturedRect(mouseX, mouseY + 7 + 18 * rows, 176, 7, 0, 1, 25 / 32f, 1, GL11.GL_NEAREST);
				GlStateManager.enableDepth();

				for (int i = 0; i < rows * 9; i++) {
					ItemStack stack = page.items[i];
					if (stack != null) {
						GlStateManager.enableDepth();
						Utils.drawItemStack(stack, mouseX + 8 + 18 * (i % 9), mouseY + 8 + 18 * (i / 9));
						GlStateManager.disableDepth();
					}
				}
				GlStateManager.translate(0, 0, -100);
			} else {
				Utils.drawHoveringText(tooltipToDisplay, mouseX, mouseY, width, height, -1);
			}
		} else if (tooltipToDisplay != null) {
			Utils.drawHoveringText(tooltipToDisplay, mouseX, mouseY, width, height, -1);
		} else {
			allowTypingInSearchBar = true;
		}
		GlStateManager.translate(0, 0, -300);
	}

	private List<String> createTooltip(String title, int selectedOption, String... options) {
		String selPrefix = EnumChatFormatting.DARK_AQUA + " \u25b6 ";
		String unselPrefix = EnumChatFormatting.GRAY.toString();

		for (int i = 0; i < options.length; i++) {
			if (i == selectedOption) {
				options[i] = selPrefix + options[i];
			} else {
				options[i] = unselPrefix + options[i];
			}
		}

		List<String> list = Lists.newArrayList(options);
		list.add(0, "");
		list.add(0, EnumChatFormatting.GREEN + title);
		return list;
	}

	public IntPair getPageCoords(int displayId) {
		if (displayId < 0) displayId = 0;

		int y;
		if (NotEnoughUpdates.INSTANCE.config.storageGUI.masonryMode) {
			y = -scroll.getValue() + 18 + 108 * (displayId / 3);
		} else {
			y = -scroll.getValue() + 17 + 104 * (displayId / 3);
		}
		for (int i = 0; i <= displayId - 3; i += 3) {
			int maxRows = 1;
			for (int j = i; j < i + 3; j++) {
				if (NotEnoughUpdates.INSTANCE.config.storageGUI.masonryMode && displayId % 3 != j % 3) continue;

				if (!StorageManager.getInstance().storageConfig.displayToStorageIdMapRender.containsKey(j)) {
					continue;
				}
				int storageId = StorageManager.getInstance().storageConfig.displayToStorageIdMapRender.get(j);
				StorageManager.StoragePage page = StorageManager.getInstance().getPage(storageId, false);
				if (page == null || page.rows <= 0) {
					maxRows = Math.max(maxRows, 3);
				} else {
					maxRows = Math.max(maxRows, page.rows);
				}
			}
			y -= (5 - maxRows) * 18;
		}

		return new IntPair(8 + 172 * (displayId % 3), y);
	}

	@Override
	public boolean mouseInput(int mouseX, int mouseY) {
		if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) return false;

		int dWheel = Mouse.getEventDWheel();
		if (!(NotEnoughUpdates.INSTANCE.config.storageGUI.cancelScrollKey != 0 &&
			KeybindHelper.isKeyDown(NotEnoughUpdates.INSTANCE.config.storageGUI.cancelScrollKey)) && dWheel != 0) {
			if (dWheel < 0) {
				dWheel = -1;
				if (scrollVelocity > 0) scrollVelocity = 0;
			}
			if (dWheel > 0) {
				dWheel = 1;
				if (scrollVelocity < 0) scrollVelocity = 0;
			}

			long currentTime = System.currentTimeMillis();
			if (currentTime - lastScroll > 200) {
				scrollVelocity = 0;
			} else {
				scrollVelocity = (int) (scrollVelocity / 1.3f);
			}
			lastScroll = currentTime;

			scrollVelocity += dWheel * 10;
			scrollToY(scroll.getTarget() - scrollVelocity);

			return true;
		}

		if (Mouse.getEventButtonState()) {
			editingNameId = -1;
		}

		if (Mouse.getEventButton() == 0) {
			if (!Mouse.getEventButtonState()) {
				scrollGrabOffset = -1;
			} else if (mouseX >= guiLeft + 519 && mouseX <= guiLeft + 519 + 14 &&
				mouseY >= guiTop + 8 && mouseY <= guiTop + 2 + getStorageViewSize()) {
				int scrollMouseY = mouseY - (guiTop + 8);
				int scrollBarY = Math.round(getScrollBarHeight() * scroll.getValue() / (float) getMaximumScroll());

				if (scrollMouseY >= scrollBarY && scrollMouseY < scrollBarY + 12) {
					scrollGrabOffset = scrollMouseY - scrollBarY;
				}
			}
		}
		if (scrollGrabOffset >= 0 && Mouse.getEventButton() == -1 && !Mouse.getEventButtonState()) {
			int scrollMouseY = mouseY - (guiTop + 8);
			int scrollBarY = scrollMouseY - scrollGrabOffset;

			scrollToY(Math.round(scrollBarY * getMaximumScroll() / (float) getScrollBarHeight()));
			scroll.setTimeToReachTarget(10);
		}

		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		int width = scaledResolution.getScaledWidth();
		int height = scaledResolution.getScaledHeight();

		int storageViewSize = getStorageViewSize();

		int sizeX = 540;
		int sizeY = 100 + storageViewSize;
		int searchNobX = 18;

		guiLeft = width / 2 - (sizeX - searchNobX) / 2;
		guiTop = height / 2 - sizeY / 2;

		if (Mouse.getEventButtonState() && !StorageManager.getInstance().onStorageMenu) {
			if (mouseX > guiLeft + 171 - 36 && mouseX < guiLeft + 171 &&
				mouseY > guiTop + 41 + storageViewSize && mouseY < guiTop + 41 + storageViewSize + 14) {
				NotEnoughUpdates.INSTANCE.sendChatMessage("/storage");
				searchBar.setFocus(false);
				return true;
			}
		}

		if (Mouse.getEventButtonState()) {
			if (mouseX >= guiLeft + 252 && mouseX <= guiLeft + 252 + searchBar.getWidth() &&
				mouseY >= guiTop + storageViewSize + 5 && mouseY <= guiTop + storageViewSize + 5 + searchBar.getHeight()) {
				if (searchBar.getFocus()) {
					searchBar.mouseClicked(mouseX - guiLeft, mouseY - guiTop, Mouse.getEventButton());
					StorageManager.getInstance().searchDisplay(searchBar.getText());
					dirty = true;
				} else {
					searchBar.setFocus(true);
					if (Mouse.getEventButton() == 1) {
						searchBar.setText("");
						StorageManager.getInstance().searchDisplay(searchBar.getText());
						dirty = true;
					}
				}
			} else {
				searchBar.setFocus(false);
			}
		}

		if (mouseX > guiLeft + 181 && mouseX < guiLeft + 181 + 162 &&
			mouseY > guiTop + storageViewSize + 18 && mouseY < guiTop + storageViewSize + 94) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
				dirty = true;
			return false;
		}

		if (mouseY > guiTop + 3 && mouseY < guiTop + storageViewSize + 3) {
			int currentPage = StorageManager.getInstance().getCurrentPageId();
			for (Map.Entry<Integer, Integer> entry : StorageManager.getInstance().storageConfig.displayToStorageIdMapRender.entrySet()) {
				IntPair pageCoords = getPageCoords(entry.getKey());

				if (pageCoords.y > storageViewSize + 3 || pageCoords.y + 90 < 3) continue;

				StorageManager.StoragePage page = StorageManager.getInstance().getPage(entry.getValue(), false);
				int rows = page == null ? 3 : page.rows <= 0 ? 3 : page.rows;

				if (page != null) {
					String pageTitle;
					if (page.customTitle != null && !page.customTitle.isEmpty()) {
						pageTitle = page.customTitle;
					} else if (entry.getValue() < 9) {
						pageTitle = "Ender Chest Page " + (entry.getValue() + 1);
					} else {
						pageTitle = "Backpack Slot " + (entry.getValue() - 8);
					}
					int titleLen = Minecraft.getMinecraft().fontRendererObj.getStringWidth(pageTitle);

					if (mouseX >= guiLeft + pageCoords.x && mouseX <= guiLeft + pageCoords.x + titleLen + 15 &&
						mouseY >= guiTop + pageCoords.y - 14 && mouseY <= guiTop + pageCoords.y + 1) {
						if (Mouse.getEventButtonState() && (Mouse.getEventButton() == 0 || Mouse.getEventButton() == 1)) {
							if (editingNameId != entry.getValue()) {
								editingNameId = entry.getValue();
								if (!renameStorageField.getText().equalsIgnoreCase(pageTitle)) {
									renameStorageField.setText(pageTitle);
								}
							}
							if (!renameStorageField.getFocus()) {
								renameStorageField.setFocus(true);
							} else {
								renameStorageField.mouseClicked(mouseX - guiLeft, mouseY - guiTop, Mouse.getEventButton());
							}
						} else if (Mouse.getEventButton() < 0 && Mouse.isButtonDown(0)) {
							renameStorageField.mouseClickMove(mouseX - guiLeft, mouseY - guiTop, 0, 0);
						}
						return true;
					}
				}

				if (mouseX > guiLeft + pageCoords.x && mouseX < guiLeft + pageCoords.x + 162 &&
					mouseY > guiTop + pageCoords.y && mouseY < guiTop + pageCoords.y + rows * 18) {
					if (currentPage >= 0 && entry.getValue() == currentPage) {
						dirty = true;
						return false;
					} else {
						if (Mouse.getEventButtonState() && Mouse.getEventButton() == 0 &&
							Minecraft.getMinecraft().thePlayer.inventory.getItemStack() == null &&
							page != null) {
							scrollToStorage(entry.getKey(), false);
							StorageManager.getInstance().sendToPage(entry.getValue());
							return true;
						}
					}
				}
			}
		}

		for (int i = 0; i < 10; i++) {
			int buttonX = 388 + (i % 5) * 18;
			int buttonY = getStorageViewSize() + 35 + (i / 5) * 18;

			float minU = (i * 16) / 256f;
			float maxU = (i * 16 + 16) / 256f;

			int vIndex = 0;

			switch (i) {
				case 2:
					vIndex = NotEnoughUpdates.INSTANCE.config.storageGUI.displayStyle;
					break;
				/*case 3:
					vIndex = */
			}

			Utils.drawTexturedRect(
				buttonX,
				buttonY,
				16,
				16,
				minU,
				maxU,
				(vIndex * 16) / 256f,
				(vIndex * 16 + 16) / 256f,
				GL11.GL_NEAREST
			);
		}
		if (desiredHeightSwitch != -1 && Mouse.getEventButton() == -1 && !Mouse.getEventButtonState()) {
			int delta = Math.abs(desiredHeightMX - mouseX) + Math.abs(desiredHeightMY - mouseY);
			if (delta > 3) {
				NotEnoughUpdates.INSTANCE.config.storageGUI.storageHeight = desiredHeightSwitch;
				desiredHeightSwitch = -1;
			}
		}
		if (Mouse.getEventButtonState() && mouseX >= guiLeft + 388 && mouseX < guiLeft + 388 + 90 &&
			mouseY >= guiTop + storageViewSize + 35 && mouseY < guiTop + storageViewSize + 35 + 36) {
			int xN = mouseX - (guiLeft + 388);
			int yN = mouseY - (guiTop + storageViewSize + 35);

			int xIndex = xN / 18;
			int yIndex = yN / 18;

			int buttonIndex = xIndex + 5 * yIndex;

			switch (buttonIndex) {
				case 0:
					NotEnoughUpdates.INSTANCE.config.storageGUI.enableStorageGUI3 = false;
					ChatComponentText storageMessage = new ChatComponentText(
						EnumChatFormatting.YELLOW + "[NEU] " + EnumChatFormatting.YELLOW +
							"You just disabled the custom storage gui, did you mean to do that? If not click this message to turn it back on.");
					storageMessage.setChatStyle(Utils.createClickStyle(ClickEvent.Action.RUN_COMMAND, "/neuenablestorage"));
					storageMessage.setChatStyle(storageMessage.getChatStyle().setChatHoverEvent(
						new HoverEvent(HoverEvent.Action.SHOW_TEXT,
							new ChatComponentText(EnumChatFormatting.YELLOW + "Click to enable the custom storage gui."))));
					ChatComponentText storageChatMessage = new ChatComponentText("");
					storageChatMessage.appendSibling(storageMessage);
					Minecraft.getMinecraft().thePlayer.addChatMessage(storageChatMessage);
					break;
				case 1:
					int size =
						desiredHeightSwitch != -1 ? desiredHeightSwitch : NotEnoughUpdates.INSTANCE.config.storageGUI.storageHeight;
					int sizeIndex = Math.round((size - 104) / 54f);
					if (Mouse.getEventButton() == 0) {
						sizeIndex--;
					} else {
						sizeIndex++;
					}
					size = sizeIndex * 54 + 104;
					if (size < 104) size = 312;
					if (size > 320) size = 104;
					desiredHeightMX = mouseX;
					desiredHeightMY = mouseY;
					desiredHeightSwitch = size;
					break;
				case 2:
					int displayStyle = NotEnoughUpdates.INSTANCE.config.storageGUI.displayStyle;
					if (Mouse.getEventButton() == 0) {
						displayStyle++;
					} else {
						displayStyle--;
					}
					if (displayStyle < 0) displayStyle = STORAGE_TEXTURES.length - 1;
					if (displayStyle >= STORAGE_TEXTURES.length) displayStyle = 0;

					NotEnoughUpdates.INSTANCE.config.storageGUI.displayStyle = displayStyle;
					break;
				case 3:
					NotEnoughUpdates.INSTANCE.config.storageGUI.backpackPreview =
						!NotEnoughUpdates.INSTANCE.config.storageGUI.backpackPreview;
					break;
				case 4:
					NotEnoughUpdates.INSTANCE.config.storageGUI.enderchestPreview =
						!NotEnoughUpdates.INSTANCE.config.storageGUI.enderchestPreview;
					break;
				case 5:
					NotEnoughUpdates.INSTANCE.config.storageGUI.masonryMode =
						!NotEnoughUpdates.INSTANCE.config.storageGUI.masonryMode;
					break;
				case 6:
					int fancyPanes = NotEnoughUpdates.INSTANCE.config.storageGUI.fancyPanes;
					if (Mouse.getEventButton() == 0) {
						fancyPanes++;
					} else {
						fancyPanes--;
					}
					if (fancyPanes < 0) fancyPanes = 2;
					if (fancyPanes >= 3) fancyPanes = 0;

					NotEnoughUpdates.INSTANCE.config.storageGUI.fancyPanes = fancyPanes;
					break;
				case 7:
					NotEnoughUpdates.INSTANCE.config.storageGUI.searchBarAutofocus =
						!NotEnoughUpdates.INSTANCE.config.storageGUI.searchBarAutofocus;
					break;
				case 8:
					NotEnoughUpdates.INSTANCE.config.storageGUI.showEnchantGlint =
						!NotEnoughUpdates.INSTANCE.config.storageGUI.showEnchantGlint;
					break;
				case 9:
					NotEnoughUpdates.INSTANCE.config.storageGUI.disableCIT =
						!NotEnoughUpdates.INSTANCE.config.storageGUI.disableCIT;
					break;
			}
			dirty = true;
		}

		if (mouseX >= guiLeft + 10 && mouseX <= guiLeft + 171 &&
			mouseY >= guiTop + storageViewSize + 23 && mouseY <= guiTop + storageViewSize + 91) {
			if (StorageManager.getInstance().onStorageMenu) {
				return false;
			} else if (Mouse.getEventButtonState() && Mouse.getEventButton() == 0) {
				for (int i = 0; i < 9; i++) {
					int storageId = i;
					int displayId = StorageManager.getInstance().getDisplayIdForStorageIdRender(i);

					StorageManager.StoragePage page = StorageManager.getInstance().getPage(storageId, false);
					if (page != null) {
						int itemX = 10 + (i % 9) * 18;
						int itemY = storageViewSize + 24 + (i / 9) * 18;

						if (mouseX >= guiLeft + itemX && mouseX < guiLeft + itemX + 18 &&
							mouseY >= guiTop + itemY && mouseY < guiTop + itemY + 18) {
							StorageManager.getInstance().sendToPage(storageId);
							scrollToStorage(displayId, true);
							return true;
						}
					}
				}
				for (int i = 0; i < 18; i++) {
					int storageId = i + StorageManager.MAX_ENDER_CHEST_PAGES;
					int displayId = StorageManager.getInstance().getDisplayIdForStorageIdRender(i);

					StorageManager.StoragePage page = StorageManager.getInstance().getPage(storageId, false);
					if (page != null) {
						int itemX = 10 + (i % 9) * 18;
						int itemY = storageViewSize + 56 + (i / 9) * 18;

						if (mouseX >= guiLeft + itemX && mouseX < guiLeft + itemX + 18 &&
							mouseY >= guiTop + itemY && mouseY < guiTop + itemY + 18) {
							StorageManager.getInstance().sendToPage(storageId);
							scrollToStorage(displayId, true);
							return true;
						}
					}
				}
			}
		}

		return true;
	}

	public void overrideIsMouseOverSlot(Slot slot, int mouseX, int mouseY, CallbackInfoReturnable<Boolean> cir) {
		if (StorageManager.getInstance().shouldRenderStorageOverlayFast()) {
			boolean playerInv = slot.inventory == Minecraft.getMinecraft().thePlayer.inventory;

			int slotId = slot.getSlotIndex();
			int storageViewSize = getStorageViewSize();

			if (playerInv) {
				if (slotId < 9) {
					if (mouseY >= guiTop + storageViewSize + 76 && mouseY <= guiTop + storageViewSize + 92) {
						int xN = mouseX - (guiLeft + 181);

						int xClicked = xN / 18;

						if (xClicked == slotId) {
							cir.setReturnValue(true);
							return;
						}
					}
				} else {
					int xN = mouseX - (guiLeft + 181);
					int yN = mouseY - (guiTop + storageViewSize + 18);

					int xClicked = xN / 18;
					int yClicked = yN / 18;

					if (xClicked >= 0 && xClicked <= 8 &&
						yClicked >= 0 && yClicked <= 2) {
						if (xClicked + yClicked * 9 + 9 == slotId) {
							cir.setReturnValue(true);
							return;
						}
					}
				}
			} else {
				if (StorageManager.getInstance().onStorageMenu) {
					if (slotId >= 9 && slotId < 18) {
						if (mouseY >= guiTop + storageViewSize + 24 && mouseY < guiTop + storageViewSize + 24 + 18) {
							int xN = mouseX - (guiLeft + 10);

							int xClicked = xN / 18;

							if (xClicked == slotId % 9) {
								cir.setReturnValue(true);
								return;
							}
						}
					} else if (slotId >= 27 && slotId < 45) {
						int xN = mouseX - (guiLeft + 10);
						int yN = mouseY - (guiTop + storageViewSize + 56);

						int xClicked = xN / 18;
						int yClicked = yN / 18;

						if (xClicked == slotId % 9 &&
							yClicked >= 0 && yClicked == slotId / 9 - 3) {
							cir.setReturnValue(true);
							return;
						}
					}
				} else {
					int currentPage = StorageManager.getInstance().getCurrentPageId();
					int displayId = StorageManager.getInstance().getDisplayIdForStorageIdRender(currentPage);
					if (displayId >= 0) {
						IntPair pageCoords = getPageCoords(displayId);

						int xN = mouseX - (guiLeft + pageCoords.x);
						int yN = mouseY - (guiTop + pageCoords.y);

						int xClicked = xN / 18;
						int yClicked = yN / 18;

						if (xClicked >= 0 && xClicked <= 8 &&
							yClicked >= 0 && yClicked <= 5) {
							if (xClicked + yClicked * 9 + 9 == slotId) {
								if (NotEnoughUpdates.INSTANCE.config.storageGUI.fancyPanes == 1 && slot.getHasStack() &&
									getPaneType(slot.getStack(), -1, null) > 0) {
									cir.setReturnValue(false);
									return;
								}
								cir.setReturnValue(true);
								return;
							}
						}
					}
				}
			}
			cir.setReturnValue(false);
		}
	}

	public void clearSearch() {
		searchBar.setFocus(false);
		searchBar.setText("");
		StorageManager.getInstance().searchDisplay(searchBar.getText());
	}

	@Override
	public boolean keyboardInput() {
		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		int width = scaledResolution.getScaledWidth();
		int height = scaledResolution.getScaledHeight();
		int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
		int mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;

		if (!(Minecraft.getMinecraft().currentScreen instanceof GuiContainer)) return true;
		GuiContainer container = (GuiContainer) Minecraft.getMinecraft().currentScreen;

		if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
			clearSearch();
			return false;
		}
		if (Keyboard.getEventKey() == Minecraft.getMinecraft().gameSettings.keyBindScreenshot.getKeyCode()) {
			return false;
		}
		if (Keyboard.getEventKey() == Minecraft.getMinecraft().gameSettings.keyBindFullscreen.getKeyCode()) {
			return false;
		}

		if (!searchBar.getFocus() && !renameStorageField.getFocus() &&
				(Keyboard.getEventKey() == manager.keybindViewRecipe.getKeyCode() ||
				Keyboard.getEventKey() == manager.keybindViewUsages.getKeyCode())) {
			for (Slot slot : container.inventorySlots.inventorySlots) {
				if (slot != null && ((AccessorGuiContainer) container).doIsMouseOverSlot(slot, mouseX, mouseY)) {
					String internalName = manager.createItemResolutionQuery().withItemStack(slot.getStack()).resolveInternalName();
					JsonObject item = manager.getItemInformation().get(internalName);
					if (Keyboard.getEventKey() == manager.keybindViewRecipe.getKeyCode()) manager.showRecipe(item);
					if (Keyboard.getEventKey() == manager.keybindViewUsages.getKeyCode()) manager.displayGuiItemUsages(internalName);
				}
			}
		}

		if (Keyboard.getEventKeyState()) {
			if (NotEnoughUpdates.INSTANCE.config.slotLocking.enableSlotLocking &&
				KeybindHelper.isKeyPressed(NotEnoughUpdates.INSTANCE.config.slotLocking.slotLockKey) && !searchBar.getFocus()) {

				for (Slot slot : container.inventorySlots.inventorySlots) {
					if (slot != null &&
						slot.inventory == Minecraft.getMinecraft().thePlayer.inventory &&
						((AccessorGuiContainer) container).doIsMouseOverSlot(slot, mouseX, mouseY)) {
						SlotLocking.getInstance().toggleLock(slot.getSlotIndex());
						return true;
					}
				}
			}

			if (editingNameId >= 0) {
				if (Keyboard.getEventKey() == Keyboard.KEY_RETURN) {
					editingNameId = -1;
					return true;
				}

				String prevText = renameStorageField.getText();
				renameStorageField.setFocus(true);
				searchBar.setFocus(false);
				renameStorageField.keyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());
				if (!prevText.equals(renameStorageField.getText())) {
					StorageManager.StoragePage page = StorageManager.getInstance().getPage(editingNameId, false);
					if (page != null) {
						page.customTitle = renameStorageField.getText();
					}
				}
			} else if (searchBar.getFocus() ||
				(allowTypingInSearchBar && NotEnoughUpdates.INSTANCE.config.storageGUI.searchBarAutofocus)) {
				String prevText = searchBar.getText();
				searchBar.setFocus(true);
				renameStorageField.setFocus(false);
				searchBar.keyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());
				if (!prevText.equals(searchBar.getText())) {
					StorageManager.getInstance().searchDisplay(searchBar.getText());
					dirty = true;
				}
				if (NotEnoughUpdates.INSTANCE.config.storageGUI.searchBarAutofocus &&
					searchBar.getText().isEmpty()) {
					searchBar.setFocus(false);
				}
			} else return Keyboard.getEventKey() != Minecraft.getMinecraft().gameSettings.keyBindInventory.getKeyCode();

		}

		return true;
	}

	private void renderEnchOverlay(Set<Vector2f> locations) {
		float f = (float) (Minecraft.getSystemTime() % 3000L) / 3000.0F / 8.0F;
		float f1 = (float) (Minecraft.getSystemTime() % 4873L) / 4873.0F / 8.0F;
		if (NotEnoughUpdates.INSTANCE.config.storageGUI.showEnchantGlint) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(RES_ITEM_GLINT);
		}

		GL11.glPushMatrix();
		for (Vector2f loc : locations) {
			GlStateManager.pushMatrix();
			GlStateManager.enableRescaleNormal();
			GlStateManager.enableAlpha();
			GlStateManager.alphaFunc(516, 0.1F);
			GlStateManager.enableBlend();

			GlStateManager.disableLighting();

			GlStateManager.translate(loc.x, loc.y, 0);

			GlStateManager.depthMask(false);
			GlStateManager.depthFunc(GL11.GL_EQUAL);
			GlStateManager.blendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
			GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
			GlStateManager.matrixMode(5890);
			GlStateManager.pushMatrix();
			GlStateManager.scale(8.0F, 8.0F, 8.0F);
			GlStateManager.translate(f, 0.0F, 0.0F);
			GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);

			GlStateManager.color(0x80 / 255f, 0x40 / 255f, 0xCC / 255f, 1);
			Utils.drawTexturedRectNoBlend(0, 0, 16, 16, 0, 1 / 16f, 0, 1 / 16f, GL11.GL_NEAREST);

			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			GlStateManager.scale(8.0F, 8.0F, 8.0F);
			GlStateManager.translate(-f1, 0.0F, 0.0F);
			GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);

			GlStateManager.color(0x80 / 255f, 0x40 / 255f, 0xCC / 255f, 1);
			Utils.drawTexturedRectNoBlend(0, 0, 16, 16, 0, 1 / 16f, 0, 1 / 16f, GL11.GL_NEAREST);

			GlStateManager.popMatrix();
			GlStateManager.matrixMode(5888);
			GlStateManager.blendFunc(770, 771);
			GlStateManager.depthFunc(515);
			GlStateManager.depthMask(true);

			GlStateManager.popMatrix();
		}
		GlStateManager.disableRescaleNormal();
		GL11.glPopMatrix();

		GlStateManager.bindTexture(0);
	}

	public void fastRenderCheck() {
		if (!OpenGlHelper.isFramebufferEnabled() && NotEnoughUpdates.INSTANCE.config.notifications.doFastRenderNotif &&
			NotEnoughUpdates.INSTANCE.config.storageGUI.enableStorageGUI3) {
			this.fastRender = true;
			NotificationHandler.displayNotification(Lists.newArrayList(
				"\u00a74Warning",
				"\u00a77Due to the way fast render and antialiasing work, they're not compatible with NEU.",
				"\u00a77Please disable fast render and antialiasing in your options under",
				"\u00a77ESC > Options > Video Settings > Performance > \u00A7cFast Render",
				"\u00a77ESC > Options > Video Settings > Quality > \u00A7cAntialiasing",
				"\u00a77This can't be fixed.",
				"\u00a77",
				"\u00a77Press X on your keyboard to close this notification"
			), true, true);
			return;
		}

		this.fastRender = false;
	}

	private static class IntPair {
		int x;
		int y;

		public IntPair(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
}
