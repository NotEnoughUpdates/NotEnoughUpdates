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
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.SpecialColour;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;

@NEUAutoSubscribe
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

	@SubscribeEvent
	public void onTickNew(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END || !isEnabled()) return;
		World w = Minecraft.getMinecraft().theWorld;
		if (w == null) return;
		List<Entity> entities = w.getLoadedEntityList();
		for (Entity e : entities) {
			if ((e instanceof EntityArmorStand) && ((EntityArmorStand) e).getCurrentArmor(3) != null) highlightedBlocks.add(e
				.getPosition()
				.add(0, 1, 0));
		}
	}

	@Override
	protected int getColor(BlockPos blockPos) {
		return SpecialColour.specialToChromaRGB(NotEnoughUpdates.INSTANCE.config.world.frozenTreasuresColor);
	}
}
