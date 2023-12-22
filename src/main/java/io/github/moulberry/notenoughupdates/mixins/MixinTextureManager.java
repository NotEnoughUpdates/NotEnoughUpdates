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

import com.google.common.collect.Lists;
import io.github.moulberry.notenoughupdates.miscfeatures.ItemCustomizeManager;
import io.github.moulberry.notenoughupdates.profileviewer.BasicPage;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

@Mixin(TextureManager.class)
public class MixinTextureManager {

	@Mutable
	@Final
	@Shadow
	private List<ITickable> listTickables;

	@Inject(method = "bindTexture", at = @At("HEAD"), cancellable = true)
	public void bindTexture(ResourceLocation location, CallbackInfo ci) {
		if (ItemCustomizeManager.disableTextureBinding) {
			ci.cancel();
		}
	}

	/**
	 Fixes ConcurrentModificationException in TextureManager#tick
	 This is caused by NEU loading a player asynchronously which can cause issues with other mods (5zig in this case)
	 @see BasicPage#drawPage at profileLoader.submit()
	 */
	@Inject(method = "<init>", at = @At("RETURN"))
	public void constructor(CallbackInfo ci) {
		listTickables = Collections.synchronizedList(Lists.newArrayList());
	}
}
