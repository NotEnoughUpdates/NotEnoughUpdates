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
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemCooldowns {
	private static final Map<ItemStack, Float> durabilityOverrideMap = new HashMap<>();
	public static long pickaxeUseCooldownMillisRemaining = -1;
	private static long treecapitatorCooldownMillisRemaining = -1;
	private static long bonzomaskCooldownMillisRemaining = -1;
	private static long fraggedBonzomaskCooldownMillisRemaining = -1;

	public static boolean firstLoad = true;
	public static long firstLoadMillis = 0;

	private static long lastMillis = 0;

	public static long pickaxeCooldown = -1;
	private static long bonzoMaskCooldown = -1;
	private static long fraggedBonzoMaskCooldown = -1;

	public static TreeMap<Long, BlockPos> blocksClicked = new TreeMap<>();

	private static int tickCounter = 0;

	@SubscribeEvent
	public void tick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END && NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
			if (tickCounter++ >= 20 * 10) {
				tickCounter = 0;
				pickaxeCooldown = -1;
				bonzoMaskCooldown = -1;
				fraggedBonzoMaskCooldown = -1;
			}

			long currentTime = System.currentTimeMillis();
			if (firstLoad) {
				firstLoadMillis = currentTime;
				firstLoad = false;
			}

			Long key;
			while ((key = blocksClicked.floorKey(currentTime - 1500)) != null) {
				blocksClicked.remove(key);
			}

			long millisDelta = currentTime - lastMillis;
			lastMillis = currentTime;

			durabilityOverrideMap.clear();

			if (pickaxeUseCooldownMillisRemaining >= 0) {
				pickaxeUseCooldownMillisRemaining -= millisDelta;
			}
			if (treecapitatorCooldownMillisRemaining >= 0) {
				treecapitatorCooldownMillisRemaining -= millisDelta;
			}
			if (bonzomaskCooldownMillisRemaining >= 0) {
				bonzomaskCooldownMillisRemaining -= millisDelta;
			}
			if (fraggedBonzomaskCooldownMillisRemaining >= 0) {
				fraggedBonzomaskCooldownMillisRemaining -= millisDelta;
			}
		}
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		blocksClicked.clear();
		if (pickaxeCooldown > 0) pickaxeUseCooldownMillisRemaining = 60 * 1000;
		pickaxeCooldown = -1;
	}

	public static long getTreecapCooldownWithPet() {
		if (!NotEnoughUpdates.INSTANCE.config.itemOverlays.enableCooldownInItemDurability) {
			return 0;
		}

		PetInfoOverlay.Pet pet = PetInfoOverlay.getCurrentPet();
		if (NotEnoughUpdates.INSTANCE.config.itemOverlays.enableMonkeyCheck && pet != null) {
			if (pet.petLevel != null &&
				pet.petType.equalsIgnoreCase("monkey") &&
				pet.rarity.equals(PetInfoOverlay.Rarity.LEGENDARY)
			) {
				return 2000 - (int) (2000 * (0.005 * pet.petLevel.getCurrentLevel()));
			}
		}
		return 2000;
	}

	public static void blockClicked(BlockPos pos) {
		long currentTime = System.currentTimeMillis();
		blocksClicked.put(currentTime, pos);
	}

	public static void processBlockChangePacket(S23PacketBlockChange packetIn) {
		BlockPos pos = packetIn.getBlockPosition();

		if (blocksClicked.containsValue(pos)) {
			IBlockState oldState = Minecraft.getMinecraft().theWorld.getBlockState(pos);
			if (oldState.getBlock() != packetIn.getBlockState().getBlock()) {
				onBlockMined(pos);
			}
		}
	}

	public static void onBlockMined(BlockPos pos) {
		ItemStack held = Minecraft.getMinecraft().thePlayer.getHeldItem();
		String internalname = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(held);
		if (internalname != null) {
			if (treecapitatorCooldownMillisRemaining < 0 &&
				(internalname.equals("TREECAPITATOR_AXE") || internalname.equals("JUNGLE_AXE"))) {
				treecapitatorCooldownMillisRemaining = getTreecapCooldownWithPet();
			}
		}
	}

	private static final Pattern PICKAXE_ABILITY_REGEX = Pattern.compile("\\u00a7r\\u00a7aYou used your " +
		"\\u00a7r\\u00a7..+ \\u00a7r\\u00a7aPickaxe Ability!\\u00a7r");

	private static final Pattern COOLDOWN_LORE = Pattern.compile("\\u00a78Cooldown: \\u00a7a(\\d+)s");

	private static final Pattern BONZO_ABILITY_ACTIVATION =
		Pattern.compile("\\u00a7r\\u00a7aYour \\u00a7r\\u00a7[9|5](\\u269A )*Bonzo's Mask \\u00a7r\\u00a7asaved your life!\\u00a7r");

	private static boolean isPickaxe(String internalname) {
		if (internalname == null) return false;

		if (internalname.endsWith("_PICKAXE")) {
			return true;
		} else if (internalname.contains("_DRILL_")) {
			char lastChar = internalname.charAt(internalname.length() - 1);
			return lastChar >= '0' && lastChar <= '9';
		} else return internalname.equals("GEMSTONE_GAUNTLET") || internalname.equals("PICKONIMBUS") || internalname.equals("DIVAN_DRILL");
	}

	private static void updatePickaxeCooldown() {
		if (pickaxeCooldown == -1 && NotEnoughUpdates.INSTANCE.config.itemOverlays.pickaxeAbility) {
			for (ItemStack stack : Minecraft.getMinecraft().thePlayer.inventory.mainInventory) {
				if (stack != null && stack.hasTagCompound()) {
					String internalname = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(stack);
					if (isPickaxe(internalname)) {
						for (String line : NotEnoughUpdates.INSTANCE.manager.getLoreFromNBT(stack.getTagCompound())) {
							Matcher matcher = COOLDOWN_LORE.matcher(line);
							if (matcher.find()) {
								try {
									pickaxeCooldown = Integer.parseInt(matcher.group(1));
									return;
								} catch (Exception ignored) {
								}
							}
						}
					}
				}
			}
			pickaxeCooldown = 0;
		}
	}

	private static void updateBonzoMaskCooldown(boolean fragged) {
		if (bonzoMaskCooldown == -1 || fraggedBonzoMaskCooldown == -1 && NotEnoughUpdates.INSTANCE.config.itemOverlays.bonzoAbility) {
			for (ItemStack stack : Minecraft.getMinecraft().thePlayer.inventory.mainInventory) {
				findBonzoCooldown(stack, fragged);
			}
			// Only need to check the helmet slot in armorInventory since the mask can only go there
			findBonzoCooldown(Minecraft.getMinecraft().thePlayer.inventory.armorInventory[3], fragged);
		}
	}

	private static void findBonzoCooldown(ItemStack stack, boolean fragged) {
		if (stack != null && stack.hasTagCompound() && stack.getDisplayName().contains("Bonzo's Mask")) {
			for (String line : NotEnoughUpdates.INSTANCE.manager.getLoreFromNBT(stack.getTagCompound())) {
				Matcher matcher = COOLDOWN_LORE.matcher(line);
				if (matcher.find()) {
					try {
						if (fragged) {
							fraggedBonzoMaskCooldown = Integer.parseInt(matcher.group(1));
						} else {
							bonzoMaskCooldown = Integer.parseInt(matcher.group(1));
						}
						return;
					} catch (Exception ignored) {
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onChatMessage(ClientChatReceivedEvent event) {
		if (pickaxeCooldown != 0 && PICKAXE_ABILITY_REGEX.matcher(event.message.getFormattedText()).matches() &&
			NotEnoughUpdates.INSTANCE.config.itemOverlays.pickaxeAbility) {
			updatePickaxeCooldown();
			pickaxeUseCooldownMillisRemaining = pickaxeCooldown * 1000;
		}

		if (BONZO_ABILITY_ACTIVATION.matcher(event.message.getFormattedText()).matches() &&
				NotEnoughUpdates.INSTANCE.config.itemOverlays.bonzoAbility &&
				(bonzoMaskCooldown != 0 || fraggedBonzoMaskCooldown != 0)) {
			if (event.message.getFormattedText().contains("⚚")) {
				updateBonzoMaskCooldown(true);
				fraggedBonzomaskCooldownMillisRemaining = fraggedBonzoMaskCooldown * 1000;
			} else {
				updateBonzoMaskCooldown(false);
				bonzomaskCooldownMillisRemaining = bonzoMaskCooldown * 1000;
			}
		}
	}

	public static float getDurabilityOverride(ItemStack stack) {
		if (Minecraft.getMinecraft().theWorld == null) return -1;
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) return -1;

		if (durabilityOverrideMap.containsKey(stack)) {
			return durabilityOverrideMap.get(stack);
		}

		String internalname = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(stack);
		if (internalname == null) {
			durabilityOverrideMap.put(stack, -1f);
			return -1;
		}

		// Pickaxes
		if (isPickaxe(internalname)) {
			updatePickaxeCooldown();

			return durabilityOverride(pickaxeUseCooldownMillisRemaining, pickaxeCooldown, stack);
		}
		// Treecapitator / Jungle Axe
		if (internalname.equals("TREECAPITATOR_AXE") || internalname.equals("JUNGLE_AXE")) {
			if (treecapitatorCooldownMillisRemaining < 0) {
				durabilityOverrideMap.put(stack, -1f);
				return -1;
			}

			if (treecapitatorCooldownMillisRemaining > getTreecapCooldownWithPet()) {
				return stack.getItemDamage();
			}

			float durability = treecapitatorCooldownMillisRemaining / (float) getTreecapCooldownWithPet();
			durabilityOverrideMap.put(stack, durability);

			return durability;
		}
		// Bonzo Masks
		if (internalname.equals("BONZO_MASK")) {
			updateBonzoMaskCooldown(false);

			return durabilityOverride(bonzomaskCooldownMillisRemaining, bonzoMaskCooldown, stack);
		}
		if (internalname.equals("STARRED_BONZO_MASK")) {
			updateBonzoMaskCooldown(true);

			return durabilityOverride(fraggedBonzomaskCooldownMillisRemaining, fraggedBonzoMaskCooldown, stack);
		}

		durabilityOverrideMap.put(stack, -1f);
		return -1;
	}

	private static float durabilityOverride(float millisRemaining, long cooldown, ItemStack stack) {
		if (millisRemaining < 0) {
			durabilityOverrideMap.put(stack, -1f);
			return -1;
		}

		if (millisRemaining > cooldown * 1000) {
			return stack.getItemDamage();
		}

		float durability = (float) (millisRemaining / (cooldown * 1000.0));
		durabilityOverrideMap.put(stack, durability);

		return durability;
	}
}
