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

package io.github.moulberry.notenoughupdates.miscfeatures.world;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.SpecialColour;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@NEUAutoSubscribe
public class FrozenTreasuresHighlighter extends GenericBlockHighlighter {

	private static final FrozenTreasuresHighlighter INSTANCE = new FrozenTreasuresHighlighter();

	private static final List<String> rideablePetTextureUrls = new ArrayList<String>() {{
		// Armadillo
		add("http://textures.minecraft.net/texture/c1eb6df4736ae24dd12a3d00f91e6e3aa7ade6bbefb0978afef2f0f92461018f");
		// Rock
		add("http://textures.minecraft.net/texture/cb2b5d48e57577563aca31735519cb622219bc058b1f34648b67b8e71bc0fa");
		// Rat
		add("http://textures.minecraft.net/texture/a8abb471db0ab78703011979dc8b40798a941f3a4dec3ec61cbeec2af8cffe8");
		// Mayor Jerry
		add("http://textures.minecraft.net/texture/41b830eb4082acec836bc835e40a11282bb51193315f91184337e8d3555583");
	}};

	public static FrozenTreasuresHighlighter getInstance() {return INSTANCE;}

	@Override
	protected boolean isEnabled() {
		return SBInfo.getInstance().getScoreboardLocation().equals("Glacial Cave")
			&& NotEnoughUpdates.INSTANCE.config.world.highlightFrozenTreasures;
	}

	@Override
	protected boolean isValidHighlightSpot(BlockPos key) {
		World w = Minecraft.getMinecraft().theWorld;
		if (w == null) return false;
		Block b = w.getBlockState(key).getBlock();
		return b == Blocks.ice;
	}

	@SubscribeEvent
	public void onTickNew(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END || !isEnabled()) return;
		World w = Minecraft.getMinecraft().theWorld;
		if (w == null) return;
		List<Entity> entities = w.getLoadedEntityList();
		for (Entity entity : entities) {
			if ((entity instanceof EntityArmorStand) &&
					((EntityArmorStand) entity).getCurrentArmor(3) != null) {

				// If an armor stand has a 'hat' with a NBTTagCompound check if it has a pet texture url
				if (((EntityArmorStand) entity).getCurrentArmor(3).hasTagCompound()) {
					NBTTagCompound nbtTagCompound = ((EntityArmorStand) entity).getCurrentArmor(3).getTagCompound();

					// Get Base64 texture value from the tag compound
					String textureValue = nbtTagCompound
						.getCompoundTag("SkullOwner")
						.getCompoundTag("Properties")
						.getTagList("textures", 10)
						.getCompoundTagAt(0)
						.getString("Value");

					// Decode and find texture url from the texture value
					String trimmedJson = new String(Base64.getDecoder().decode(textureValue.replace(";", ""))).replace(" ", "");


					String textureUrl = "";
					if (trimmedJson.contains("url")) {
						textureUrl = trimmedJson.substring(
							trimmedJson.indexOf("url")+6, // Start of url
							trimmedJson.substring( // Get the substring from the start of the url to the end of string
								trimmedJson.indexOf("url")+6).indexOf("\"") // Get index of first " after start of url
								+ trimmedJson.indexOf("url")+6); // Add on the length of numbers up until the start of url to get correct index from overall string
					}


					// If the list of rideable pet texture urls doesn't include the found texture then it is a frozen treasure
					if (!rideablePetTextureUrls.contains(textureUrl)) {
						highlightedBlocks.add(entity.getPosition().add(0, 1, 0));
					}
				} else {
					// This is for frozen treasures which are just blocks i.e. Packed Ice, Enchanted Packed Ice etc.
					// (Since I don't believe the blocks have NBTTagCompound data)
					highlightedBlocks.add(entity.getPosition().add(0, 1, 0));
				}
			}
		}
	}

	@Override
	protected int getColor(BlockPos blockPos) {
		return SpecialColour.specialToChromaRGB(NotEnoughUpdates.INSTANCE.config.world.frozenTreasuresColor2);
	}
}
