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

package io.github.moulberry.notenoughupdates.miscfeatures;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.events.SlotClickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@NEUAutoSubscribe
public class BetterContainers {
	private static final ResourceLocation TOGGLE_OFF = new ResourceLocation("notenoughupdates:dynamic_54/toggle_off.png");
	private static final ResourceLocation TOGGLE_ON = new ResourceLocation("notenoughupdates:dynamic_54/toggle_on.png");

	private static final ResourceLocation DYNAMIC_54_BASE = new ResourceLocation(
		"notenoughupdates:dynamic_54/style1/dynamic_54.png");
	private static final ResourceLocation DYNAMIC_54_SLOT = new ResourceLocation(
		"notenoughupdates:dynamic_54/style1/dynamic_54_slot_ctm.png");
	private static final ResourceLocation DYNAMIC_54_BUTTON = new ResourceLocation(
		"notenoughupdates:dynamic_54/style1/dynamic_54_button_ctm.png");
	private static final ResourceLocation rl = new ResourceLocation("notenoughupdates:dynamic_chest_inventory.png");
	private static boolean loaded = false;
	private static DynamicTexture texture = null;
	private static int textColour = 4210752;

	private static int lastClickedSlot = 0;
	private static int clickedSlot = 0;
	private static long clickedSlotMillis = 0;
	public static long lastRenderMillis = 0;

	private static int lastInvHashcode = 0;
	private static final int lastHashcodeCheck = 0;

	public static HashMap<Integer, ItemStack> itemCache = new HashMap<>();

	public static int profileViewerStackIndex = -1;

	public static void clickSlot(int slot) {
		clickedSlotMillis = System.currentTimeMillis();
		clickedSlot = slot;
	}

	public static int getClickedSlot() {
		if (System.currentTimeMillis() - clickedSlotMillis < 500) {
			return clickedSlot;
		}
		return -1;
	}

	public static void bindHook(TextureManager textureManager, ResourceLocation location) {
		long currentMillis = System.currentTimeMillis();

		if (isChestOpen()) {
			int invHashcode = lastInvHashcode;

			if (currentMillis - lastHashcodeCheck > 50) {
				Container container = ((GuiChest) Minecraft.getMinecraft().currentScreen).inventorySlots;
				invHashcode = container.getInventory().hashCode();
			}

			if ((texture != null && lastClickedSlot != getClickedSlot()) || !loaded || lastInvHashcode != invHashcode) {
				lastInvHashcode = invHashcode;
				lastClickedSlot = getClickedSlot();
				generateTex(location);
			}
			if (texture != null && loaded) {
				lastRenderMillis = currentMillis;

				GlStateManager.color(1, 1, 1, 1);
				textureManager.loadTexture(rl, texture);
				textureManager.bindTexture(rl);
				return;
			}
		} else if (currentMillis - lastRenderMillis < 200 && texture != null) {
			GlStateManager.color(1, 1, 1, 1);
			textureManager.loadTexture(rl, texture);
			textureManager.bindTexture(rl);
			return;
		}
		GlStateManager.enableBlend();
		textureManager.bindTexture(location);
	}

	public static boolean getUsingCache() {
		return false;
	}

	public static boolean isBlacklistedInventory() {
		if (!isChestOpen()) return false;

		GuiChest eventGui = (GuiChest) Minecraft.getMinecraft().currentScreen;
		ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
		String containerName = cc.getLowerChestInventory().getDisplayName().getUnformattedText();
		return containerName.toLowerCase().trim().startsWith("navigate the maze");
	}

	public static boolean isOverriding() {
		return isChestOpen() && ((loaded && texture != null) || System.currentTimeMillis() - lastRenderMillis < 200) &&
			!isBlacklistedInventory();
	}

