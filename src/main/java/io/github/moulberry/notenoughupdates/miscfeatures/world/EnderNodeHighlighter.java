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

import static io.github.moulberry.notenoughupdates.util.MathUtil.basicallyEqual;

@NEUAutoSubscribe
public class EnderNodeHighlighter extends GenericBlockHighlighter {

	private static final EnderNodeHighlighter INSTANCE = new EnderNodeHighlighter();

	public static EnderNodeHighlighter getInstance()
	{
		return INSTANCE;
	}


	@SubscribeEvent
	public void onParticleSpawn(SpawnParticleEvent event) {
		if (!isEnabled()) return;
		if (event.getParticleTypes() == EnumParticleTypes.PORTAL) {
			double x = event.getXCoord();
			double y = event.getYCoord();
			double z = event.getZCoord();

			boolean xZero = basicallyEqual((x - 0.5) % 1, 0, 0.2);
			boolean yZero = basicallyEqual((y - 0.5) % 1, 0, 0.2);
			boolean zZero = basicallyEqual((z - 0.5) % 1, 0, 0.2);

			if (Math.abs(y % 1) == 0.25 && xZero && zZero) {
				if (tryRegisterInterest(x, y - 1, z)) return;
			}
			if (Math.abs(y % 1) == 0.75 && xZero && zZero) {
				if (tryRegisterInterest(x, y + 1, z)) return;
			}
			if (Math.abs(x % 1) == 0.25 && yZero && zZero) {
				if (tryRegisterInterest(x + 1, y, z)) return;
			}
			if (Math.abs(x % 1) == 0.75 && yZero && zZero) {
				if (tryRegisterInterest(x - 1, y, z)) return;
			}
			if (Math.abs(z % 1) == 0.25 && yZero && xZero) {
				if (tryRegisterInterest(x, y, z + 1)) return;
			}
			if (Math.abs(z % 1) == 0.75 && yZero && xZero) {
				tryRegisterInterest(x, y, z - 1);
			}
		}
	}

	@Override
	protected boolean isEnabled() {
		return "combat_3".equals(SBInfo.getInstance().getLocation())
			&& NotEnoughUpdates.INSTANCE.config.world.highlightEnderNodes;
	}

	@Override
	protected boolean isValidHighlightSpot(BlockPos key) {
		World w = Minecraft.getMinecraft().theWorld;
		if (w == null) return false;
		Block b = w.getBlockState(key).getBlock();
		return b == Blocks.end_stone || b == Blocks.obsidian;
	}

	@Override
	protected int getColor(BlockPos blockPos) {
		return SpecialColour.specialToChromaRGB(NotEnoughUpdates.INSTANCE.config.world.enderNodeColor2);
	}
}
