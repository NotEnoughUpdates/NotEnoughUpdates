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
		highlightedBlocks.removeIf(it -> !isValidHighlightSpot(it) ||
			!canPlayerSeeNearBlocks(it.getX(), it.getY(), it.getZ()));
	}

	protected boolean canPlayerSeeBlock(double xCoord, double yCoord, double zCoord) {
		EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
		if (p == null) return false;
		Vec3 playerPosition = new Vec3(p.posX, p.posY + p.eyeHeight, p.posZ);
		MovingObjectPosition hitResult = rayTraceBlocks(p.worldObj, playerPosition, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5);
		return canSee(hitResult, new BlockPos(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5));
	}

	protected boolean canPlayerSeeNearBlocks(double x, double y, double z) {
		EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
		if (p == null) return false;
		World world = p.worldObj;
		Vec3 playerPosition = new Vec3(p.posX, p.posY + p.eyeHeight, p.posZ);
		BlockPos blockPos = new BlockPos(x, y, z);
		MovingObjectPosition hitResult1 = rayTraceBlocks(world, playerPosition, x, y, z);
		if (canSee(hitResult1, blockPos)) return true;
		MovingObjectPosition hitResult2 = rayTraceBlocks(world, playerPosition, x + 1, y, z);
		if (canSee(hitResult2, blockPos.add(1, 0, 1))) return true;
		MovingObjectPosition hitResult3 = rayTraceBlocks(world, playerPosition, x + 1, y + 1, z);
		if (canSee(hitResult3, blockPos.add(1, 1, 0))) return true;
		MovingObjectPosition hitResult4 = rayTraceBlocks(world, playerPosition, x + 1, y + 1, z + 1);
		if (canSee(hitResult4, blockPos.add(1, 1, 1))) return true;

		MovingObjectPosition hitResult5 = rayTraceBlocks(world, playerPosition, x, y + 1, z + 1);
		if (canSee(hitResult5, blockPos.add(0, 1, 1))) return true;
		MovingObjectPosition hitResult6 = rayTraceBlocks(world, playerPosition, x, y + 1, z);
		if (canSee(hitResult6, blockPos.add(0, 1, 0))) return true;
		MovingObjectPosition hitResult7 = rayTraceBlocks(world, playerPosition, x + 1, y, z + 1);
		if (canSee(hitResult7, blockPos.add(1, 0, 1))) return true;
		MovingObjectPosition hitResult8 = rayTraceBlocks(world, playerPosition, x, y + 1, z);
		if (canSee(hitResult8, blockPos.add(0, 1, 0))) return true;

		return false;
	}

	private static boolean canSee(MovingObjectPosition hitResult, BlockPos bp) {
		return hitResult == null
			|| hitResult.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
			|| bp.equals(hitResult.getBlockPos());
	}

	private static MovingObjectPosition rayTraceBlocks(World world, Vec3 playerPosition, double x, double y, double z) {
		return world.rayTraceBlocks(playerPosition, new Vec3(x, y, z), false, true, true);
	}

	@SubscribeEvent
	public void onWorldChange(WorldEvent.Load event) {
		highlightedBlocks.clear();
	}

	public boolean tryRegisterInterest(double x, double y, double z) {
		BlockPos blockPos = new BlockPos(x, y, z);
		boolean contains = highlightedBlocks.contains(blockPos);
		if (!contains) {
			boolean canSee = canPlayerSeeNearBlocks(blockPos.getX(), blockPos.getY(), blockPos.getZ());
			if (isValidHighlightSpot(blockPos) && canSee) {
				highlightedBlocks.add(blockPos);
			}
		}
		return contains;
	}
}
