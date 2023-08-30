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

package io.github.moulberry.notenoughupdates.listener;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.dungeons.DungeonWin;
import io.github.moulberry.notenoughupdates.miscfeatures.CookieWarning;
import io.github.moulberry.notenoughupdates.miscfeatures.CrystalMetalDetectorSolver;
import io.github.moulberry.notenoughupdates.miscfeatures.EnderNodes;
import io.github.moulberry.notenoughupdates.miscfeatures.StreamerMode;
import io.github.moulberry.notenoughupdates.miscfeatures.world.EnderNodeHighlighter;
import io.github.moulberry.notenoughupdates.overlays.OverlayManager;
import io.github.moulberry.notenoughupdates.overlays.SlayerOverlay;
import io.github.moulberry.notenoughupdates.overlays.TimersOverlay;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.moulberry.notenoughupdates.overlays.SlayerOverlay.RNGMeter;
import static io.github.moulberry.notenoughupdates.overlays.SlayerOverlay.slayerXp;
import static io.github.moulberry.notenoughupdates.overlays.SlayerOverlay.timeSinceLastBoss;
import static io.github.moulberry.notenoughupdates.overlays.SlayerOverlay.timeSinceLastBoss2;

public class ChatListener {

	private final NotEnoughUpdates neu;

	private static final Pattern SLAYER_EXP_PATTERN = Pattern.compile(
		"   (Spider|Zombie|Wolf|Enderman|Blaze) Slayer LVL (\\d) - (?:Next LVL in ([\\d,]+) XP!|LVL MAXED OUT!)");
	private static final Pattern SKY_BLOCK_LEVEL_PATTERN = Pattern.compile("\\[(\\d{1,4})\\] .*");
	private final Pattern PARTY_FINDER_PATTERN = Pattern.compile("§dParty Finder §r§f> (.*)§ejoined the (dungeon )?group!");

	private AtomicBoolean missingRecipe = new AtomicBoolean(false);

	public ChatListener(NotEnoughUpdates neu) {
		this.neu = neu;
	}

	private String processText(String text) {
		if (SBInfo.getInstance().getLocation() == null) return text;
		if (!SBInfo.getInstance().getLocation().startsWith("mining_") && !SBInfo.getInstance().getLocation().equals(
			"crystal_hollows"))
			return text;

		if (Minecraft.getMinecraft().thePlayer == null) return text;
		if (!NotEnoughUpdates.INSTANCE.config.mining.drillFuelBar) return text;

		return Utils.trimIgnoreColour(text.replaceAll(EnumChatFormatting.DARK_GREEN + "\\S+ Drill Fuel", ""));
	}

	private IChatComponent processChatComponent(IChatComponent chatComponent) {
		IChatComponent newComponent;
		if (chatComponent instanceof ChatComponentText) {
			ChatComponentText text = (ChatComponentText) chatComponent;

			newComponent = new ChatComponentText(processText(text.getUnformattedTextForChat()));
			newComponent.setChatStyle(text.getChatStyle().createShallowCopy());

			for (IChatComponent sibling : text.getSiblings()) {
				newComponent.appendSibling(processChatComponent(sibling));
			}
		} else if (chatComponent instanceof ChatComponentTranslation) {
			ChatComponentTranslation trans = (ChatComponentTranslation) chatComponent;

			Object[] args = trans.getFormatArgs();
			Object[] newArgs = new Object[args.length];
			for (int i = 0; i < trans.getFormatArgs().length; i++) {
				if (args[i] instanceof IChatComponent) {
					newArgs[i] = processChatComponent((IChatComponent) args[i]);
				} else {
					newArgs[i] = args[i];
				}
			}
			newComponent = new ChatComponentTranslation(trans.getKey(), newArgs);

			for (IChatComponent sibling : trans.getSiblings()) {
				newComponent.appendSibling(processChatComponent(sibling));
			}
		} else {
			newComponent = chatComponent.createCopy();
		}

		return newComponent;
	}

