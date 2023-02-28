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
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@NEUAutoSubscribe
public class LavaFishingESP {

	int tick = 0;
	int radius = 0;
	private boolean disabledInSession = false;
	private static final LavaFishingESP INSTANCE = new LavaFishingESP();
	private final Set<BlockPos> highlightBlockList = new HashSet<>();
	private final Set<BlockPos> highlightBlockListCopy = new HashSet<>();
	protected int getColor() {
		return SpecialColour.specialToChromaRGB(NotEnoughUpdates.INSTANCE.config.fishing.lavaColor);
	}
	protected boolean isEnabled() {
		return (NotEnoughUpdates.INSTANCE.config.fishing.lavaESP ) && !disabledInSession;
	}
	protected  boolean inPrecursor(){
		return (SBInfo.getInstance().getLocation() != null && SBInfo.getInstance().getLocation().equals("crystal_hollows") &&
			SBInfo.getInstance().location.equals("Precursor Remnants"));
	}
	@SubscribeEvent
	public void onChatMessage(ClientChatReceivedEvent event) {
		if (event.message.getUnformattedText().equals("A flaming worm surfaces from the depths!")) {
			disabledInSession = true;
			Utils.addChatMessage("Lava ESP disabled");
		}
	}

	@SubscribeEvent
	public void onWorldChange(WorldEvent.Unload event) {
			disabledInSession = false;
	}
	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (isEnabled() && tick % NotEnoughUpdates.INSTANCE.config.fishing.lavaScanInterval == 0) {
			tick = 0;
			if (event.phase != TickEvent.Phase.END ) return; //|| !inPrecursor()) return;

			Thread t = new Thread(() -> {
				 final Set<BlockPos> blockList = Collections.newSetFromMap(new LinkedHashMap<BlockPos, Boolean>() {
					protected boolean removeEldestEntry(Map.Entry<BlockPos, Boolean> eldest) {
						return size() > NotEnoughUpdates.INSTANCE.config.fishing.lavaLimit;
					}
				});

				World w = Minecraft.getMinecraft().theWorld;
				if (w == null) return;
				BlockPos position = Minecraft.getMinecraft().thePlayer.getPosition();
				radius = (int) NotEnoughUpdates.INSTANCE.config.fishing.lavaScanRadius;
				Vec3i vec = new Vec3i(radius, radius, radius);
				blockList.clear();
				for (BlockPos a : BlockPos.getAllInBox(position.add(vec), position.subtract(vec))) {
					Block blockType = w.getBlockState(a).getBlock();
					if ((blockType == Blocks.lava || blockType == Blocks.flowing_lava) && a.getY() > 64) {
						blockList.add(a);
					}
				}
					blockList.removeIf(it -> Minecraft.getMinecraft().thePlayer.getPosition().distanceSq(it) < 12);
				synchronized (highlightBlockList){
					highlightBlockList.clear();
					highlightBlockList.addAll(blockList);
				}
			});
			t.start();
		}
		tick += 1;
	}

	public static LavaFishingESP getInstance() {
		return INSTANCE;
	}

	@SubscribeEvent
	public void onWorldRenderLast(RenderWorldLastEvent event) {
		if (!isEnabled()) return;
		World w = Minecraft.getMinecraft().theWorld;
		if (w == null) return;
		try{
		highlightBlockListCopy.addAll(highlightBlockList);
		for (BlockPos blockPos : highlightBlockListCopy) {
			RenderUtils.renderBoundingBox(blockPos, getColor(), event.partialTicks);
		}}catch (Exception e){
			Utils.addChatMessage(e.getMessage());
		}
		highlightBlockListCopy.clear();

	}
}
