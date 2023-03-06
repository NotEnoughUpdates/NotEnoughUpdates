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

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.events.SlotClickEvent;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@NEUAutoSubscribe
public class EnchantingSolvers {
	public static SolverType currentSolver = SolverType.NONE;

	public enum SolverType {
		NONE,
		CHRONOMATRON,
		ULTRASEQUENCER,
		SUPERPAIRS
	}

	private static final NBTTagCompound enchTag = new NBTTagCompound() {{
		setTag("ench", new NBTTagList());
	}};

	// Chronomatron
	private static boolean addToChronomatron = false;
	private static boolean chronomatronStartSeq = false;
	private static final List<String> chronomatronOrder = new ArrayList<>();
	private static int chronomatronReplayIndex = 0;
	private static int lastChronomatronSize = 0;
	private static long millisLastClick = 0;

	// Ultrasequencer
	private static class UltrasequencerItem {
		ItemStack stack;
		int containerIndex;

		public UltrasequencerItem(ItemStack stack, int containerIndex) {
			this.stack = stack;
			this.containerIndex = containerIndex;
		}
	}

	private static final Map<Integer, UltrasequencerItem> ultraSequencerOrder = new HashMap<>();
	private static int ultrasequencerReplayIndex = 0;

	// Superpairs
	private static final Map<Integer, ItemStack> superpairStacks = new HashMap<>();
	private static int lastSlotClicked = -1;
	private static final HashSet<Integer> successfulMatches = new HashSet<>();
	private static final HashSet<Integer> possibleMatches = new HashSet<>();
	private static final HashSet<Integer> powerupMatches = new HashSet<>();

	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
		chronomatronOrder.clear();
		currentSolver = SolverType.NONE;

		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
			return;
		}

		String openChestName = Utils.getOpenChestName();
		if (!openChestName.contains("Stakes")) {
			if (openChestName.startsWith("Chronomatron")) {
				currentSolver = SolverType.CHRONOMATRON;
			} else if (openChestName.startsWith("Ultrasequencer")) {
				currentSolver = SolverType.ULTRASEQUENCER;
			} else if (openChestName.startsWith("Superpairs")) {
				currentSolver = SolverType.SUPERPAIRS;
			}
		}
	}

	public static ItemStack overrideStack(IInventory inventory, int slotIndex, ItemStack stack) {
		if (!NotEnoughUpdates.INSTANCE.config.enchantingSolvers.enableEnchantingSolvers) {
			return null;
		}

		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
			return null;
		}

		if (stack != null && stack.getDisplayName() != null) {
			if (Minecraft.getMinecraft().currentScreen instanceof GuiChest) {
				GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
				ContainerChest container = (ContainerChest) chest.inventorySlots;
				IInventory lower = container.getLowerChestInventory();

				if (lower != inventory) {
					return null;
				}

				String displayName = stack.getDisplayName();

				if (currentSolver == SolverType.CHRONOMATRON) {
					ItemStack timerStack = lower.getStackInSlot(lower.getSizeInventory() - 5);
					if (timerStack == null) {
						return null;
					}

					boolean yepClock = timerStack.getItem() == Items.clock;
					if (yepClock && (addToChronomatron && chronomatronOrder.size() >= lastChronomatronSize + 1)) {
						if (chronomatronReplayIndex < chronomatronOrder.size()) {
							String chronomatronCurrent = chronomatronOrder.get(chronomatronReplayIndex);
							if (stack.getItem() == Item.getItemFromBlock(Blocks.stained_glass) ||
								stack.getItem() == Item.getItemFromBlock(Blocks.stained_hardened_clay)) {
								long currentTime = System.currentTimeMillis();

								boolean lastSame = chronomatronReplayIndex > 0 &&
									chronomatronCurrent.equals(chronomatronOrder.get(chronomatronReplayIndex - 1));

								if (chronomatronCurrent.equals(displayName)) {
									if (!lastSame || currentTime - millisLastClick > 300) {
										ItemStack retStack = new ItemStack(
											Item.getItemFromBlock(Blocks.stained_hardened_clay),
											1,
											stack.getItemDamage()
										);
										retStack.setTagCompound(enchTag);
										retStack.setStackDisplayName(stack.getDisplayName());
										return retStack;
									} else {
										ItemStack retStack = new ItemStack(
											Item.getItemFromBlock(Blocks.stained_glass),
											1,
											stack.getItemDamage()
										);
										retStack.setStackDisplayName(stack.getDisplayName());
										return retStack;
									}
								} else {
									if (chronomatronReplayIndex + 1 < chronomatronOrder.size() &&
										NotEnoughUpdates.INSTANCE.config.enchantingSolvers.showNextClick) {
										String chronomatronNext = chronomatronOrder.get(chronomatronReplayIndex + 1);
										if (chronomatronNext.equals(displayName)) {
											ItemStack retStack = new ItemStack(
												Item.getItemFromBlock(Blocks.stained_glass),
												1,
												stack.getItemDamage()
											);
											retStack.setStackDisplayName(stack.getDisplayName());
											return retStack;
										}
									}
									ItemStack retStack = new ItemStack(Item.getItemFromBlock(Blocks.stained_glass), 1, 8);
									retStack.setStackDisplayName(stack.getDisplayName());
									return retStack;
								}
							}

						}
					}
				} else if (currentSolver == SolverType.ULTRASEQUENCER) {
					ItemStack timerStack = lower.getStackInSlot(lower.getSizeInventory() - 5);
					if (timerStack == null) {
						return null;
					}

					boolean yepClock = timerStack.getItem() == Items.clock;
					if (stack.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane) && stack.getItemDamage() != 15) {
						if (yepClock) {
							for (int solveIndex : ultraSequencerOrder.keySet()) {
								UltrasequencerItem item = ultraSequencerOrder.get(solveIndex);
								if (item.containerIndex == slotIndex) {
									ItemStack newStack = item.stack;
									if (solveIndex == ultrasequencerReplayIndex) {
										newStack.setTagCompound(enchTag);
									} else {
										newStack.setTagCompound(null);
									}
									return newStack;
								}
							}
							ItemStack retStack = new ItemStack(Item.getItemFromBlock(Blocks.stained_glass_pane), 1, 15);
							retStack.setStackDisplayName(stack.getDisplayName());
							return retStack;
						}
					}
				} else if (currentSolver == SolverType.SUPERPAIRS) {
					if (stack.getItem() == Item.getItemFromBlock(Blocks.stained_glass) &&
						superpairStacks.containsKey(slotIndex)) {
						return superpairStacks.get(slotIndex);
					}
				}
			}
		}
		return null;
	}

	public static boolean onStackRender(ItemStack stack, IInventory inventory, int slotIndex, int x, int y) {
		if (!NotEnoughUpdates.INSTANCE.config.enchantingSolvers.enableEnchantingSolvers) {
			return false;
		}

		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
			return false;
		}

		if (stack != null && stack.getDisplayName() != null) {
			if (Minecraft.getMinecraft().currentScreen instanceof GuiChest) {
				GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
				ContainerChest container = (ContainerChest) chest.inventorySlots;
				IInventory lower = container.getLowerChestInventory();

				if (lower != inventory) {
					return false;
				}

				if (currentSolver == SolverType.ULTRASEQUENCER) {
					ItemStack timerStack = lower.getStackInSlot(lower.getSizeInventory() - 5);
					if (timerStack == null) {
						return false;
					}

					boolean yepClock = timerStack.getItem() == Items.clock;
					if (stack.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane) && stack.getItemDamage() != 15) {
						if (yepClock) {
							for (int solveIndex : ultraSequencerOrder.keySet()) {
								UltrasequencerItem item = ultraSequencerOrder.get(solveIndex);
								if (item.containerIndex == slotIndex) {
									int meta = 0;
									if (solveIndex == ultrasequencerReplayIndex) {
										meta = NotEnoughUpdates.INSTANCE.config.enchantingSolvers.seqNext;
									} else if (solveIndex == ultrasequencerReplayIndex + 1) {
										meta = NotEnoughUpdates.INSTANCE.config.enchantingSolvers.seqUpcoming;
									}
									if (meta > 0) {
										Utils.drawItemStack(
											new ItemStack(Item.getItemFromBlock(Blocks.stained_glass_pane), 1, meta - 1),
											x,
											y
										);
									}
									if (NotEnoughUpdates.INSTANCE.config.enchantingSolvers.seqNumbers &&
										solveIndex >= ultrasequencerReplayIndex) {
										int w = Minecraft.getMinecraft().fontRendererObj.getStringWidth((solveIndex + 1) + "");
										GlStateManager.disableDepth();
										GlStateManager.enableBlend();
										GlStateManager.disableLighting();
										Utils.drawStringScaled((solveIndex + 1) + "",
											x + 8.5f - w / 2f, y + 8.5f - 4, true, 0xffc0c0c0, 1f
										);
										return true;
									}
								}
							}
						}
					}
				} else if (currentSolver == SolverType.SUPERPAIRS) {
					int meta = 0;
					if (stack.getItem() == Item.getItemFromBlock(Blocks.stained_glass) &&
						superpairStacks.containsKey(slotIndex)) {
						if (possibleMatches.contains(slotIndex)) {
							meta = NotEnoughUpdates.INSTANCE.config.enchantingSolvers.supPossible;
						} else {
							meta = NotEnoughUpdates.INSTANCE.config.enchantingSolvers.supUnmatched;
						}
					} else {
						if (powerupMatches.contains(slotIndex)) {
							meta = NotEnoughUpdates.INSTANCE.config.enchantingSolvers.supPower;
						} else if (successfulMatches.contains(slotIndex)) {
							meta = NotEnoughUpdates.INSTANCE.config.enchantingSolvers.supMatched;
						}
					}
					if (meta > 0) {
						Utils.drawItemStack(new ItemStack(Item.getItemFromBlock(Blocks.stained_glass_pane), 1, meta - 1), x, y);
					}
				}
			}
		}
		return false;
	}

	@SubscribeEvent
	public void onStackClick(SlotClickEvent event) {
		if (!NotEnoughUpdates.INSTANCE.config.enchantingSolvers.enableEnchantingSolvers
			|| !NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
			return;
		}

		ItemStack stack = event.slot.getStack();
		if (stack == null || stack.getDisplayName() == null) {
			return;
		}
		String displayName = stack.getDisplayName();
		if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) {
			return;
		}
		GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
		ContainerChest container = (ContainerChest) chest.inventorySlots;
		IInventory lower = container.getLowerChestInventory();

		if (currentSolver == SolverType.CHRONOMATRON) {
			ItemStack timerStack = lower.getStackInSlot(lower.getSizeInventory() - 5);
			if (timerStack == null) {
				return;
			}

			boolean yepClock = timerStack.getItem() == Items.clock;
			if (timerStack.getItem() == Item.getItemFromBlock(Blocks.glowstone) ||
				(yepClock && (!addToChronomatron || chronomatronOrder.size() < lastChronomatronSize + 1))) {
				event.setCanceled(true);
				return;
			}
			if (yepClock) {
				long currentTime = System.currentTimeMillis();
				if (currentTime - millisLastClick < 150) {
					event.setCanceled(true);
					return;
				}

				if (chronomatronReplayIndex < chronomatronOrder.size()) {
					String chronomatronCurrent = chronomatronOrder.get(chronomatronReplayIndex);
					if ((!NotEnoughUpdates.INSTANCE.config.enchantingSolvers.preventMisclicks1 ||
						chronomatronCurrent.equals(displayName) || Keyboard.getEventKey() == Keyboard.KEY_LSHIFT) &&
						stack.getItem() != Item.getItemFromBlock(Blocks.stained_glass_pane) && event.slotId != 4 &&
						event.slotId != 49) {
						chronomatronReplayIndex++;
						millisLastClick = currentTime;
						event.usePickblockInstead();
						return;
					}
				}
				event.setCanceled(true);
				return;
			}
		}
		if (currentSolver == SolverType.ULTRASEQUENCER) {
			ItemStack timerStack = lower.getStackInSlot(lower.getSizeInventory() - 5);
			if (timerStack == null) {
				return;
			}

			boolean yepClock = timerStack.getItem() == Items.clock;
			if (yepClock) {
				UltrasequencerItem current = ultraSequencerOrder.get(ultrasequencerReplayIndex);
				long currentTime = System.currentTimeMillis();
				if (current == null) {
					event.setCanceled(true);
					return;
				}
				if (currentTime - millisLastClick > 150 &&
					(!NotEnoughUpdates.INSTANCE.config.enchantingSolvers.preventMisclicks1 ||
						current.containerIndex == event.slotId || Keyboard.getEventKey() == Keyboard.KEY_LSHIFT) &&
					(event.slotId < 45 && event.slotId > 8)) {
					ultrasequencerReplayIndex++;
					millisLastClick = currentTime;
					event.usePickblockInstead();
					return;
				}
			}
			event.setCanceled(true);
		} else if (currentSolver == SolverType.SUPERPAIRS) {
			lastSlotClicked = event.slotId;
		}
	}

	public static void processInventoryContents(boolean fromTick) {
		if (currentSolver != SolverType.CHRONOMATRON && !fromTick) return;

		if (!NotEnoughUpdates.INSTANCE.config.enchantingSolvers.enableEnchantingSolvers) {
			return;
		}

		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
			return;
		}

		if (Minecraft.getMinecraft().currentScreen instanceof GuiChest) {
			GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
			ContainerChest container = (ContainerChest) chest.inventorySlots;
			IInventory lower = container.getLowerChestInventory();

			if (currentSolver == SolverType.CHRONOMATRON) {
				ItemStack timerStack = lower.getStackInSlot(lower.getSizeInventory() - 5);
				if (timerStack == null) {
					return;
				}

				String stainedHardenedClayName = null;
				for (int index = 0; index < lower.getSizeInventory(); index++) {
					ItemStack stack = lower.getStackInSlot(index);
					if (stack != null && stack.getItem() == Item.getItemFromBlock(Blocks.stained_hardened_clay)) {
						if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("ench")) {
							if (stainedHardenedClayName != null && !stack.getDisplayName().equals(stainedHardenedClayName)) {
								return;
							}
							stainedHardenedClayName = stack.getDisplayName();
						}
					}
				}

				boolean yepClock = timerStack.getItem() == Items.clock;
				if (timerStack.getItem() == Item.getItemFromBlock(Blocks.glowstone) ||
					(yepClock && (!addToChronomatron || chronomatronOrder.size() < lastChronomatronSize + 1))) {
					if (chronomatronStartSeq) {
						chronomatronStartSeq = false;
						addToChronomatron = false;
						lastChronomatronSize = chronomatronOrder.size();
						chronomatronOrder.clear();
					}

					if (stainedHardenedClayName != null) {
						if (addToChronomatron) {
							chronomatronOrder.add(stainedHardenedClayName);
						}
						addToChronomatron = false;
					} else {
						addToChronomatron = true;
						chronomatronReplayIndex = 0;
					}
				} else if (yepClock) {
					chronomatronStartSeq = true;
				}
			} else {
				chronomatronStartSeq = true;
				addToChronomatron = true;
			}
			if (currentSolver == SolverType.ULTRASEQUENCER) {
				ItemStack timerStack = lower.getStackInSlot(lower.getSizeInventory() - 5);
				if (timerStack == null) {
					return;
				}
				if (timerStack.getItem() == Item.getItemFromBlock(Blocks.glowstone)) {
					ultrasequencerReplayIndex = 0;
				}

				for (int index = 0; index < lower.getSizeInventory(); index++) {
					ItemStack stack = lower.getStackInSlot(index);
					if (stack != null && stack.getItem() == Items.dye) {
						if (ultraSequencerOrder.containsKey(stack.stackSize - 1)) {
							UltrasequencerItem ultrasequencerItem = ultraSequencerOrder.get(stack.stackSize - 1);
							ultrasequencerItem.containerIndex = index;
							ultrasequencerItem.stack = stack;
						} else {
							ultraSequencerOrder.put(stack.stackSize - 1, new UltrasequencerItem(stack, index));
						}
					}
				}
			} else {
				ultraSequencerOrder.clear();
			}
			if (currentSolver == SolverType.SUPERPAIRS) {
				successfulMatches.clear();
				possibleMatches.clear();
				powerupMatches.clear();
				out:
				for (int index = 0; index < lower.getSizeInventory(); index++) {
					ItemStack stack = lower.getStackInSlot(index);
					if (stack == null) continue;
					if (stack.getItem() != Item.getItemFromBlock(Blocks.stained_glass) &&
						stack.getItem() != Item.getItemFromBlock(Blocks.stained_glass_pane)) {
						superpairStacks.put(index, stack);

						NBTTagCompound tag = stack.getTagCompound();
						if (tag != null) {
							NBTTagCompound display = tag.getCompoundTag("display");
							if (display.hasKey("Lore", 9)) {
								NBTTagList list = display.getTagList("Lore", 8);
								for (int i = 0; i < list.tagCount(); i++) {
									if (list.getStringTagAt(i).toLowerCase().contains("powerup")) {
										powerupMatches.add(index);
										continue out;
									}
								}
							}
						}

						int numMatches = 0;
						for (int index2 = 0; index2 < lower.getSizeInventory(); index2++) {
							ItemStack stack2 = lower.getStackInSlot(index2);
							if (stack2 != null && stack2.getDisplayName().equals(stack.getDisplayName()) &&
								stack.getItem() == stack2.getItem() && stack.getItemDamage() == stack2.getItemDamage()) {
								numMatches++;
							}
						}
						boolean oddMatches = (numMatches % 2) == 1;

						if ((!oddMatches || index != lastSlotClicked) && !successfulMatches.contains(index)) {
							for (int index2 = 0; index2 < lower.getSizeInventory(); index2++) {
								if (index == index2) continue;
								if (oddMatches && index2 == lastSlotClicked) continue;

								ItemStack stack2 = lower.getStackInSlot(index2);
								if (stack2 != null && stack2.getDisplayName().equals(stack.getDisplayName()) &&
									stack.getItem() == stack2.getItem() && stack.getItemDamage() == stack2.getItemDamage()) {
									successfulMatches.add(index);
									successfulMatches.add(index2);
								}
							}
						}
					} else {
						if (superpairStacks.containsKey(index) && superpairStacks.get(index) != null &&
							!possibleMatches.contains(index)) {
							ItemStack stack1 = superpairStacks.get(index);
							for (int index2 = 0; index2 < lower.getSizeInventory(); index2++) {
								if (index == index2) continue;

								if (superpairStacks.containsKey(index2) && superpairStacks.get(index2) != null) {
									ItemStack stack2 = superpairStacks.get(index2);
									if (stack1.getDisplayName().equals(stack2.getDisplayName()) &&
										stack1.getItem() == stack2.getItem() && stack1.getItemDamage() == stack2.getItemDamage()) {
										possibleMatches.add(index);
										possibleMatches.add(index2);
									}
								}
							}
						}
					}
				}
			} else {
				superpairStacks.clear();
				successfulMatches.clear();
				powerupMatches.clear();
				lastSlotClicked = -1;
			}
		}
	}

	@SubscribeEvent
	public void onItemTooltip(ItemTooltipEvent event) {
		if (NotEnoughUpdates.INSTANCE.config.enchantingSolvers.hideTooltips &&
			(currentSolver == SolverType.CHRONOMATRON || currentSolver == SolverType.ULTRASEQUENCER)) {
			String internal = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(event.itemStack);
			if (internal == null && event.toolTip.size() > 0 && !event.toolTip
				.get(0)
				.trim()
				.replaceAll("\\(#.+\\)$", "")
				.trim()
				.contains(" ")) {
				event.toolTip.clear();
			}
		}
	}

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) {
			currentSolver = SolverType.NONE;
		}

		if (event.phase != TickEvent.Phase.END) {
			return;
		}

		processInventoryContents(true);
	}

	public static boolean disableButtons() {
		return currentSolver != SolverType.NONE && NotEnoughUpdates.INSTANCE.config.enchantingSolvers.hideButtons;
	}
}