	private IChatComponent replaceSocialControlsWithPV(IChatComponent chatComponent) {


		if (NotEnoughUpdates.INSTANCE.config.misc.replaceSocialOptions1 > 0 &&
			NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard() &&
			((!chatComponent.getSiblings().isEmpty() && chatComponent.getSiblings().get(0).getChatStyle() != null &&
				chatComponent.getSiblings().get(0).getChatStyle().getChatClickEvent() != null &&
				chatComponent.getSiblings().get(0).getChatStyle().getChatClickEvent().getAction() == ClickEvent.Action.RUN_COMMAND) ||
				// Party/global chat with levels off components are different from global chat with levels on, so need to check for them here
				// Party/global without levels being 0 and global with levels being 1
			(!chatComponent.getSiblings().isEmpty() && chatComponent.getSiblings().size() > 1 && chatComponent.getSiblings().get(1).getChatStyle() != null &&
				chatComponent.getSiblings().get(1).getChatStyle().getChatClickEvent() != null &&
				chatComponent.getSiblings().get(1).getChatStyle().getChatClickEvent().getAction() == ClickEvent.Action.RUN_COMMAND))) {

			String startsWith = null;
			List<IChatComponent> siblings = chatComponent.getSiblings();
			int chatComponentsCount = siblings.size()-2;

			if (!siblings.isEmpty() && siblings.get(0).getChatStyle() != null && siblings.get(0).getChatStyle().getChatClickEvent() != null && siblings.get(0).getChatStyle().getChatClickEvent().getValue().startsWith("/viewprofile")) {
				startsWith = "/viewprofile";
			} else if (!siblings.isEmpty() && siblings.size() > 1 && siblings.get(1).getChatStyle() != null) {

				ClickEvent chatClickEvent = chatComponent.getSiblings().get(chatComponentsCount).getChatStyle().getChatClickEvent();
				if (chatClickEvent != null) {
					if (chatClickEvent.getValue().startsWith("/socialoptions")) {
						startsWith = "/socialoptions";
					}
				}
			}

			if (startsWith != null) {
				String username = startsWith.equals("/viewprofile") ?
					Utils.getNameFromChatComponent(chatComponent) :
					chatComponent.getSiblings().get(chatComponentsCount).getChatStyle().getChatClickEvent().getValue().substring(15);

				if (NotEnoughUpdates.INSTANCE.config.misc.replaceSocialOptions1 == 1) {

					ChatStyle pvClickStyle = getPVChatStyle(username);
						chatComponent.getSiblings().get(chatComponentsCount).setChatStyle(pvClickStyle);
					return chatComponent;
				} else if (NotEnoughUpdates.INSTANCE.config.misc.replaceSocialOptions1 == 2) {

					ChatStyle ahClickStyle = Utils.createClickStyle(
						ClickEvent.Action.RUN_COMMAND,
						"/ah " + username,
						"" + EnumChatFormatting.YELLOW + "Click to open " + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD +
							username + EnumChatFormatting.RESET + EnumChatFormatting.YELLOW + "'s /ah page"
					);

						chatComponent.getSiblings().get(chatComponentsCount).setChatStyle(ahClickStyle);
					return chatComponent;
				}
			} // wanted to add this for guild but guild uses uuid :sad:
		}
		return chatComponent;
	}

	private static ChatStyle getPVChatStyle(String username) {
		return Utils.createClickStyle(
			ClickEvent.Action.RUN_COMMAND,
			"/pv " + username,
			"" + EnumChatFormatting.YELLOW + "Click to open " + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD +
				username + EnumChatFormatting.RESET + EnumChatFormatting.YELLOW + "'s profile in " +
				EnumChatFormatting.DARK_PURPLE + EnumChatFormatting.BOLD + "NEU's" + EnumChatFormatting.RESET +
				EnumChatFormatting.YELLOW + " profile viewer."
		);
	}

