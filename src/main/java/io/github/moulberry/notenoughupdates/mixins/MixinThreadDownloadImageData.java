/*
 * Copyright (C) 2022-2023 Linnea Gräf
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

import io.github.moulberry.notenoughupdates.hooks.ThreadDownloadImageHook;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ThreadDownloadImageData.class)
public class MixinThreadDownloadImageData implements ThreadDownloadImageHook.AccessorThreadDownloadImageData{
	@Mutable
	@Shadow
	@Final
	private String imageUrl;

	private String originalUrl;

	@Redirect(
		method = "<init>",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/renderer/ThreadDownloadImageData;imageUrl:Ljava/lang/String;",
			opcode = Opcodes.PUTFIELD))
	public void useHttpsDownloadLinks(ThreadDownloadImageData instance, String value) {
		this.imageUrl = ThreadDownloadImageHook.hookThreadImageLink(value);
		this.originalUrl = value;
	}

	@Override
	public String getOriginalUrl() {
		return originalUrl;
	}

	@Override
	public String getPatchedUrl() {
		return imageUrl;
	}
}
