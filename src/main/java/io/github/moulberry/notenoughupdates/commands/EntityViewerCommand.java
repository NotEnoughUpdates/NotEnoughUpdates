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

package io.github.moulberry.notenoughupdates.commands;

import com.google.common.collect.Lists;
import io.github.moulberry.notenoughupdates.miscfeatures.entityviewer.EntityViewer;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class EntityViewerCommand extends ClientCommandBase {
	public EntityViewerCommand() {
		super("neushowentity");
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public List<String> getCommandAliases() {
		return Lists.newArrayList("neuentityviewer");
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return EnumChatFormatting.RED + "Use /neushowentity list";
	}

	public void showUsage(ICommandSender sender) {
		sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
	}

	private final Queue<EntityViewer> queuedGUIS = new ConcurrentLinkedDeque<>();

	@SubscribeEvent
	public void onTick(TickEvent event) {
		if (Minecraft.getMinecraft().currentScreen == null) {
			EntityViewer poll = queuedGUIS.poll();
			if (poll == null) return;
			Minecraft.getMinecraft().displayGuiScreen(poll);
		}
	}

	@Override
	public void processCommand(ICommandSender sender, String[] strings) throws CommandException {
		if (strings.length == 0) {
			showUsage(sender);
			return;
		}
		if (strings[0].equals("list")) {
			for (String label : EntityViewer.validEntities.keySet()) {
				sender.addChatMessage(new ChatComponentText(EnumChatFormatting.BLUE + " " + label)
					.setChatStyle(new ChatStyle().setChatClickEvent(
						new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/neuentityviewer " + label))));
			}
			return;
		}
		EntityLivingBase entityLivingBase;
		if (strings[0].startsWith("@")) {
			ResourceLocation resourceLocation = new ResourceLocation(strings[0].substring(1));
			entityLivingBase = EntityViewer.constructEntity(resourceLocation);
		} else {
			entityLivingBase = EntityViewer.constructEntity(strings[0], Arrays.copyOfRange(strings, 1, strings.length));
		}
		if (entityLivingBase == null) {
			sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Could not create that entity"));
			return;
		}
		queuedGUIS.add(new EntityViewer(strings[0], entityLivingBase));
	}
}
