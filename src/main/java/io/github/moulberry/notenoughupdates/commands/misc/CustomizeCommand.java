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

package io.github.moulberry.notenoughupdates.commands.misc;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import io.github.moulberry.notenoughupdates.miscgui.GuiItemCustomize;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;

import java.util.Collections;
import java.util.List;

public class CustomizeCommand extends ClientCommandBase {

	public CustomizeCommand() {
		super("neucustomize");
	}

	@Override
	public List<String> getCommandAliases() {
		return Collections.singletonList("neurename");
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		ItemStack held = Minecraft.getMinecraft().thePlayer.getHeldItem();

		if (held == null) {
			sender.addChatMessage(new ChatComponentText("\u00a7cYou can't customize your hand..."));
			return;
		}

		String heldUUID = NotEnoughUpdates.INSTANCE.manager.getUUIDForItem(held);

		if (heldUUID == null) {
			sender.addChatMessage(new ChatComponentText("\u00a7cHeld item does not have UUID, cannot be customized"));
			return;
		}

		NotEnoughUpdates.INSTANCE.openGui = new GuiItemCustomize(held, heldUUID);
	}

    /*SimpleCommand itemRenameCommand = new SimpleCommand("neurename", new SimpleCommand.ProcessCommandRunnable() {
        public void processCommand(ICommandSender sender, String[] args) {
            if (args.length == 0) {
                args = new String[]{"help"};
            }
            String heldUUID = NotEnoughUpdates.INSTANCE.manager.getUUIDForItem(Minecraft.getMinecraft().thePlayer.getHeldItem());
            switch (args[0].toLowerCase()) {
                case "clearall":
                    NotEnoughUpdates.INSTANCE.manager.itemRenameJson = new JsonObject();
                    NotEnoughUpdates.INSTANCE.manager.saveItemRenameConfig();
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "[NEU] Cleared custom name for all items"));
                    break;
                case "clear":
                    if (heldUUID == null) {
                        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[NEU] Can't clear rename - no UUID"));
                        return;
                    }
                    NotEnoughUpdates.INSTANCE.manager.itemRenameJson.remove(heldUUID);
                    NotEnoughUpdates.INSTANCE.manager.saveItemRenameConfig();
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "[NEU] Cleared custom name for held item"));
                    break;
                case "copyuuid":
                    if (heldUUID == null) {
                        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[NEU] Can't clear rename - no UUID"));
                        return;
                    }
                    StringSelection selection = new StringSelection(heldUUID);
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "[NEU] UUID copied to clipboard"));
                    break;
                case "uuid":
                    if (heldUUID == null) {
                        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[NEU] Can't get UUID - no UUID"));
                        return;
                    }
                    ChatStyle style = new ChatStyle();
                    style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ChatComponentText(EnumChatFormatting.GRAY + "Click to copy to clipboard")));
                    style.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/neurename copyuuid"));

                    ChatComponentText text = new ChatComponentText(EnumChatFormatting.YELLOW + "[NEU] The UUID of your currently held item is: " +
                            EnumChatFormatting.GREEN + heldUUID);
                    text.setChatStyle(style);
                    sender.addChatMessage(text);
                    break;
                case "set":
                    if (heldUUID == null) {
                        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[NEU] Can't rename item - no UUID"));
                        return;
                    }
                    if (args.length == 1) {
                        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[NEU] Usage: /neurename set [name...]"));
                        return;
                    }
                    StringBuilder sb = new StringBuilder();
                    for (int i = 1; i < args.length; i++) {
                        sb.append(args[i]);
                        if (i < args.length - 1) sb.append(" ");
                    }
                    String name = sb.toString()
                            .replace("\\&", "{amp}")
                            .replace("&", "\u00a7")
                            .replace("{amp}", "&");
                    name = new UnicodeUnescaper().translate(name);
                    NotEnoughUpdates.INSTANCE.manager.itemRenameJson.addProperty(heldUUID, name);
                    NotEnoughUpdates.INSTANCE.manager.saveItemRenameConfig();
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "[NEU] Set custom name for held item"));
                    break;
                default:
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[NEU] Unknown subcommand \"" + args[0] + "\""));
                case "help":
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "[NEU] Available commands:"));
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "help: Print this message"));
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "clearall: Clears all custom names "
                            + EnumChatFormatting.BOLD + "(Cannot be undone)"));
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "clear: Clears held item name "
                            + EnumChatFormatting.BOLD + "(Cannot be undone)"));
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "uuid: Returns the UUID of the currently held item"));
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "set: Sets the custom name of the currently held item"));
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "Usage: /neurename set [name...]"));

            }
        }
    });*/
}