	/**
	 * 1) When receiving "You are playing on profile" messages, will set the current profile.
	 * 2) When a /viewrecipe command fails (i.e. player does not have recipe unlocked, will open the custom recipe GUI)
	 * 3) Replaces lobby join notifications when streamer mode is active
	 */
	@SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
	public void onGuiChat(ClientChatReceivedEvent e) {
		if (e.type == 2) {
			CrystalMetalDetectorSolver.process(e.message);
			TimersOverlay.processActionBar(e.message.getUnformattedText());
			e.message = processChatComponent(e.message);
			return;
		} else if (e.type == 0) {
			e.message = replaceSocialControlsWithPV(e.message);
			if (NotEnoughUpdates.INSTANCE.config.misc.dungeonGroupsPV) {
				e.message = dungeonPartyJoinPV(e.message);
			}
		}

		DungeonWin.onChatMessage(e);

		String r = null;
		String unformatted = Utils.cleanColour(e.message.getUnformattedText());
		Matcher matcher = SLAYER_EXP_PATTERN.matcher(unformatted);
		if (unformatted.startsWith("You are playing on profile: ")) {
			SBInfo.getInstance().setCurrentProfile(unformatted
				.substring("You are playing on profile: ".length())
				.split(" ")[0].trim());
		} else if (unformatted.startsWith("Your profile was changed to: ")) {//Your profile was changed to:
			SBInfo.getInstance().setCurrentProfile(unformatted
				.substring("Your profile was changed to: ".length())
				.split(" ")[0].trim());
		} else if (unformatted.startsWith("Player List Info is now disabled!")) {
			SBInfo.getInstance().hasNewTab = false;
		} else if (unformatted.startsWith("Player List Info is now enabled!")) {
			SBInfo.getInstance().hasNewTab = true;
		}
		if (e.message.getFormattedText().equals(
			EnumChatFormatting.RESET.toString() + EnumChatFormatting.RED + "You haven't unlocked this recipe!" +
				EnumChatFormatting.RESET)) {
			r = EnumChatFormatting.RED + "You haven't unlocked this recipe!";
		} else if (e.message.getFormattedText().startsWith(
			EnumChatFormatting.RESET.toString() + EnumChatFormatting.RED + "Invalid recipe ")) {
			r = "";
		} else if (unformatted.equals("  NICE! SLAYER BOSS SLAIN!")) {
			SlayerOverlay.isSlain = true;
		} else if (unformatted.equals("  SLAYER QUEST STARTED!")) {
			SlayerOverlay.isSlain = false;
			if (timeSinceLastBoss == 0) {
				SlayerOverlay.timeSinceLastBoss = System.currentTimeMillis();
			} else {
				timeSinceLastBoss2 = timeSinceLastBoss;
				timeSinceLastBoss = System.currentTimeMillis();
			}
		} else if (unformatted.startsWith("   RNG Meter")) {
			RNGMeter = unformatted.substring("   RNG Meter - ".length());
		} else if (matcher.matches()) {
			SlayerOverlay.slayerLVL = matcher.group(2);
			if (!SlayerOverlay.slayerLVL.equals("9")) {
				SlayerOverlay.slayerXp = matcher.group(3);
			} else {
				slayerXp = "maxed";
			}
		} else if (unformatted.startsWith("Sending to server") || (unformatted.startsWith(
			"Your Slayer Quest has been cancelled!"))) {
			SlayerOverlay.slayerQuest = false;
			SlayerOverlay.unloadOverlayTimer = System.currentTimeMillis();
		} else if (unformatted.startsWith("You consumed a Booster Cookie!")) {
			CookieWarning.resetNotification();
		} else if (unformatted.startsWith("QUICK MATHS! Solve:") && NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
			if (Math.random() < 0.2) {
				if (NotEnoughUpdates.INSTANCE.config.misc.calculationMode == 2) {
					ClientCommandHandler.instance.executeCommand(
						Minecraft.getMinecraft().thePlayer,
						"/neucalc " + unformatted.substring("QUICK MATHS! Solve: ".length())
					);
				}
			}
		}
		if (r != null) {
			if (neu.manager.failViewItem(r)) {
				e.setCanceled(true);
			}
			missingRecipe.set(true);
		}
		if (unformatted.startsWith("Sending to server") &&
			NotEnoughUpdates.INSTANCE.config.misc.streamerMode && e.message instanceof ChatComponentText) {
			String m = e.message.getFormattedText();
			String m2 = StreamerMode.filterChat(e.message.getFormattedText());
			if (!m.equals(m2)) {
				e.message = new ChatComponentText(m2);
			}
		}
		if (unformatted.startsWith("You found ") && SBInfo.getInstance().getLocation() != null &&
			SBInfo.getInstance().getLocation().equals("crystal_hollows")) {
			CrystalMetalDetectorSolver.resetSolution(true);
		}
		if (unformatted.startsWith("[NPC] Keeper of ") | unformatted.startsWith("[NPC] Professor Robot: ") ||
			unformatted.startsWith("  ") || unformatted.startsWith("✦") || unformatted.equals(
			"  You've earned a Crystal Loot Bundle!"))
			OverlayManager.crystalHollowOverlay.message(unformatted);

		Matcher LvlMatcher = SKY_BLOCK_LEVEL_PATTERN.matcher(unformatted);
		if (LvlMatcher.matches()) {
			if (Integer.parseInt(LvlMatcher.group(1)) < NotEnoughUpdates.INSTANCE.config.misc.filterChatLevel &&
				NotEnoughUpdates.INSTANCE.config.misc.filterChatLevel != 0) {
				e.setCanceled(true);
			}
		}

		OverlayManager.powderGrindingOverlay.onMessage(unformatted);

		if (unformatted.startsWith("ENDER NODE!"))
			EnderNodeHighlighter.getInstance().highlightedBlocks.clear();

		if (unformatted.equals("ENDER NODE! You found Endermite Nest!"))
			EnderNodes.displayEndermiteNotif();
	}

	private IChatComponent dungeonPartyJoinPV(IChatComponent message) {
		String text = message.getFormattedText();
		Matcher matcher = PARTY_FINDER_PATTERN.matcher(text);

		if (matcher.find()) {
			String name = StringUtils.stripControlCodes(matcher.group(1)).trim();
			ChatComponentText componentText = new ChatComponentText(text);
			componentText.setChatStyle(getPVChatStyle(name));
			return componentText;
		} else {
			return message;
		}
	}
}
