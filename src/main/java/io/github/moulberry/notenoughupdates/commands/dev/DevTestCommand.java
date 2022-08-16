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

package io.github.moulberry.notenoughupdates.commands.dev;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import io.github.moulberry.notenoughupdates.core.config.GuiPositionEditor;
import io.github.moulberry.notenoughupdates.core.util.MiscUtils;
import io.github.moulberry.notenoughupdates.miscfeatures.FishingHelper;
import io.github.moulberry.notenoughupdates.miscfeatures.customblockzones.CustomBiomes;
import io.github.moulberry.notenoughupdates.miscfeatures.customblockzones.LocationChangeEvent;
import io.github.moulberry.notenoughupdates.miscfeatures.customblockzones.SpecialBlockZone;
import io.github.moulberry.notenoughupdates.miscgui.GuiPriceGraph;
import io.github.moulberry.notenoughupdates.miscgui.minionhelper.MinionHelperManager;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.TabListUtils;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.common.MinecraftForge;

import java.util.Arrays;
import java.util.List;

public class DevTestCommand extends ClientCommandBase {

	private static final List<String> DEV_TESTERS =
		Arrays.asList(
			"moulberry",
			"lucycoconut",
			"ironm00n",
			"ariyio",
			"throwpo",
			"lrg89",
			"dediamondpro",
			"lulonaut",
			"craftyoldminer",
			"eisengolem",
			"hannibal2"
		);

	private static final String[] DEV_FAIL_STRINGS = {
		"No.",
		"I said no.",
		"You aren't allowed to use this.",
		"Are you sure you want to use this? Type 'Yes' in chat.",
		"Are you sure you want to use this? Type 'Yes' in chat.",
		"Lmao you thought",
		"Ok please stop",
		"What do you want from me?",
		"This command almost certainly does nothing useful for you",
		"Ok, this is the last message, after this it will repeat",
		"No.",
		"I said no.",
		"Dammit. I thought that would work. Uhh...",
		"\u00a7dFrom \u00a7c[ADMIN] Minikloon\u00a77: If you use that command again, I'll have to ban you",
		"",
		"Ok, this is actually the last message, use the command again and you'll crash I promise"
	};
	private int devFailIndex = 0;

