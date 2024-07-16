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

import net.minecraft.util.ChatComponentStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatComponentStyle.class)
public class MixinChatComponentStyle {

	@Inject(method = "getFormattedText", at = @At("HEAD"), cancellable = true)
	public void getFormattedText(CallbackInfoReturnable<String> cir) {
		StringBuilder stringbuilder = new StringBuilder();

		for (IChatComponent ichatcomponent : (ChatComponentStyle) (Object) this) {
			if (ichatcomponent == null) continue;
			if (ichatcomponent.getChatStyle() == null) continue;
			if (ichatcomponent.getChatStyle().getFormattingCode() == null) continue;
			if (ichatcomponent.getUnformattedTextForChat() == null) continue;
			stringbuilder.append(ichatcomponent.getChatStyle().getFormattingCode());
			stringbuilder.append(ichatcomponent.getUnformattedTextForChat());
			stringbuilder.append(EnumChatFormatting.RESET);
		}

		cir.setReturnValue(stringbuilder.toString());
	}
}
