/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
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

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.core.config.KeybindHelper;
import io.github.moulberry.notenoughupdates.miscfeatures.StorageManager;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

@NEUAutoSubscribe
public class InventoryStorageSelector {
	private static final InventoryStorageSelector INSTANCE = new InventoryStorageSelector();

	private static final ResourceLocation ICONS = new ResourceLocation("notenoughupdates:storage_gui/hotbar_icons.png");
	private static final ResourceLocation STORAGE_PANE_CTM_TEXTURE = new ResourceLocation(
		"notenoughupdates:storage_gui/storage_gui_pane_ctm.png");

	public boolean isOverridingSlot = false;

	public static InventoryStorageSelector getInstance() {
		return INSTANCE;
	}

	public boolean isSlotSelected() {
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard() ||
			!NotEnoughUpdates.INSTANCE.config.storageGUI.showInvBackpack) {
			isOverridingSlot = false;
			return false;
		}
		if (Minecraft.getMinecraft().currentScreen != null) {
			return false;
		}
		if (Minecraft.getMinecraft().thePlayer == null) {
			isOverridingSlot = false;
			return false;
		}
		if (Minecraft.getMinecraft().thePlayer.inventory.currentItem != 0) {
			isOverridingSlot = false;
			return false;
		}
		return isOverridingSlot;
	}

	@SubscribeEvent
	public void onMousePress(MouseEvent event) {
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard() ||
			!NotEnoughUpdates.INSTANCE.config.storageGUI.showInvBackpack) {
			return;
		}

		if (Minecraft.getMinecraft().currentScreen == null && isSlotSelected()) {
			int useKeycode = Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode() + 100;
			int attackKeycode = Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode() + 100;

			if (Mouse.getEventButton() == useKeycode || Mouse.getEventButton() == attackKeycode) {
				if (Mouse.getEventButtonState() &&
					Mouse.getEventButton() != NotEnoughUpdates.INSTANCE.config.storageGUI.backpackScrollKey + 100) {
					sendToPage(NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex);
				}

				event.setCanceled(true);
			}
		}
	}

	private void sendToPage(int displayId) {
		if (!StorageManager.getInstance().storageConfig.displayToStorageIdMap.containsKey(displayId)) {
			return;
		}
		if (getPage(NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex) == null) {
			NotEnoughUpdates.INSTANCE.sendChatMessage("/storage");
		} else {
			int index =
				StorageManager.getInstance().storageConfig.displayToStorageIdMap.get(NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex);
			StorageManager.getInstance().sendToPage(index);
		}
	}

	@SubscribeEvent
	public void onKeyPress(InputEvent.KeyInputEvent event) {
		if (Minecraft.getMinecraft().gameSettings.keyBindsHotbar[0].isKeyDown()) {
			isOverridingSlot = false;
		}
		if (Minecraft.getMinecraft().currentScreen != null) {
			return;
		}

		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard() ||
			!NotEnoughUpdates.INSTANCE.config.storageGUI.showInvBackpack) {
			return;
		}

		if (KeybindHelper.isKeyPressed(NotEnoughUpdates.INSTANCE.config.storageGUI.backpackHotkey)) {
			Minecraft.getMinecraft().thePlayer.inventory.currentItem = 0;
			isOverridingSlot = true;
		}

		if (NotEnoughUpdates.INSTANCE.config.storageGUI.arrowKeyBackpacks) {
			if (KeybindHelper.isKeyPressed(NotEnoughUpdates.INSTANCE.config.storageGUI.arrowLeftKey)) {
				NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex--;

				int max = StorageManager.getInstance().storageConfig.displayToStorageIdMap.size() - 1;
				if (NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex > max)
					NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex = max;
				if (NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex < 0)
					NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex = 0;
			} else if (KeybindHelper.isKeyPressed(NotEnoughUpdates.INSTANCE.config.storageGUI.arrowRightKey)) {
				NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex++;

				int max = StorageManager.getInstance().storageConfig.displayToStorageIdMap.size() - 1;
				if (NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex > max)
					NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex = max;
				if (NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex < 0)
					NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex = 0;
			} else if (KeybindHelper.isKeyPressed(NotEnoughUpdates.INSTANCE.config.storageGUI.arrowDownKey)) {
				sendToPage(NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex);
			}
		}

		if (isSlotSelected()) {
			KeyBinding attack = Minecraft.getMinecraft().gameSettings.keyBindAttack;
			KeyBinding use = Minecraft.getMinecraft().gameSettings.keyBindUseItem;

			if (attack.isPressed() || attack.isKeyDown()) {
				if (attack.getKeyCode() != NotEnoughUpdates.INSTANCE.config.storageGUI.backpackScrollKey) {
					sendToPage(NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex);
				}

				KeyBinding.setKeyBindState(attack.getKeyCode(), false);
				while (attack.isPressed()) {
				}
			}

			if (use.isPressed() || use.isKeyDown()) {
				if (attack.getKeyCode() != NotEnoughUpdates.INSTANCE.config.storageGUI.backpackScrollKey) {
					sendToPage(NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex);
				}

				KeyBinding.setKeyBindState(use.getKeyCode(), false);
				while (use.isPressed()) {
				}
			}
		}
	}

	public int onScroll(int direction, int resultantSlot) {
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard() ||
			!NotEnoughUpdates.INSTANCE.config.storageGUI.showInvBackpack) {
			return resultantSlot;
		}
		if (Minecraft.getMinecraft().currentScreen != null) {
			return resultantSlot;
		}

		int keyCode = NotEnoughUpdates.INSTANCE.config.storageGUI.backpackScrollKey;
		if (isOverridingSlot && KeybindHelper.isKeyDown(keyCode)) {
			NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex -= direction;
			int max = StorageManager.getInstance().storageConfig.displayToStorageIdMap.size() - 1;

			if (NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex > max)
				NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex = max;
			if (NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex < 0)
				NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex = 0;
			return 0;
		}

		boolean allowScroll = NotEnoughUpdates.INSTANCE.config.storageGUI.scrollToBackpack2 == 0 ?
			KeybindHelper.isKeyDown(NotEnoughUpdates.INSTANCE.config.storageGUI.backpackScrollKey) :
			NotEnoughUpdates.INSTANCE.config.storageGUI.scrollToBackpack2 == 1;

		if (allowScroll && resultantSlot == 0 && direction == -1 && !isOverridingSlot) {
			isOverridingSlot = true;
			Minecraft.getMinecraft().getItemRenderer().resetEquippedProgress();
			return 0;
		} else if (resultantSlot == 1 && direction == -1 && isOverridingSlot) {
			isOverridingSlot = false;
			Minecraft.getMinecraft().getItemRenderer().resetEquippedProgress();
			return 0;
		} else if (allowScroll && resultantSlot == 8 && direction == 1 && !isOverridingSlot) {
			isOverridingSlot = true;
			Minecraft.getMinecraft().getItemRenderer().resetEquippedProgress();
			return 0;
		}
		return resultantSlot;
	}

	private StorageManager.StoragePage getPage(int selectedIndex) {
		if (!StorageManager.getInstance().storageConfig.displayToStorageIdMap.containsKey(selectedIndex)) {
			return null;
		}
		int index = StorageManager.getInstance().storageConfig.displayToStorageIdMap.get(selectedIndex);
		return StorageManager.getInstance().getPage(index, false);
	}

	public ItemStack getNamedHeldItemOverride() {
		StorageManager.StoragePage page = getPage(NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex);
		if (page != null && page.backpackDisplayStack != null) {
			return page.backpackDisplayStack;
		}
		return new ItemStack(Item.getItemFromBlock(Blocks.chest));
	}

	public ItemStack getHeldItemOverride() {
		return getHeldItemOverride(NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex);
	}

	public ItemStack getHeldItemOverride(int selectedIndex) {
		StorageManager.StoragePage page = getPage(selectedIndex);
		if (page != null) {
			ItemStack stack = page.backpackDisplayStack;
			if (stack == null || stack.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane)) {
				return new ItemStack(Item.getItemFromBlock(Blocks.ender_chest));
			}
			return stack;
		}
		return new ItemStack(Item.getItemFromBlock(Blocks.chest));
	}

	public void render(ScaledResolution scaledResolution, float partialTicks) {
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard() ||
			!NotEnoughUpdates.INSTANCE.config.storageGUI.showInvBackpack) {
			return;
		}
		if (Minecraft.getMinecraft().currentScreen != null) {
			return;
		}

		int max = StorageManager.getInstance().storageConfig.displayToStorageIdMap.size() - 1;
		if (NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex > max)
			NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex = max;
		if (NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex < 0)
			NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex = 0;

		int width = scaledResolution.getScaledWidth();
		int height = scaledResolution.getScaledHeight();
		int centerX = width / 2;

		int offset = 91 + 10 + 12;

		if (NotEnoughUpdates.INSTANCE.config.storageGUI.backpackHotbarSide == 1) {
			offset *= -1;
		}

		ItemStack held = getHeldItemOverride();
		int left = centerX - offset - 12;
		int top = scaledResolution.getScaledHeight() - 22;

		if (NotEnoughUpdates.INSTANCE.config.storageGUI.showInvBackpackPreview && isSlotSelected()) {
			StorageManager.StoragePage page = getPage(NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex);

			if (page != null && page.rows > 0) {
				int rows = page.rows;

				ResourceLocation storagePreviewTexture =
					StorageOverlay.STORAGE_PREVIEW_TEXTURES[NotEnoughUpdates.INSTANCE.config.storageGUI.displayStyle];

				int startX = centerX - 172 / 2;
				int startY = height - 80 - (10 + 18 * rows);

				GlStateManager.translate(0, 0, 100);
				GL11.glDepthMask(false);

				Minecraft.getMinecraft().getTextureManager().bindTexture(storagePreviewTexture);
				GlStateManager.color(1, 1, 1,
					NotEnoughUpdates.INSTANCE.config.storageGUI.backpackOpacity / 100f
				);
				Utils.drawTexturedRect(startX, startY, 176, 7, 0, 1, 0, 7 / 32f, GL11.GL_NEAREST);
				for (int i = 0; i < rows; i++) {
					Utils.drawTexturedRect(startX, startY + 7 + 18 * i, 176, 18, 0, 1, 7 / 32f, 25 / 32f, GL11.GL_NEAREST);
				}
				Utils.drawTexturedRect(startX, startY + 7 + 18 * rows, 176, 7, 0, 1, 25 / 32f, 1, GL11.GL_NEAREST);

				GL11.glDepthMask(true);

				int[] isPaneCache = new int[rows * 9];
				int[] ctmIndexCache = new int[rows * 9];

				for (int i = 0; i < rows * 9; i++) {
					ItemStack stack = page.items[i];
					if (stack != null) {
						int itemX = startX + 8 + 18 * (i % 9);
						int itemY = startY + 8 + 18 * (i / 9);

						int paneType = StorageOverlay.getPaneType(stack, i, isPaneCache);
						if (paneType > 0) {
							GlStateManager.disableAlpha();
							Gui.drawRect(itemX - 1, itemY - 1, itemX + 17, itemY + 17, 0x01000000);
							GlStateManager.enableAlpha();

							int ctmIndex = StorageOverlay.getCTMIndex(page, i, isPaneCache, ctmIndexCache);
							int startCTMX = (ctmIndex % 12) * 19;
							int startCTMY = (ctmIndex / 12) * 19;

							ctmIndexCache[i] = ctmIndex;

							if (paneType != 17) {
								int rgb = StorageOverlay.getRGBFromPane(paneType - 1);
								{
									int a = (rgb >> 24) & 0xFF;
									int r = (rgb >> 16) & 0xFF;
									int g = (rgb >> 8) & 0xFF;
									int b = rgb & 0xFF;
									Minecraft.getMinecraft().getTextureManager().bindTexture(STORAGE_PANE_CTM_TEXTURE);
									GlStateManager.color(r / 255f, g / 255f, b / 255f, a / 255f);
									Utils.drawTexturedRect(itemX - 1, itemY - 1, 18, 18,
										startCTMX / 227f, (startCTMX + 18) / 227f, startCTMY / 75f, (startCTMY + 18) / 75f, GL11.GL_NEAREST
									);
								}

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

							page.shouldDarkenIfNotSelected[i] = false;
							continue;
						} else {
							int upIndex = i - 9;
							int leftIndex = i % 9 > 0 ? i - 1 : -1;
							int rightIndex = i % 9 < 8 ? i + 1 : -1;
							int downIndex = i + 9;

							int[] indexArr = {rightIndex, downIndex, leftIndex, upIndex};

							for (int j = 0; j < 4; j++) {
								int index = indexArr[j];
								int type = index >= 0 && index < isPaneCache.length ? StorageOverlay.getPaneType(
									page.items[index],
									index,
									isPaneCache
								) : -1;
								if (type > 0) {
									int ctmIndex = StorageOverlay.getCTMIndex(page, index, isPaneCache, ctmIndexCache);
									if (ctmIndex < 0) continue;

									boolean renderConnection;
									boolean horizontal = ctmIndex == 1 || ctmIndex == 2 || ctmIndex == 3;
									boolean vertical = ctmIndex == 12 || ctmIndex == 24 || ctmIndex == 36;
									if ((i % 9 == 0 && index % 9 == 0) || (i % 9 == 8 && index % 9 == 8)) {
										renderConnection = horizontal || vertical;
									} else if (index == leftIndex || index == rightIndex) {
										renderConnection = horizontal;
									} else {
										renderConnection = vertical;
									}

									if (renderConnection) {
										Minecraft.getMinecraft().getTextureManager().bindTexture(STORAGE_PANE_CTM_TEXTURE);
										int rgb = StorageOverlay.getRGBFromPane(type - 1);
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

										Utils.drawTexturedRect(0, -9, 8, 18,
											!horzFlip ? 209 / 227f : 219 / 227f, horzFlip ? 227 / 227f : 217 / 227f,
											!vertFlip ? 57 / 75f : 75f / 75f, vertFlip ? 57 / 75f : 75f / 75f, GL11.GL_NEAREST
										);
										Utils.drawTexturedRect(8, -9, 10, 18,
											!horzFlip ? 217 / 227f : 209 / 227f, horzFlip ? 219 / 227f : 227 / 227f,
											!vertFlip ? 57 / 75f : 75f / 75f, vertFlip ? 57 / 75f : 75f / 75f, GL11.GL_NEAREST
										);

										GlStateManager.popMatrix();
									}
								}
							}
						}

						GlStateManager.translate(0, 0, 20);
						Utils.drawItemStack(stack, itemX, itemY);
						GlStateManager.translate(0, 0, -20);
					}
				}

				String pageTitle;
				if (page.customTitle != null && !page.customTitle.isEmpty()) {
					pageTitle = page.customTitle;
				} else {
					pageTitle = getNamedHeldItemOverride().getDisplayName();
				}

				Utils.drawItemStack(held, centerX - 8, startY - 8);

				GlStateManager.translate(0, 0, 100);
				Utils.drawStringCentered(pageTitle, centerX, height - 76, true, 0xffff0000);
				int keyCode = NotEnoughUpdates.INSTANCE.config.storageGUI.backpackScrollKey;
				if (KeybindHelper.isKeyValid(keyCode) && !KeybindHelper.isKeyDown(keyCode)) {
					String keyName = KeybindHelper.getKeyName(keyCode);
					Utils.drawStringCentered("[" + keyName + "] Scroll Backpacks", centerX, startY - 10, true, 0xff32CD32);
				}
				GlStateManager.translate(0, 0, -200);

			} else if (page == null) {
				Utils.drawStringCentered("Run /storage to enable this feature!", centerX, height - 80, true, 0xffff0000);
			} else {
				Utils.drawStringCentered("Right-click to load items", centerX, height - 80, true, 0xffff0000);
			}
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(ICONS);
		GlStateManager.color(1, 1, 1, 1);
		Utils.drawTexturedRect(left + 1, top,
			22, 22, 0, 22 / 64f, 0, 22 / 64f, GL11.GL_NEAREST
		);
		if (isSlotSelected()) {
			Utils.drawTexturedRect(left, top - 1,
				24, 22, 0, 24 / 64f, 22 / 64f, 44 / 64f, GL11.GL_NEAREST
			);
		}

		int index = 1;
		if (StorageManager.getInstance().storageConfig.displayToStorageIdMap.containsKey(NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex)) {
			int displayIndex =
				StorageManager.getInstance().storageConfig.displayToStorageIdMap.get(NotEnoughUpdates.INSTANCE.config.storageGUI.selectedIndex);
			if (displayIndex < 9) {
				index = displayIndex + 1;
			} else {
				index = displayIndex - 8;
			}
		}

		Utils.drawItemStackWithText(held, left + 4, top + 3, "" + index);

		GlStateManager.enableBlend();
	}
}
