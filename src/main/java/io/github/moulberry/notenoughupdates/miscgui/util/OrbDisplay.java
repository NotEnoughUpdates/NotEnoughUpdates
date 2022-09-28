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

package io.github.moulberry.notenoughupdates.miscgui.util;

import io.github.moulberry.notenoughupdates.core.util.lerp.LerpUtils;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

public class OrbDisplay {
	private static final ResourceLocation TEXTURE = new ResourceLocation("notenoughupdates:custom_enchant_gui.png");
	private static final int DEFAULT_COUNT = 30;

	private final List<ExperienceOrb> experienceOrbList = new ArrayList<>();

	public ExperienceOrb spawnExperienceOrb(Random random, Vector2f start, Vector2f target, int baseType) {
		ExperienceOrb orb = new ExperienceOrb();
		orb.position = new Vector2f(start);
		orb.positionLast = new Vector2f(orb.position);
		orb.velocity = new Vector2f(
			random.nextFloat() * 20 - 10,
			random.nextFloat() * 20 - 10
		);
		orb.target = new Vector2f(target);
		orb.type = baseType;
		orb.rotationDeg = random.nextInt(4) * 90;

		float v = random.nextFloat();
		if (v > 0.6) {
			orb.type += 1;
		}
		if (v > 0.9) {
			orb.type += 1;
		}

		experienceOrbList.add(orb);

		return orb;
	}

	public void spawnExperienceOrbs(int startX, int startY, int targetX, int targetY, int baseType) {
		spawnExperienceOrbs(new Random(),new Vector2f(startX, startY), new Vector2f(targetX, targetY), baseType, DEFAULT_COUNT);
	}

	public void spawnExperienceOrbs(Random random, Vector2f start, Vector2f target, int baseType, int count) {
		for (int i = 0; i < count; i++) {
			spawnExperienceOrb(random, start, target, baseType);
		}
	}

	public void physicsTickOrbs() {
		for (ListIterator<ExperienceOrb> it = experienceOrbList.listIterator(); it.hasNext(); ) {
			ExperienceOrb orb = it.next();

			Vector2f delta = Vector2f.sub(orb.target, orb.position, null);
			float length = delta.length();

			// Remove close Orbs
			if (length < 8 && orb.velocity.lengthSquared() < 20) {
				it.remove();
				continue;
			}

			// Update velocity
			Vector2f.add(orb.velocity, (Vector2f) delta.scale(2 / length), orb.velocity);
			orb.velocity.scale(0.9F);

			// Update position
			orb.positionLast.set(orb.position);
			Vector2f.add(orb.position, orb.velocity, orb.position);
		}
	}

	public void renderOrbs(float partialTicks) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
		GlStateManager.disableDepth();

		for (ExperienceOrb orb : experienceOrbList) {
			int orbX = Math.round(LerpUtils.lerp(orb.position.x, orb.positionLast.x, partialTicks));
			int orbY = Math.round(LerpUtils.lerp(orb.position.y, orb.positionLast.y, partialTicks));

			GlStateManager.pushMatrix();

			GlStateManager.translate(orbX, orbY, 0);
			GlStateManager.rotate(orb.rotationDeg, 0, 0, 1);

			Vector2f delta = Vector2f.sub(orb.position, orb.target, null);

			float length = delta.length();
			float velocitySquared = orb.velocity.lengthSquared();
			float opacity = (float) Math.sqrt(
				Math.min(
					1,
					Math.min(2, Math.max(0.5F, length / 16))
						* Math.min(2, Math.max(0.5F, velocitySquared / 40))
				));
			GlStateManager.color(1, 1, 1, opacity);

			int orbU = (orb.type % 3) * 16;
			int orbV = (orb.type / 3) * 16 + 217;

			Utils.drawTexturedRect(
				-8, -8, 16, 16,
				orbU / 512f,
				(orbU + 16) / 512f,
				orbV / 512f,
				(orbV + 16) / 512f,
				GL11.GL_NEAREST
			);

			GlStateManager.popMatrix();
		}

		GlStateManager.enableDepth();
	}

}