	public static boolean isBlankStack(int index, ItemStack stack) {
		if (index != -1 && index == profileViewerStackIndex) {
			return false;
		}

		return stack != null && stack.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane) &&
			stack.getItemDamage() == 15 &&
			stack.getDisplayName() != null && stack.getDisplayName().trim().isEmpty();
	}

	public static boolean shouldRenderStack(int index, ItemStack stack) {
		return !isBlankStack(index, stack) && !isToggleOff(stack) && !isToggleOn(stack);
	}

	public static boolean isButtonStack(int index, ItemStack stack) {
		if (index == profileViewerStackIndex) {
			return true;
		}

		return stack != null && stack.getItem() != Item.getItemFromBlock(Blocks.stained_glass_pane)
			&& NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(stack) == null && !isToggleOn(stack) && !isToggleOff(
			stack);
	}

	public static int getTextColour() {
		return textColour;
	}

	public static boolean isToggleOn(ItemStack stack) {
		if (stack != null && stack.getTagCompound() != null && stack.getTagCompound().hasKey("display", 10) &&
			stack.getTagCompound().getCompoundTag("display").hasKey("Lore", 9)) {
			NBTTagList lore = stack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
			return lore.tagCount() == 1 && lore.getStringTagAt(0).equalsIgnoreCase(
				EnumChatFormatting.GRAY + "click to disable!");
		}
		return false;
	}

	public static boolean isToggleOff(ItemStack stack) {
		if (stack != null && stack.getTagCompound() != null && stack.getTagCompound().hasKey("display", 10) &&
			stack.getTagCompound().getCompoundTag("display").hasKey("Lore", 9)) {
			NBTTagList lore = stack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
			return lore.tagCount() == 1 && lore.getStringTagAt(0).equalsIgnoreCase(
				EnumChatFormatting.GRAY + "click to enable!");
		}
		return false;
	}

	private static void generateTex(ResourceLocation location) {
		if (!hasItem()) return;

		loaded = true;
		Container container = ((GuiChest) Minecraft.getMinecraft().currentScreen).inventorySlots;

		if (hasNullPane() && container instanceof ContainerChest) {
			int backgroundStyle = NotEnoughUpdates.INSTANCE.config.improvedSBMenu.backgroundStyle + 1;
			backgroundStyle = Math.max(1, Math.min(10, backgroundStyle));
			try (
				BufferedReader reader = new BufferedReader(new InputStreamReader(Minecraft
					.getMinecraft()
					.getResourceManager()
					.getResource(
						new ResourceLocation("notenoughupdates:dynamic_54/style" + backgroundStyle + "/dynamic_config.json"))
					.getInputStream(), StandardCharsets.UTF_8))
			) {
				JsonObject json = NotEnoughUpdates.INSTANCE.manager.gson.fromJson(reader, JsonObject.class);
				String textColourS = json.get("text-colour").getAsString();
				textColour = (int) Long.parseLong(textColourS, 16);
			} catch (Exception e) {
				textColour = 4210752;
			}

			try {
				BufferedImage bufferedImageOn = ImageIO.read(Minecraft
					.getMinecraft()
					.getResourceManager()
					.getResource(TOGGLE_ON)
					.getInputStream());
				BufferedImage bufferedImageOff = ImageIO.read(Minecraft
					.getMinecraft()
					.getResourceManager()
					.getResource(TOGGLE_OFF)
					.getInputStream());

				BufferedImage bufferedImageBase = ImageIO.read(Minecraft
					.getMinecraft()
					.getResourceManager()
					.getResource(DYNAMIC_54_BASE)
					.getInputStream());
				try {
					bufferedImageBase = ImageIO.read(Minecraft
						.getMinecraft()
						.getResourceManager()
						.getResource(
							new ResourceLocation("notenoughupdates:dynamic_54/style" + backgroundStyle + "/dynamic_54.png"))
						.getInputStream());
				} catch (Exception ignored) {
				}
				BufferedImage bufferedImageSlot = ImageIO.read(Minecraft
					.getMinecraft()
					.getResourceManager()
					.getResource(DYNAMIC_54_SLOT)
					.getInputStream());
				try {
					int buttonStyle = NotEnoughUpdates.INSTANCE.config.improvedSBMenu.buttonStyle + 1;
					buttonStyle = Math.max(1, Math.min(10, buttonStyle));
					bufferedImageSlot = ImageIO.read(Minecraft
						.getMinecraft()
						.getResourceManager()
						.getResource(
							new ResourceLocation("notenoughupdates:dynamic_54/style" + buttonStyle + "/dynamic_54_slot_ctm.png"))
						.getInputStream());
				} catch (Exception ignored) {
				}
				BufferedImage bufferedImageButton = ImageIO.read(Minecraft
					.getMinecraft()
					.getResourceManager()
					.getResource(DYNAMIC_54_BUTTON)
					.getInputStream());
				try {
					int buttonStyle = NotEnoughUpdates.INSTANCE.config.improvedSBMenu.buttonStyle + 1;
					buttonStyle = Math.max(1, Math.min(10, buttonStyle));
					bufferedImageButton = ImageIO.read(Minecraft
						.getMinecraft()
						.getResourceManager()
						.getResource(
							new ResourceLocation("notenoughupdates:dynamic_54/style" + buttonStyle + "/dynamic_54_button_ctm.png"))
						.getInputStream());
				} catch (Exception ignored) {
				}

				int horzTexMult = bufferedImageBase.getWidth() / 256;
				int vertTexMult = bufferedImageBase.getWidth() / 256;
				BufferedImage bufferedImageNew = new BufferedImage(
					bufferedImageBase.getColorModel(),
					bufferedImageBase.copyData(null),
					bufferedImageBase.isAlphaPremultiplied(),
					null
				);
				IInventory lower = ((ContainerChest) container).getLowerChestInventory();
				int size = lower.getSizeInventory();
				boolean[][] slots = new boolean[9][size / 9];
				boolean[][] buttons = new boolean[9][size / 9];

				boolean ultrasequencer = lower.getDisplayName().getUnformattedText().startsWith("Ultrasequencer") &&
					!lower.getDisplayName().getUnformattedText().contains("Stakes");
				boolean superpairs = lower.getDisplayName().getUnformattedText().startsWith("Superpairs") &&
					!lower.getDisplayName().getUnformattedText().contains("Stakes");
				for (int index = 0; index < size; index++) {
					ItemStack stack = getStackCached(lower, index);
					buttons[index % 9][index / 9] = isButtonStack(index, stack);

					if (ultrasequencer && stack.getItem() == Items.dye) {
						buttons[index % 9][index / 9] = false;
					}

					if (superpairs && index > 9 && index < size - 9) {
						buttons[index % 9][index / 9] = false;
					}

					if (buttons[index % 9][index / 9] && lastClickedSlot == index) {
						//buttons[index%9][index/9] = false;
						//slots[index%9][index/9] = true;
					} else {
						slots[index % 9][index / 9] = !isBlankStack(index, stack) && !buttons[index % 9][index / 9];
					}
				}
				for (int index = 0; index < size; index++) {
					ItemStack stack = getStackCached(lower, index);
					int xi = index % 9;
					int yi = index / 9;
					if (slots[xi][yi] || buttons[xi][yi]) {
						int x = 7 * horzTexMult + xi * 18 * horzTexMult;
						int y = 17 * vertTexMult + yi * 18 * vertTexMult;

						boolean on = isToggleOn(stack);
						boolean off = isToggleOff(stack);

						if (on || off) {
							for (int x2 = 0; x2 < 18; x2++) {
								for (int y2 = 0; y2 < 18; y2++) {
									BufferedImage toggle = on ? bufferedImageOn : bufferedImageOff;
									Color c = new Color(toggle.getRGB(x2, y2), true);
									if (c.getAlpha() < 10) {
										continue;
									}
									bufferedImageNew.setRGB(x + x2, y + y2, c.getRGB());
								}
							}
							continue;
						}

						if (buttons[xi][yi]) {
							boolean up = yi > 0 && buttons[xi][yi - 1];
							boolean right = xi < buttons.length - 1 && buttons[xi + 1][yi];
							boolean down = yi < buttons[xi].length - 1 && buttons[xi][yi + 1];
							boolean left = xi > 0 && buttons[xi - 1][yi];

							boolean upleft = yi > 0 && xi > 0 && buttons[xi - 1][yi - 1];
							boolean upright = yi > 0 && xi < buttons.length - 1 && buttons[xi + 1][yi - 1];
							boolean downright = xi < buttons.length - 1 && yi < buttons[xi + 1].length - 1 && buttons[xi + 1][yi + 1];
							boolean downleft = xi > 0 && yi < buttons[xi - 1].length - 1 && buttons[xi - 1][yi + 1];

							int ctmIndex = getCTMIndex(up, right, down, left, upleft, upright, downright, downleft);
							int[] rgbs = bufferedImageButton.getRGB(
								(ctmIndex % 12) * 19 * horzTexMult,
								(ctmIndex / 12) * 19 * vertTexMult,
								18 * horzTexMult,
								18 * vertTexMult,
								null,
								0,
								18 * vertTexMult
							);
							bufferedImageNew.setRGB(x, y, 18 * horzTexMult, 18 * vertTexMult, rgbs, 0, 18 * vertTexMult);

						} else {
							boolean up = yi > 0 && slots[xi][yi - 1];
							boolean right = xi < slots.length - 1 && slots[xi + 1][yi];
							boolean down = yi < slots[xi].length - 1 && slots[xi][yi + 1];
							boolean left = xi > 0 && slots[xi - 1][yi];

							boolean upleft = yi > 0 && xi > 0 && slots[xi - 1][yi - 1];
							boolean upright = yi > 0 && xi < slots.length - 1 && slots[xi + 1][yi - 1];
							boolean downright = xi < slots.length - 1 && yi < slots[xi + 1].length - 1 && slots[xi + 1][yi + 1];
							boolean downleft = xi > 0 && yi < slots[xi - 1].length - 1 && slots[xi - 1][yi + 1];

							int ctmIndex = getCTMIndex(up, right, down, left, upleft, upright, downright, downleft);
							int[] rgbs = bufferedImageSlot.getRGB(
								(ctmIndex % 12) * 19 * horzTexMult,
								(ctmIndex / 12) * 19 * vertTexMult,
								18 * horzTexMult,
								18 * vertTexMult,
								null,
								0,
								18 * vertTexMult
							);
							bufferedImageNew.setRGB(x, y, 18 * horzTexMult, 18 * vertTexMult, rgbs, 0, 18 * vertTexMult);
						}
					}
				}
				if (texture != null) {
					bufferedImageNew.getRGB(0, 0, bufferedImageNew.getWidth(), bufferedImageNew.getHeight(),
						texture.getTextureData(), 0, bufferedImageNew.getWidth()
					);
					texture.updateDynamicTexture();
				} else {
					texture = new DynamicTexture(bufferedImageNew);
				}
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		texture = null;
	}

	public static void reset() {
		loaded = false;
		clickedSlot = -1;
		clickedSlotMillis = 0;
	}

	private static boolean isChestOpen() {
		return Minecraft.getMinecraft().currentScreen instanceof GuiChest &&
			NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard() &&
			NotEnoughUpdates.INSTANCE.config.improvedSBMenu.enableSbMenus;
	}

	private static boolean hasItem() {
		if (!isChestOpen()) return false;
		Container container = ((GuiChest) Minecraft.getMinecraft().currentScreen).inventorySlots;
		if (container instanceof ContainerChest) {
			IInventory lower = ((ContainerChest) container).getLowerChestInventory();
			int size = lower.getSizeInventory();
			for (int index = 0; index < size; index++) {
				if (getStackCached(lower, index) != null) return true;
			}
		}
		return false;
	}

	private static ItemStack getStackCached(IInventory lower, int index) {
		if (getUsingCache()) {
			return itemCache.get(index);
		} else {
			return lower.getStackInSlot(index);
		}
	}

	private static boolean hasNullPane() {
		if (!isChestOpen()) return false;
		Container container = ((GuiChest) Minecraft.getMinecraft().currentScreen).inventorySlots;
		if (container instanceof ContainerChest) {
			IInventory lower = ((ContainerChest) container).getLowerChestInventory();
			int size = lower.getSizeInventory();
			for (int index = 0; index < size; index++) {
				if (isBlankStack(index, getStackCached(lower, index))) return true;
			}
		}
		return false;
	}

	public static int getCTMIndex(
		boolean up,
		boolean right,
		boolean down,
		boolean left,
		boolean upleft,
		boolean upright,
		boolean downright,
		boolean downleft
	) {
		if (up && right && down && left) {
			if (upleft && upright && downright && downleft) {
				return 26;
			} else if (upleft && upright && downright && !downleft) {
				return 33;
			} else if (upleft && upright && !downright && downleft) {
				return 32;
			} else if (upleft && upright && !downright && !downleft) {
				return 11;
			} else if (upleft && !upright && downright && downleft) {
				return 44;
			} else if (upleft && !upright && downright && !downleft) {
				return 35;
			} else if (upleft && !upright && !downright && downleft) {
				return 10;
			} else if (upleft && !upright && !downright && !downleft) {
				return 20;
			} else if (!upleft && upright && downright && downleft) {
				return 45;
			} else if (!upleft && upright && downright && !downleft) {
				return 23;
			} else if (!upleft && upright && !downright && downleft) {
				return 34;
			} else if (!upleft && upright && !downright && !downleft) {
				return 8;
			} else if (!upleft && !upright && downright && downleft) {
				return 22;
			} else if (!upleft && !upright && downright && !downleft) {
				return 9;
			} else if (!upleft && !upright && !downright && downleft) {
				return 21;
			} else {
				return 46;
			}
		} else if (up && right && down && !left) {
			if (!upright && !downright) {
				return 6;
			} else if (!upright) {
				return 28;
			} else if (!downright) {
				return 30;
			} else {
				return 25;
			}
		} else if (up && right && !down && left) {
			if (!upleft && !upright) {
				return 18;
			} else if (!upleft) {
				return 40;
			} else if (!upright) {
				return 42;
			} else {
				return 38;
			}
		} else if (up && right && !down && !left) {
			if (!upright) {
				return 16;
			} else {
				return 37;
			}
		} else if (up && !right && down && left) {
			if (!upleft && !downleft) {
				return 19;
			} else if (!upleft) {
				return 43;
			} else if (!downleft) {
				return 41;
			} else {
				return 27;
			}
		} else if (up && !right && down && !left) {
			return 24;
		} else if (up && !right && !down && left) {
			if (!upleft) {
				return 17;
			} else {
				return 39;
			}
		} else if (up && !right && !down && !left) {
			return 36;
		} else if (!up && right && down && left) {
			if (!downleft && !downright) {
				return 7;
			} else if (!downleft) {
				return 31;
			} else if (!downright) {
				return 29;
			} else {
				return 14;
			}
		} else if (!up && right && down && !left) {
			if (!downright) {
				return 4;
			} else {
				return 13;
			}
		} else if (!up && right && !down && left) {
			return 2;
		} else if (!up && right && !down && !left) {
			return 1;
		} else if (!up && !right && down && left) {
			if (!downleft) {
				return 5;
			} else {
				return 15;
			}
		} else if (!up && !right && down && !left) {
			return 12;
		} else if (!up && !right && !down && left) {
			return 3;
		} else {
			return 0;
		}
	}

	@SubscribeEvent
	public void onMouseClick(SlotClickEvent event) {
		if (!isOverriding()) return;
		boolean isBlankStack = BetterContainers.isBlankStack(event.slot.slotNumber, event.slot.getStack());
		if (!(isBlankStack ||
			BetterContainers.isButtonStack(event.slot.slotNumber, event.slot.getStack()))) return;
		clickSlot(event.slotId);
		if (isBlankStack) {
			event.usePickblockInstead();
		}
	}

}
