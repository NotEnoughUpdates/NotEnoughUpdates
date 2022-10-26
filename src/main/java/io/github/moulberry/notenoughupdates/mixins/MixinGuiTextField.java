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
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;

@Mixin(GuiTextField.class)
public abstract class MixinGuiTextField {

	private final ArrayList<String> stringStack = new ArrayList<String>() {
		{
			//allows the ability to undo the entire textField
			add("");
		}
	};
	private int currentStringStackIndex = -1;

	@Shadow private int maxStringLength;

	@Shadow
	private String text;

	@Shadow
	public abstract void setCursorPositionEnd();

	@Shadow
	public abstract void setText(String string);

	private void addToStack(String string) {
		if (currentStringStackIndex != -1 && currentStringStackIndex != stringStack.size() - 1) {
			//did redo/undo before
			stringStack.subList(currentStringStackIndex, stringStack.size()).clear();
		}
		if (stringStack.size() > 20) {
			stringStack.remove(0);
		}
		stringStack.add(string);
		currentStringStackIndex = stringStack.size() - 1;
	}

	@Inject(method = "setText", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT)
	public void setText_stringStack(String string, CallbackInfo ci) {
		if (NotEnoughUpdates.INSTANCE.config.misc.textFieldTweaksEnabled) {
			addToStack(string.length() > this.maxStringLength ? string.substring(0, this.maxStringLength) : string);
		}
	}

	@Inject(method = "writeText", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiTextField;moveCursorBy(I)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
	public void writeText_stringStack(String string, CallbackInfo ci, String string2, String string3, int i, int j, int l) {
		if (NotEnoughUpdates.INSTANCE.config.misc.textFieldTweaksEnabled) {
			addToStack(string2);
		}
	}

	@Inject(method = "deleteFromCursor", at = @At(value = "INVOKE", target = "Lcom/google/common/base/Predicate;apply(Ljava/lang/Object;)Z", remap = false), locals = LocalCapture.CAPTURE_FAILSOFT)
	public void deleteFromCursor_stringStack(int i, CallbackInfo ci, boolean bl, int j, int k, String string) {
		if (NotEnoughUpdates.INSTANCE.config.misc.textFieldTweaksEnabled) {
			addToStack(string);
		}
	}

	@Inject(method = "textboxKeyTyped", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;isKeyComboCtrlA(I)Z"), cancellable = true)
	public void textboxKeyTyped_stringStack(char c, int i, CallbackInfoReturnable<Boolean> cir) {
		if (NotEnoughUpdates.INSTANCE.config.misc.textFieldTweaksEnabled) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
				if (currentStringStackIndex == -1 && stringStack.size() > 0) {
					currentStringStackIndex = stringStack.size() - 1;
				}

				if (Keyboard.isKeyDown(Keyboard.KEY_Y) || (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && Keyboard.isKeyDown(
					Keyboard.KEY_Z))) {
					//go forward in action stack
					if (currentStringStackIndex != stringStack.size() - 1) {
						text = stringStack.get(currentStringStackIndex + 1);
						currentStringStackIndex += 1;
						setCursorPositionEnd();
					}
					cir.setReturnValue(true);
				} else if (Keyboard.isKeyDown(Keyboard.KEY_Z)) {
					//go back in action stack
					if (!stringStack.isEmpty() && currentStringStackIndex > 0 && stringStack.get(currentStringStackIndex - 1) !=
						null) {
						text = stringStack.get(currentStringStackIndex - 1);
						currentStringStackIndex -= 1;
						setCursorPositionEnd();
					}
					cir.setReturnValue(true);
				}
			}
		}
	}

	@Inject(method = "setMaxStringLength", at = @At(value = "INVOKE", target = "Ljava/lang/String;length()I"))
	public void setMaxStringLength_stringStack(int i, CallbackInfo ci) {
		//will remove the possibility of creating a bug in the matrix (untested)
		if (NotEnoughUpdates.INSTANCE.config.misc.textFieldTweaksEnabled) {
			for (int j = 0; j < stringStack.size(); j++) {
				String string = stringStack.get(j);
				if (string.length() > i) {
					stringStack.set(j, string.substring(0, j));
					if (j < currentStringStackIndex) {
						currentStringStackIndex -= 1;
					}
				}
			}
		}
	}
}
