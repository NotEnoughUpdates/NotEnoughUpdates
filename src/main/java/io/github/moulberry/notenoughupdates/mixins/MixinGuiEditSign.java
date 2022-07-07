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

import io.github.moulberry.notenoughupdates.events.SignSubmitEvent;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiEditSign.class)
public class MixinGuiEditSign {

	@Redirect(method = "onGuiClosed", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/tileentity/TileEntitySign;signText:[Lnet/minecraft/util/IChatComponent;"))
	public IChatComponent[] onOnGuiClosed(TileEntitySign instance) {
		String[] x = new String[4];
		for (int i = 0; i < 4; i++) {
			x[i] = instance.signText[i].getUnformattedText();
		}
		SignSubmitEvent signSubmitEvent = new SignSubmitEvent((GuiEditSign) (Object) this, x);
		signSubmitEvent.post();
		IChatComponent[] arr = new IChatComponent[4];
		for (int i = 0; i < 4; i++) {
			arr[i] = new ChatComponentText(signSubmitEvent.lines[i]);
		}
		return arr;
	}

}
