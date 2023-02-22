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

import io.github.moulberry.notenoughupdates.core.ChromaColour;
import io.github.moulberry.notenoughupdates.miscfeatures.ItemCustomizeManager;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin({ItemStack.class})
public class MixinItemStack {
	@Inject(method = "hasEffect", at = @At("HEAD"), cancellable = true)
	public void hasEffect(CallbackInfoReturnable<Boolean> cir) {
		if (Utils.getHasEffectOverride()) {
			cir.setReturnValue(false);
			return;
		}
	}

	@Shadow
	private NBTTagCompound stackTagCompound;

	@Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
	public void getDisplayName(CallbackInfoReturnable<String> returnable) {
		try {
			if (stackTagCompound == null || !stackTagCompound.hasKey("ExtraAttributes", 10)) {
				return;
			}

			ItemCustomizeManager.ItemData data = ItemCustomizeManager.getDataForItem((ItemStack) (Object) this);

			if (data != null && data.customName != null) {
				String customName = data.customName;
				if (customName != null && !customName.equals("")) {
					customName = Utils.chromaStringByColourCode(customName);

					if (data.customNamePrefix != null) {
						customName = data.customNamePrefix + customName;
					}

					returnable.setReturnValue(customName);
				}
			}
		} catch (Exception ignored) {
		}
	}

	@Redirect(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NBTTagCompound;hasKey(Ljava/lang/String;I)Z", ordinal = 2))
	public boolean getTooltip_hasKey2(NBTTagCompound nbttagcompound, String key, int type) {
		ItemStack stack = (ItemStack) (Object) this;
		ItemCustomizeManager.ItemData data = ItemCustomizeManager.getDataForItem(stack);
		if (data != null && data.customLeatherColour != null && ItemCustomizeManager.shouldRenderLeatherColour(stack)) {
			return true;
		} else if (data != null && !ItemCustomizeManager.shouldRenderLeatherColour(stack)) {
			return false;
		}
		return nbttagcompound.hasKey("color", 3);
		}

	@Redirect(method = "getTooltip", at = @At(value = "INVOKE", target = "Ljava/lang/Integer;toHexString(I)Ljava/lang/String;"))
	public String getTooltip_toHexString(int colour) {
		ItemStack stack = (ItemStack) (Object) this;
		ItemCustomizeManager.ItemData data = ItemCustomizeManager.getDataForItem(stack);
		if (data != null && data.customLeatherColour != null && ItemCustomizeManager.shouldRenderLeatherColour(stack)) {
			int currentColour = ChromaColour.specialToChromaRGB(data.customLeatherColour);
			Color c = new Color(currentColour, false);
			String hex = Integer.toHexString(c.getRGB() & 0xFFFFFF);
			if (hex.length() < 6) {
				hex = hex + "000000".substring(0, 6 - hex.length());
			}
			return hex.length() < 6 ? "0" + hex : hex;
		}
		return Integer.toHexString(colour);
	}
}
