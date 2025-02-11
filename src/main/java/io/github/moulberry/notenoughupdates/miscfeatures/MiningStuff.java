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

package io.github.moulberry.notenoughupdates.miscfeatures;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.core.util.render.TextRenderUtils;
import io.github.moulberry.notenoughupdates.overlays.MiningOverlay;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.SpecialColour;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;

@NEUAutoSubscribe
public class MiningStuff {
	private static BlockPos overlayLoc = null;
	private static Minecraft mc;

	public MiningStuff() {
		mc = Minecraft.getMinecraft();
	}

	private static BlockPos[] getAttachedBlocks(BlockPos block) {
		BlockPos[] blocks = new BlockPos[6];
		blocks[0] = new BlockPos(block.getX() - 1, block.getY(), block.getZ());
		blocks[1] = new BlockPos(block.getX() + 1, block.getY(), block.getZ());
		blocks[2] = new BlockPos(block.getX(), block.getY() - 1, block.getZ());
		blocks[3] = new BlockPos(block.getX(), block.getY() + 1, block.getZ());
		blocks[4] = new BlockPos(block.getX(), block.getY(), block.getZ() - 1);
		blocks[5] = new BlockPos(block.getX(), block.getY(), block.getZ() + 1);
		return blocks;
	}

	private static boolean checkIfAnyIsAir(BlockPos[] blocks) {
		for (BlockPos block : blocks) {
			if (mc.theWorld.getBlockState(block).getBlock() instanceof BlockAir) {
				return true;
			}
		}
		return false;
	}

	@SubscribeEvent
	public void renderWorldLast(RenderWorldLastEvent event) {
		if (overlayLoc == null) return;

		Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
		double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.partialTicks;
		double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.partialTicks;
		double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks;

		AxisAlignedBB bb = new AxisAlignedBB(
			overlayLoc.getX() - viewerX,
			overlayLoc.getY() - viewerY,
			overlayLoc.getZ() - viewerZ,
			overlayLoc.getX() + 1 - viewerX,
			overlayLoc.getY() + 1 - viewerY,
			overlayLoc.getZ() + 1 - viewerZ
		).expand(0.01f, 0.01f, 0.01f);

		GlStateManager.disableCull();
		CustomItemEffects.drawFilledBoundingBox(bb, 1f, SpecialColour.special(0, 100, 0xff0000));
		GlStateManager.enableCull();
		GlStateManager.enableTexture2D();
	}

	@SubscribeEvent
	public void onLoadWorld(WorldEvent.Load event) {
		overlayLoc = null;
	}

	@SubscribeEvent
	public void onChatReceived(ClientChatReceivedEvent event) {
		if (!NotEnoughUpdates.INSTANCE.config.mining.puzzlerSolver) {
			overlayLoc = null;
			return;
		}

		if (event.message.getFormattedText().startsWith("\u00A7e[NPC] \u00A7dPuzzler") &&
			event.message.getUnformattedText().contains(":")) {
			String clean = Utils.cleanColour(event.message.getUnformattedText());
			clean = clean.split(":")[1].trim();

			BlockPos pos = new BlockPos(181, 195, 135);

			for (int i = 0; i < clean.length(); i++) {
				char c = clean.charAt(i);

				if (c == '\u25C0') { //Left
					pos = pos.add(1, 0, 0);
				} else if (c == '\u25B2') { //Up
					pos = pos.add(0, 0, 1);
				} else if (c == '\u25BC') { //Down
					pos = pos.add(0, 0, -1);
				} else if (c == '\u25B6') { //Right
					pos = pos.add(-1, 0, 0);
				} else {
					return;
				}
			}

			overlayLoc = pos;
		}
	}
}
