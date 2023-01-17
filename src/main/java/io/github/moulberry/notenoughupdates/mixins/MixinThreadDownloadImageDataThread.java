/*
 * Copyright (C) 2022 Linnea Gräf
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.net.HttpURLConnection;

@Mixin(targets = "net.minecraft.client.renderer.ThreadDownloadImageData$1")
public class MixinThreadDownloadImageDataThread {

	@Inject(
		method = "run",
		at = @At(
			value = "INVOKE",
			target = "Ljava/net/HttpURLConnection;setDoOutput(Z)V"
		),
		locals = LocalCapture.CAPTURE_FAILSOFT
	)
	public void patchHttpConnection(CallbackInfo ci, HttpURLConnection httpURLConnection) {
		ThreadDownloadImageHook.hookThreadImageConnection(httpURLConnection);
	}

}
