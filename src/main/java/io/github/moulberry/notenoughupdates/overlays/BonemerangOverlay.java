/*
 * Copyright (C) 2022-2023 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.overlays;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.config.Position;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class BonemerangOverlay extends TextOverlay {
	public BonemerangOverlay(
		Position position,
		Supplier<List<String>> dummyStrings,
		Supplier<TextOverlayStyle> styleSupplier
	) {
		super(position, dummyStrings, styleSupplier);
		INSTANCE = this;
	}

	public static BonemerangOverlay INSTANCE;

	public final Set<EntityLivingBase> bonemeragedEntities = new HashSet<>();

	@Override
	public boolean isEnabled() {
		return NotEnoughUpdates.INSTANCE.config.itemOverlays.enableBonemerangOverlay;
	}

	@Override
	public void updateFrequent() {
		if (NotEnoughUpdates.INSTANCE.config.itemOverlays.bonemerangFastUpdate) {
			updateOverlay();
		}
	}

	@Override
	public void update() {
		if (!NotEnoughUpdates.INSTANCE.config.itemOverlays.bonemerangFastUpdate) {
			updateOverlay();
		}
	}

	private void updateOverlay() {
		if (!isEnabled() &&
			NotEnoughUpdates.INSTANCE.config.itemOverlays.highlightTargeted) {
			overlayStrings = null;
			return;
		}
		overlayStrings = new ArrayList<>();

		bonemeragedEntities.clear();
		if (Minecraft.getMinecraft().thePlayer == null) return;
		if (Minecraft.getMinecraft().theWorld == null) return;

		ItemStack held = Minecraft.getMinecraft().thePlayer.getHeldItem();

		String internal = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(held);

		if (internal != null && internal.equals("BONE_BOOMERANG")) {
			HashMap<Integer, String> map = new HashMap<>();

			EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
			float stepSize = 0.15f;
			float bonemerangDistance = 15;

			Vector3f position = new Vector3f((float) p.posX, (float) p.posY + p.getEyeHeight(), (float) p.posZ);
			Vec3 look = p.getLook(0);

			Vector3f step = new Vector3f((float) look.xCoord, (float) look.yCoord, (float) look.zCoord);
			step.scale(stepSize / step.length());

			for (int i = 0; i < Math.floor(bonemerangDistance / stepSize) - 2; i++) {
				AxisAlignedBB bb = new AxisAlignedBB(position.x - 0.75f, position.y - 0.1, position.z - 0.75f,
					position.x + 0.75f, position.y + 0.25, position.z + 0.75
				);

				BlockPos blockPos = new BlockPos(position.x, position.y, position.z);

				if (!Minecraft.getMinecraft().theWorld.isAirBlock(blockPos) &&
					Minecraft.getMinecraft().theWorld.getBlockState(blockPos).getBlock().isFullCube()) {
					map.put(0, EnumChatFormatting.RED + "Bonemerang will break!");
					break;
				}

				List<Entity> entities = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABBExcludingEntity(
					Minecraft.getMinecraft().thePlayer,
					bb
				);
				for (Entity entity : entities) {
					if (entity instanceof EntityLivingBase && !(entity instanceof EntityArmorStand) && !entity.isInvisible()) {
						if (!bonemeragedEntities.contains(entity)) {
							bonemeragedEntities.add((EntityLivingBase) entity);
						}
					}
				}

				position.translate(step.x, step.y, step.z);
			}
			if (NotEnoughUpdates.INSTANCE.config.itemOverlays.enableBonemerangOverlay) {
				map.put(
					1,
					EnumChatFormatting.GRAY + "Targets: " + EnumChatFormatting.GOLD + EnumChatFormatting.BOLD +
						bonemeragedEntities.size()
				);
				for (int index : NotEnoughUpdates.INSTANCE.config.itemOverlays.bonemerangOverlayText) {
					if (map.containsKey(index)) {
						overlayStrings.add(map.get(index));
					}
				}
			}

		}

		if (overlayStrings.isEmpty()) overlayStrings = null;
	}
}
