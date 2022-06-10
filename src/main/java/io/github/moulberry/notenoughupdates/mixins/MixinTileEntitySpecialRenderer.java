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

package io.github.moulberry.notenoughupdates.mixins;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.dungeons.DungeonBlocks;
import io.github.moulberry.notenoughupdates.util.SpecialColour;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({TileEntitySpecialRenderer.class})
public abstract class MixinTileEntitySpecialRenderer {
	@Inject(method = "bindTexture", at = @At("HEAD"), cancellable = true)
	public void bindTexture(ResourceLocation location, CallbackInfo info) {
		if (DungeonBlocks.isOverriding()) {
			if (location.getResourcePath().equals("textures/entity/chest/normal.png") ||
				location.getResourcePath().equals("textures/entity/chest/normal_double.png") ||
				location.getResourcePath().equals("textures/entity/chest/trapped.png") ||
				location.getResourcePath().equals("textures/entity/chest/trapped_double.png")) {
				String colour = location.getResourcePath().contains("trapped")
					? NotEnoughUpdates.INSTANCE.config.dungeons.dungTrappedChestColour
					:
						NotEnoughUpdates.INSTANCE.config.dungeons.dungChestColour;
				if (DungeonBlocks.bindModifiedTexture(
					location,
					SpecialColour.specialToChromaRGB(colour)
				)) {
					info.cancel();
					GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_LINEAR);
					GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
				}
			}
		}
	}
}
