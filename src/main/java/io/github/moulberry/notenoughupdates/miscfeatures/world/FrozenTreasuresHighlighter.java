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
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.SpecialColour;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class FrozenTreasuresHighlighter extends GenericBlockHighlighter {

	private static final FrozenTreasuresHighlighter INSTANCE = new FrozenTreasuresHighlighter();

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

	protected static boolean canSee(MovingObjectPosition hitResult, BlockPos bp) {
		return hitResult == null
			|| hitResult.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
			|| bp.equals(hitResult.getBlockPos());
	}
	@SubscribeEvent
	public void onWorldRenderLast(RenderWorldLastEvent event) {
		if (!isEnabled()) return;
		World w = Minecraft.getMinecraft().theWorld;
		if (w == null) return;
		List<Entity> entities = w.getLoadedEntityList();
		for (Entity e : entities) {
			if (!(e instanceof EntityArmorStand) || ((EntityArmorStand) e).getCurrentArmor(3)==null) continue;
			BlockPos blockPos = e.getPosition();
			BlockPos blockPos2 = blockPos.add(0, 1, 0);
			if (tryRegisterInterest(blockPos2)) {
				RenderUtils.renderBoundingBox(blockPos2, getColor(blockPos2), event.partialTicks);
			}
		}
	}
	@Override
	protected int getColor(BlockPos blockPos) {
		return SpecialColour.specialToChromaRGB(NotEnoughUpdates.INSTANCE.config.world.frozenTreasuresColor);
	}
	@Override
	public boolean tryRegisterInterest(double x, double y, double z) {
		BlockPos blockPos = new BlockPos(x, y, z);
		boolean canSee = canPlayerSeeNearBlocks(blockPos.getX(), blockPos.getY(), blockPos.getZ());
		return isValidHighlightSpot(blockPos) && canSee;
	}
	public boolean tryRegisterInterest(BlockPos blockPos) {
		boolean canSee = canPlayerSeeNearBlocks(blockPos.getX(), blockPos.getY(), blockPos.getZ());
		return isValidHighlightSpot(blockPos) && canSee;
	}
}
