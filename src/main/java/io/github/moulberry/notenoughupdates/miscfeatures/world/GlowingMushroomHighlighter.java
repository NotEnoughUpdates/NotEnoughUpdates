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

package io.github.moulberry.notenoughupdates.miscfeatures.world;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.events.SpawnParticleEvent;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.SpecialColour;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static io.github.moulberry.notenoughupdates.util.MathUtil.isDecimalPartApproximately;

@NEUAutoSubscribe
public class GlowingMushroomHighlighter extends GenericBlockHighlighter {

	@SubscribeEvent
	public void onParticleSpawn(SpawnParticleEvent event) {
		if (!isEnabled()) return;
		if (event.getParticleTypes() == EnumParticleTypes.SPELL_MOB) {
			if (
				isDecimalPartApproximately(event.getXCoord(), 0.5)
					&& isDecimalPartApproximately(event.getYCoord(), 0.1)
					&& isDecimalPartApproximately(event.getZCoord(), 0.5)
			) {
				tryRegisterInterest(event.getXCoord(), event.getYCoord(), event.getZCoord());
			}
		}
	}


	@Override
	protected boolean isEnabled() {
		return "farming_1".equals(SBInfo.getInstance().getLocation())
			&& NotEnoughUpdates.INSTANCE.config.world.highlightGlowingMushrooms;
	}

	@Override
	protected boolean isValidHighlightSpot(BlockPos key) {
		World w = Minecraft.getMinecraft().theWorld;
		if (w == null) return false;
		Block b = w.getBlockState(key).getBlock();
		return b == Blocks.brown_mushroom || b == Blocks.red_mushroom;
	}

	@Override
	protected int getColor(BlockPos blockPos) {
		return SpecialColour.specialToChromaRGB(NotEnoughUpdates.INSTANCE.config.world.glowingMushroomColor2);
	}

}
