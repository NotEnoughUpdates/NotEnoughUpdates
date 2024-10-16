/*
 * Copyright (C) 2024 NotEnoughUpdates contributors
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

import io.github.moulberry.notenoughupdates.util.ApiUtil;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.net.ssl.HttpsURLConnection;

@Pseudo
@Mixin(targets = "at.hannibal2.skyhanni.utils.APIUtils")
public class MixinSkyhanniUpdaterFix {


	@Inject(method = "patchHttpsRequest", at = @At("HEAD"), cancellable = true)
	public void run(HttpsURLConnection connection, CallbackInfo ci) {
		ModContainer skyhanni = Loader.instance().getIndexedModList().get("skyhanni");
		String version = skyhanni.getVersion();
		if ("0.27.Beta.12".equals(version) || "0.27.Beta.13".equals(version)) {
			ApiUtil.patchHttpsRequest(connection);
			ci.cancel();
		}
	}
}