	public DevTestCommand() {
		super("neudevtest");
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if (!DEV_TESTERS.contains(Minecraft.getMinecraft().thePlayer.getName().toLowerCase())) {
			if (devFailIndex >= DEV_FAIL_STRINGS.length) {
				throw new Error("L") {
					@Override
					public void printStackTrace() {
						throw new Error("L");
					}
				};
			}
			if (devFailIndex == DEV_FAIL_STRINGS.length - 2) {
				devFailIndex++;

				ChatComponentText component = new ChatComponentText("\u00a7cYou are permanently banned from this server!");
				component.appendText("\n");
				component.appendText("\n\u00a77Reason: \u00a7rI told you not to run the command - Moulberry");
				component.appendText("\n\u00a77Find out more: \u00a7b\u00a7nhttps://www.hypixel.net/appeal");
				component.appendText("\n");
				component.appendText("\n\u00a77Ban ID: \u00a7r#49871982");
				component.appendText("\n\u00a77Sharing your Ban ID may affect the processing of your appeal!");
				Minecraft.getMinecraft().getNetHandler().getNetworkManager().closeChannel(component);
				return;
			}
			Utils.addChatMessage(EnumChatFormatting.RED + DEV_FAIL_STRINGS[devFailIndex++]);
			return;
		}
		if (args.length >= 1 && args[0].equalsIgnoreCase("profileinfo")) {
			String currentProfile = SBInfo.getInstance().currentProfile;
			SBInfo.Gamemode gamemode = SBInfo.getInstance().getGamemodeForProfile(currentProfile);
			sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD +
				"You are on Profile " +
				currentProfile +
				" with the mode " +
				gamemode));
		}
		if (args.length >= 1 && args[0].equalsIgnoreCase("pricetest")) {
			if (args.length == 1) {
				NotEnoughUpdates.INSTANCE.manager.auctionManager.updateBazaar();
			} else {
				NotEnoughUpdates.INSTANCE.openGui = new GuiPriceGraph(args[1]);
			}
		}
		if (args.length == 1 && args[0].equalsIgnoreCase("zone")) {
			BlockPos target = Minecraft.getMinecraft().objectMouseOver.getBlockPos();
			if (target == null) target = Minecraft.getMinecraft().thePlayer.getPosition();
			SpecialBlockZone zone = CustomBiomes.INSTANCE.getSpecialZone(target);
			Arrays.asList(
				new ChatComponentText("Showing Zone Info for: " + target),
				new ChatComponentText("Zone: " + (zone != null ? zone.name() : "null")),
				new ChatComponentText("Location: " + SBInfo.getInstance().getLocation()),
				new ChatComponentText("Biome: " + CustomBiomes.INSTANCE.getCustomBiome(target))
			).forEach(Minecraft.getMinecraft().thePlayer::addChatMessage);
			MinecraftForge.EVENT_BUS.post(new LocationChangeEvent(SBInfo.getInstance().getLocation(), SBInfo
				.getInstance()
				.getLocation()));
		}
		if (args.length == 1 && args[0].equalsIgnoreCase("positiontest")) {
			NotEnoughUpdates.INSTANCE.openGui = new GuiPositionEditor();
			return;
		}

		if (args.length == 2 && args[0].equalsIgnoreCase("pt")) {
			EnumParticleTypes t = EnumParticleTypes.valueOf(args[1]);
			FishingHelper.type = t;
			return;
		}
		if (args.length == 1 && args[0].equalsIgnoreCase("dev")) {
			NotEnoughUpdates.INSTANCE.config.hidden.dev = !NotEnoughUpdates.INSTANCE.config.hidden.dev;
			Utils.addChatMessage("§e[NEU] Dev mode " + (NotEnoughUpdates.INSTANCE.config.hidden.dev ? "§aenabled": "§cdisabled"));
			return;
		}
		if (args.length == 1 && args[0].equalsIgnoreCase("saveconfig")) {
			NotEnoughUpdates.INSTANCE.saveConfig();
			return;
		}
		if (args.length == 1 && args[0].equalsIgnoreCase("searchmode")) {
			NotEnoughUpdates.INSTANCE.config.hidden.firstTimeSearchFocus = true;
			Utils.addChatMessage(EnumChatFormatting.AQUA + "I would never search");
			return;
		}
		if (args.length == 2 && args[0].equalsIgnoreCase("openGui")) {
			try {
				NotEnoughUpdates.INSTANCE.openGui = (GuiScreen) Class.forName(args[1]).newInstance();
				Utils.addChatMessage("Opening gui: " + NotEnoughUpdates.INSTANCE.openGui);
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | ClassCastException e) {
				e.printStackTrace();
				Utils.addChatMessage("Failed to open this gui.");
			}
		}
		if (args.length == 1 && args[0].equalsIgnoreCase("center")) {
			double x = Math.floor(Minecraft.getMinecraft().thePlayer.posX) + 0.5f;
			double z = Math.floor(Minecraft.getMinecraft().thePlayer.posZ) + 0.5f;
			Minecraft.getMinecraft().thePlayer.setPosition(x, Minecraft.getMinecraft().thePlayer.posY, z);
		}
		if (args.length >= 1 && args[0].equalsIgnoreCase("minion")) {
			MinionHelperManager.getInstance().handleCommand(args);
		}
		if (args.length == 1 && args[0].equalsIgnoreCase("copytablist")) {
			StringBuilder builder = new StringBuilder();
			for (String name : TabListUtils.getTabList()) {
				builder.append(name).append("\n");
			}
			MiscUtils.copyToClipboard(builder.toString());
			Utils.addChatMessage("§e[NEU] Copied tablist to clipboard!");
		}
	}
}
