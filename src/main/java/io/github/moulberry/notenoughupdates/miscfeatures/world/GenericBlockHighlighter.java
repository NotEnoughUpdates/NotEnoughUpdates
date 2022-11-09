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

import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashSet;
import java.util.Set;

public abstract class GenericBlockHighlighter {
	protected abstract boolean isEnabled();

	protected abstract boolean isValidHighlightSpot(BlockPos key);

	protected abstract int getColor(BlockPos blockPos);

	public final Set<BlockPos> highlightedBlocks = new HashSet<>();

	@SubscribeEvent
	public void onWorldRenderLast(RenderWorldLastEvent event) {
		if (!isEnabled()) return;
		World w = Minecraft.getMinecraft().theWorld;
		if (w == null) return;
		for (BlockPos blockPos : highlightedBlocks) {
			RenderUtils.renderBoundingBox(blockPos, getColor(blockPos), event.partialTicks);
		}
	}

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent ev) {
		if (ev.phase != TickEvent.Phase.END) return;
		highlightedBlocks.removeIf(it -> !isValidHighlightSpot(it) || !canPlayerSeeBlock(it.getX(), it.getY(), it.getZ()));
	}

	protected boolean canPlayerSeeBlock(double xCoord, double yCoord, double zCoord) {
		EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
		if (p == null) return false;
		World w = p.worldObj;
		MovingObjectPosition hitResult = w.rayTraceBlocks(
			new Vec3(p.posX, p.posY + p.eyeHeight, p.posZ),
			new Vec3(xCoord, yCoord, zCoord),
			false,
			true,
			true
		);
		BlockPos bp = new BlockPos(xCoord, yCoord, zCoord);
		return hitResult == null
			|| hitResult.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
			|| bp.equals(hitResult.getBlockPos());
	}

	@SubscribeEvent
	public void onWorldChange(WorldEvent.Load event) {
		highlightedBlocks.clear();
	}

	public void registerInterest(BlockPos pos) {
		if (isValidHighlightSpot(pos) && canPlayerSeeBlock(pos.getX(), pos.getY(), pos.getZ())) {
			highlightedBlocks.add(pos);
		}
	}
}
