/*
 * Copyright (C) 2023 NotEnoughUpdates contributors
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
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.SpecialColour;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@NEUAutoSubscribe
public class CrystalHollowChestHighlighter extends GenericBlockHighlighter {

	// Because ConcurrentModificationException is the bane of me
	public static CopyOnWriteArrayList<BlockPos> markedBlocks = new CopyOnWriteArrayList<>();

	public static void processBlockChangePacket(S23PacketBlockChange packetIn) {
		BlockPos pos = packetIn.getBlockPosition();
		checkForChest(pos, packetIn.blockState);
	}

	public static void processMultiBlockChangePacket(S22PacketMultiBlockChange packetIn) {
		for (S22PacketMultiBlockChange.BlockUpdateData blockChanged : packetIn.getChangedBlocks()) {
			BlockPos pos = blockChanged.getPos();
			checkForChest(pos, blockChanged.getBlockState());
		}
	}

	public static void checkForChest(BlockPos pos, IBlockState blockState) {
		IBlockState oldState = Minecraft.getMinecraft().theWorld.getBlockState(pos);

		if ((oldState.getBlock() == Blocks.air || oldState.getBlock() == Blocks.stone) &&
			blockState.getBlock() == Blocks.chest) {

			// Only add if in a 10x10x10 area. Minimises other players' chests being caught
			if (Minecraft.getMinecraft().thePlayer.getEntityBoundingBox().expand(10, 10, 10).isVecInside(new Vec3(pos))) {
				markedBlocks.add(pos);
			}
		}
	}

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (!isEnabled()) return;

		markedBlocks.forEach(this::tryRegisterInterest);

		// Here to catch chests that get highlighted by other people after they open them, and
		// any highlighted blocks in which the chest despawned in
		List<BlockPos> blockToRemove = new ArrayList<>();
		highlightedBlocks.forEach(it -> {
			if (Minecraft.getMinecraft().theWorld.getBlockState(it).getBlock() != Blocks.chest) {
				blockToRemove.add(it);
			}
		});

		blockToRemove.forEach(highlightedBlocks::remove);
	}

	@SubscribeEvent
	public void onBlockInteraction(PlayerInteractEvent event) {
		if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
			markedBlocks.remove(event.pos);
			highlightedBlocks.remove(event.pos);
		}
	}

	@Override
	public void onWorldRenderLast(RenderWorldLastEvent event) {
		if (!isEnabled()) return;
		World w = Minecraft.getMinecraft().theWorld;
		if (w == null) return;
		for (BlockPos blockPos : highlightedBlocks) {
			RenderUtils.renderBoundingBox(blockPos, getColor(blockPos), event.partialTicks, false);
		}
	}

	@Override
	protected boolean isEnabled() {
		return "crystal_hollows".equals(SBInfo.getInstance().getLocation()) &&
			NotEnoughUpdates.INSTANCE.config.world.highlightCrystalHollowChests;
	}

	@Override
	protected boolean isValidHighlightSpot(BlockPos key) {
		World w = Minecraft.getMinecraft().theWorld;
		if (w == null) return false;
		Block b = w.getBlockState(key).getBlock();
		return b == Blocks.chest;
	}

	@Override
	protected int getColor(BlockPos blockPos) {
		return SpecialColour.specialToChromaRGB(NotEnoughUpdates.INSTANCE.config.world.crystalHollowChestColor);
	}
}
