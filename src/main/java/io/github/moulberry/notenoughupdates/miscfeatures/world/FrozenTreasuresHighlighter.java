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
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FrozenTreasuresHighlighter extends GenericBlockHighlighter {

	private static final FrozenTreasuresHighlighter INSTANCE = new FrozenTreasuresHighlighter();

	public static FrozenTreasuresHighlighter getInstance() {return INSTANCE;}

	private final Set<Entity> highlightedTreasures = new HashSet<>();

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
	public void onTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END||!isEnabled()) return;
		List<Entity> entities = Minecraft.getMinecraft().theWorld.getLoadedEntityList();
		highlightedTreasures.removeIf(it -> !(it instanceof EntityArmorStand) || ((EntityArmorStand) it).getCurrentArmor(3)==null);
		for (Entity e : entities) {
			if ((e instanceof EntityArmorStand) && ((EntityArmorStand) e).getCurrentArmor(3)!=null) highlightedTreasures.add(e);
		}
	}
	@SubscribeEvent
	public void onWorldRenderLast(RenderWorldLastEvent event) {
		if (!isEnabled()) return;
		World w = Minecraft.getMinecraft().theWorld;
		if (w == null) return;
		for (Entity e : highlightedTreasures) {
			BlockPos blockPos = e.getPosition().add(0, 1, 0);
			if (tryRegisterInterest(blockPos)) {
				RenderUtils.renderBoundingBox(blockPos, getColor(blockPos), event.partialTicks);
			}
		}
	}

	@Override
	protected int getColor(BlockPos blockPos) {
		return SpecialColour.specialToChromaRGB(NotEnoughUpdates.INSTANCE.config.world.frozenTreasuresColor);
	}

	public boolean tryRegisterInterest(BlockPos blockPos) {
		boolean canSee = canPlayerSeeNearBlocks(blockPos.getX(), blockPos.getY(), blockPos.getZ());
		return isValidHighlightSpot(blockPos) && canSee;
	}
}
