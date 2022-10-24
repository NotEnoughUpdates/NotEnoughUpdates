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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GuiChat.class)
public class MixinGuiChat {
	@Inject(method = "keyTyped", at=@At(value="INVOKE", target= "Lnet/minecraft/client/gui/GuiChat;sendChatMessage(Ljava/lang/String;)V"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
	public void messageSend(char typedChar, int keyCode, CallbackInfo ci, String message) {
		String lowMessage = message.toLowerCase();
		if (lowMessage.matches("/hypixelcommand:.*") && NotEnoughUpdates.INSTANCE.config.hidden.blockhypixelcommandPrefixedCommands) {
			String m = lowMessage.split(":")[1];
			ChatComponentText warning = new ChatComponentText("§e[NotEnoughUpdates] §7You just executed §c" + message + "§7. Use §e/" + m + "§7 Like a civilised individual");
			warning.setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/"+m)));
			Minecraft.getMinecraft().thePlayer.addChatMessage(warning);
			//close chat and cancel the message send.
			Minecraft.getMinecraft().displayGuiScreen(null);
			ci.cancel();
		}
	}
}
