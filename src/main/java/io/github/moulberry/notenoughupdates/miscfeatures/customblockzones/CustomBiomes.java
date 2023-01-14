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

package io.github.moulberry.notenoughupdates.miscfeatures.customblockzones;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.events.OnBlockBreakSoundEffect;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.BlockPos;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;


@NEUAutoSubscribe
public class CustomBiomes {

	public static final CustomBiomes INSTANCE = new CustomBiomes();

	private final Map<String, IslandZoneSubdivider> subdividers = new HashMap<>();

	private CustomBiomes() {
		subdividers.put("crystal_hollows", new CrystalHollowsTextures());
		subdividers.put("mining_3", new DwarvenMinesTextures());
	}

	//Biome Prefix: NeuAreaBiomeName
	//Example: NeuCHJungle

	public BiomeGenBase getCustomBiome(BlockPos pos) {
		SpecialBlockZone specialZone = getSpecialZone(pos);
		if (specialZone != null) {
			if ((specialZone.isDwarvenMines() && NotEnoughUpdates.INSTANCE.config.mining.dwarvenTextures)
				|| (specialZone.isCrystalHollows() && NotEnoughUpdates.INSTANCE.config.mining.crystalHollowTextures))
				return specialZone.getCustomBiome();
		}
		return null;
	}

	/**
	 * Finds the special zone for the give block position
	 * <p>
	 * Returns null on error
	 */
	public SpecialBlockZone getSpecialZone(BlockPos pos) {
		if (Minecraft.getMinecraft().theWorld == null) return null;
		String location = SBInfo.getInstance().getLocation();
		IslandZoneSubdivider subdivider = subdividers.get(location);
		if (subdivider == null) return SpecialBlockZone.NON_SPECIAL_ZONE;
		return subdivider.getSpecialZoneForBlock(location, pos);
	}

	@SubscribeEvent
	public void onBreakSound(OnBlockBreakSoundEffect event) {
		SpecialBlockZone specialZone = getSpecialZone(event.getPosition());
		boolean hasMithrilSounds = NotEnoughUpdates.INSTANCE.config.mining.mithrilSounds;
		boolean hasCrystalSounds = NotEnoughUpdates.INSTANCE.config.mining.gemstoneSounds;
		if (specialZone != null) {
			CustomBlockSounds.CustomSoundEvent customSound = null;
			if (specialZone.hasMithril() && isBreakableMithril(event.getBlock()) && hasMithrilSounds &&
				SBInfo.getInstance().getLocation().equals("mining_3")) {
				customSound = CustomBlockSounds.mithrilBreak;
			}
			if (specialZone.hasMithril() && isMithrilHollows(event.getBlock()) && hasMithrilSounds &&
				SBInfo.getInstance().getLocation().equals("crystal_hollows")) {
				customSound = CustomBlockSounds.mithrilBreak;
			}
			if (specialZone.hasTitanium() && isTitanium(event.getBlock()) && hasMithrilSounds) {
				customSound = CustomBlockSounds.titaniumBreak;
			}

			if (specialZone.hasGemstones() && isGemstone(event.getBlock(), EnumDyeColor.RED) && hasCrystalSounds) {
				customSound = CustomBlockSounds.gemstoneBreakRuby;
			}
			if (specialZone.hasGemstones() && isGemstone(event.getBlock(), EnumDyeColor.YELLOW) && hasCrystalSounds) {
				customSound = CustomBlockSounds.gemstoneBreakTopaz;
			}
			if (specialZone.hasGemstones() && isGemstone(event.getBlock(), EnumDyeColor.PINK) && hasCrystalSounds) {
				customSound = CustomBlockSounds.gemstoneBreakJasper;
			}
			if (specialZone.hasGemstones() && isGemstone(event.getBlock(), EnumDyeColor.LIGHT_BLUE) && hasCrystalSounds) {
				customSound = CustomBlockSounds.gemstoneBreakSapphire;
			}
			if (specialZone.hasGemstones() && isGemstone(event.getBlock(), EnumDyeColor.ORANGE) && hasCrystalSounds) {
				customSound = CustomBlockSounds.gemstoneBreakAmber;
			}
			if (specialZone.hasGemstones() && isGemstone(event.getBlock(), EnumDyeColor.PURPLE) && hasCrystalSounds) {
				customSound = CustomBlockSounds.gemstoneBreakAmethyst;
			}
			if (specialZone.hasGemstones() && isGemstone(event.getBlock(), EnumDyeColor.LIME) && hasCrystalSounds) {
				customSound = CustomBlockSounds.gemstoneBreakJade;
			}

			if (customSound != null) {
				if (customSound.shouldReplace()) {
					event.setSound(customSound.replaceSoundEvent(event.getSound()));
				} else {
					event.setCanceled(true);
				}
			}
		}
	}

	public static boolean isTitanium(IBlockState state) {
		return state.getBlock() == Blocks.stone && state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.DIORITE_SMOOTH;
	}

	public static boolean isMithril(IBlockState state) {
		return isBreakableMithril(state)
			|| state.getBlock() == Blocks.bedrock;
	}

	public static boolean isBreakableMithril(IBlockState state) {
		return (state.getBlock() == Blocks.stained_hardened_clay && state.getValue(BlockColored.COLOR) == EnumDyeColor.CYAN)
			|| (state.getBlock() == Blocks.wool && state.getValue(BlockColored.COLOR) == EnumDyeColor.GRAY)
			|| (state.getBlock() == Blocks.wool && state.getValue(BlockColored.COLOR) == EnumDyeColor.LIGHT_BLUE)
			|| state.getBlock() == Blocks.prismarine;
	}

	public static boolean isMithrilHollows(IBlockState state) {
		return state.getBlock() == Blocks.prismarine
			|| (state.getBlock() == Blocks.wool && state.getValue(BlockColored.COLOR) == EnumDyeColor.LIGHT_BLUE);
	}

	public static boolean isGemstone(IBlockState state, EnumDyeColor color) {
		return ((state.getBlock() == Blocks.stained_glass || state.getBlock() == Blocks.stained_glass_pane) &&
			state.getValue(BlockColored.COLOR) == color);
	}

	@SubscribeEvent
	public void onLocationChange(LocationChangeEvent event) {
		WorldClient world = Minecraft.getMinecraft().theWorld;
		String location = event.newLocation;
		if (world == null) return;
		if (location == null) return;
		switch (location.intern()) {
			case "crystal_hollows":
			case "mining_3":
				//if has custom biome, do chunk update or something
				EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
				if (player == null) return;

				world.markBlocksDirtyVertical((int) player.posX, (int) player.posX, (int) player.posZ, (int) player.posZ);
		}
	}

}
