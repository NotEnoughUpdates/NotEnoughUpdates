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

package io.github.moulberry.notenoughupdates.commands.profile;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import io.github.moulberry.notenoughupdates.profileviewer.PlayerStats;
import io.github.moulberry.notenoughupdates.profileviewer.ProfileViewer;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PeekCommand extends ClientCommandBase {

	private ScheduledExecutorService peekCommandExecutorService = null;
	private ScheduledFuture<?> peekScheduledFuture = null;

	public PeekCommand() {
		super("peek");
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		String name;
		if (args.length == 0) {
			name = Minecraft.getMinecraft().thePlayer.getName();
		} else {
			name = args[0];
		}
		int id = new Random().nextInt(Integer.MAX_VALUE / 2) + Integer.MAX_VALUE / 2;

		Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new ChatComponentText(
			EnumChatFormatting.YELLOW + "[PEEK] Getting player information..."), id);
		NotEnoughUpdates.profileViewer.getProfileByName(name, profile -> {
			if (profile == null) {
				Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new ChatComponentText(
					EnumChatFormatting.RED + "[PEEK] Unknown player or the api is down."), id);
			} else {
				profile.resetCache();

				if (peekCommandExecutorService == null) {
					peekCommandExecutorService = Executors.newSingleThreadScheduledExecutor();
				}

				if (peekScheduledFuture != null && !peekScheduledFuture.isDone()) {
					Utils.addChatMessage(
						EnumChatFormatting.RED + "[PEEK] New peek command run, cancelling old one.");
					peekScheduledFuture.cancel(true);
				}

				Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new ChatComponentText(
					EnumChatFormatting.YELLOW + "[PEEK] Getting the player's Skyblock profile(s)..."), id);

				long startTime = System.currentTimeMillis();
				peekScheduledFuture = peekCommandExecutorService.schedule(new Runnable() {
					public void run() {
						if (System.currentTimeMillis() - startTime > 10 * 1000) {
							Minecraft.getMinecraft().ingameGUI
								.getChatGUI()
								.printChatMessageWithOptionalDeletion(new ChatComponentText(
									EnumChatFormatting.RED + "[PEEK] Getting profile info took too long, aborting."), id);
							return;
						}

						String g = EnumChatFormatting.GRAY.toString();

						JsonObject profileInfo = profile.getProfileInformation(null);
						if (profileInfo != null) {
							float overallScore = 0;

							boolean isMe = name.equalsIgnoreCase("moulberry");

							PlayerStats.Stats stats = profile.getStats(null);
							if (stats == null) return;
							Map<String, ProfileViewer.Level> skyblockInfo = profile.getSkyblockInfo(null);

							Minecraft.getMinecraft().ingameGUI
								.getChatGUI()
								.printChatMessageWithOptionalDeletion(new ChatComponentText(EnumChatFormatting.GREEN + " " +
									EnumChatFormatting.STRIKETHROUGH + "-=-" + EnumChatFormatting.RESET + EnumChatFormatting.GREEN + " " +
									Utils.getElementAsString(profile.getHypixelProfile().get("displayname"), name) + "'s Info " +
									EnumChatFormatting.STRIKETHROUGH + "-=-"), id);

							if (skyblockInfo == null) {
								Utils.addChatMessage(EnumChatFormatting.YELLOW + "Skills API disabled!");
							} else {
								float totalSkillLVL = 0;
								float totalSkillCount = 0;

								List<String> skills = Arrays.asList("taming", "mining", "foraging", "enchanting", "farming", "combat", "fishing", "alchemy");
								for (String skillName : skills) {
									totalSkillLVL += skyblockInfo.get(skillName).level;
									totalSkillCount++;
								}

								float combat = skyblockInfo.get("combat").level;
								float zombie = skyblockInfo.get("zombie").level;
								float spider = skyblockInfo.get("spider").level;
								float wolf = skyblockInfo.get("wolf").level;
								float enderman = skyblockInfo.get("enderman").level;

								float avgSkillLVL = totalSkillLVL / totalSkillCount;

								if (isMe) {
									avgSkillLVL = 6;
									combat = 4;
									zombie = 2;
									spider = 1;
									wolf = 2;
									enderman = 0;
								}

								EnumChatFormatting combatPrefix = combat > 20
									? (combat > 35 ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW)
									: EnumChatFormatting.RED;
								EnumChatFormatting zombiePrefix = zombie > 3
									? (zombie > 6 ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW)
									: EnumChatFormatting.RED;
								EnumChatFormatting spiderPrefix = spider > 3
									? (spider > 6 ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW)
									: EnumChatFormatting.RED;
								EnumChatFormatting wolfPrefix =
									wolf > 3 ? (wolf > 6 ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW) : EnumChatFormatting.RED;
								EnumChatFormatting endermanPrefix = enderman > 3
									? (enderman > 6
									? EnumChatFormatting.GREEN
									: EnumChatFormatting.YELLOW)
									: EnumChatFormatting.RED;
								EnumChatFormatting avgPrefix = avgSkillLVL > 20
									? (avgSkillLVL > 35
									? EnumChatFormatting.GREEN
									: EnumChatFormatting.YELLOW)
									: EnumChatFormatting.RED;

								overallScore += zombie * zombie / 81f;
								overallScore += spider * spider / 81f;
								overallScore += wolf * wolf / 81f;
								overallScore += enderman * enderman / 81f;
								overallScore += avgSkillLVL / 20f;

								int cata = (int) skyblockInfo.get("catacombs").level;
								EnumChatFormatting cataPrefix = cata > 15
									? (cata > 25 ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW)
									: EnumChatFormatting.RED;

								overallScore += cata * cata / 2000f;

								Utils.addChatMessage(g + "Combat: " + combatPrefix + (int) Math.floor(combat) +
										(cata > 0 ? g + " - Cata: " + cataPrefix + cata : "") +
										g + " - AVG: " + avgPrefix + (int) Math.floor(avgSkillLVL));
								Utils.addChatMessage(g + "Slayer: " + zombiePrefix + (int) Math.floor(zombie) + g + "-" +
										spiderPrefix + (int) Math.floor(spider) + g + "-" +
										wolfPrefix + (int) Math.floor(wolf) + "-" +
										endermanPrefix + (int) Math.floor(enderman));
							}
							if (stats == null) {
								Utils.addChatMessage(EnumChatFormatting.YELLOW + "Skills, collection and/or inventory apis disabled!");
							} else {
								int health = (int) stats.get("health");
								int defence = (int) stats.get("defence");
								int strength = (int) stats.get("strength");
								int intelligence = (int) stats.get("intelligence");

								EnumChatFormatting healthPrefix = health > 800
									? (health > 1600
									? EnumChatFormatting.GREEN
									: EnumChatFormatting.YELLOW)
									: EnumChatFormatting.RED;
								EnumChatFormatting defencePrefix = defence > 200
									? (defence > 600
									? EnumChatFormatting.GREEN
									: EnumChatFormatting.YELLOW)
									: EnumChatFormatting.RED;
								EnumChatFormatting strengthPrefix = strength > 100
									? (strength > 300
									? EnumChatFormatting.GREEN
									: EnumChatFormatting.YELLOW)
									: EnumChatFormatting.RED;
								EnumChatFormatting intelligencePrefix = intelligence > 300
									? (intelligence > 900
									? EnumChatFormatting.GREEN
									: EnumChatFormatting.YELLOW)
									: EnumChatFormatting.RED;

								Utils.addChatMessage(									g + "Stats  : " + healthPrefix + health + EnumChatFormatting.RED + "\u2764 " +
									defencePrefix + defence + EnumChatFormatting.GREEN + "\u2748 " +
									strengthPrefix + strength + EnumChatFormatting.RED + "\u2741 " +
									intelligencePrefix + intelligence + EnumChatFormatting.AQUA + "\u270e ");
							}
							float bankBalance = Utils.getElementAsFloat(Utils.getElement(profileInfo, "banking.balance"), -1);
							float purseBalance = Utils.getElementAsFloat(Utils.getElement(profileInfo, "coin_purse"), 0);

							long networth = profile.getNetWorth(null);
							float money = Math.max(bankBalance + purseBalance, networth);
							EnumChatFormatting moneyPrefix = money > 50 * 1000 * 1000 ?
								(money > 200 * 1000 * 1000
									? EnumChatFormatting.GREEN
									: EnumChatFormatting.YELLOW) : EnumChatFormatting.RED;
							Utils.addChatMessage(								g + "Purse: " + moneyPrefix + Utils.shortNumberFormat(purseBalance, 0) + g + " - Bank: " +
								(bankBalance == -1 ? EnumChatFormatting.YELLOW + "N/A" : moneyPrefix +
									(isMe ? "4.8b" : Utils.shortNumberFormat(bankBalance, 0))) +
								(networth > 0 ? g + " - Net: " + moneyPrefix + Utils.shortNumberFormat(networth, 0) : ""));

							overallScore += Math.min(2, money / (100f * 1000 * 1000));

							String activePet = Utils.getElementAsString(
								Utils.getElement(profile.getPetsInfo(null), "active_pet.type"),
								"None Active"
							);
							String activePetTier = Utils.getElementAsString(Utils.getElement(
								profile.getPetsInfo(null),
								"active_pet.tier"
							), "UNKNOWN");

							String col = NotEnoughUpdates.petRarityToColourMap.get(activePetTier);
							if (col == null) col = EnumChatFormatting.LIGHT_PURPLE.toString();

							Utils.addChatMessage(g + "Pet    : " + col + WordUtils.capitalizeFully(activePet.replace("_", " ")));

							String overall = "Skywars Main";
							if (isMe) {
								overall = Utils.chromaString("Literally the best player to exist"); // ego much
							} else if (overallScore < 5 && (bankBalance + purseBalance) > 500 * 1000 * 1000) {
								overall = EnumChatFormatting.GOLD + "Bill Gates";
							} else if (overallScore > 9) {
								overall = Utils.chromaString("Didn't even think this score was possible");
							} else if (overallScore > 8) {
								overall = Utils.chromaString("Mentally unstable");
							} else if (overallScore > 7) {
								overall = EnumChatFormatting.GOLD + "Why though 0.0";
							} else if (overallScore > 5.5) {
								overall = EnumChatFormatting.GOLD + "Bro stop playing";
							} else if (overallScore > 4) {
								overall = EnumChatFormatting.GREEN + "Kinda sweaty";
							} else if (overallScore > 3) {
								overall = EnumChatFormatting.YELLOW + "Alright I guess";
							} else if (overallScore > 2) {
								overall = EnumChatFormatting.YELLOW + "Ender Non";
							} else if (overallScore > 1) {
								overall = EnumChatFormatting.RED + "Played Skyblock";
							}

							Utils.addChatMessage(g + "Overall score: " + overall + g + " (" + Math.round(overallScore * 10) / 10f + ")");

							peekCommandExecutorService.shutdownNow();
						} else {
							peekScheduledFuture = peekCommandExecutorService.schedule(this, 200, TimeUnit.MILLISECONDS);
						}
					}
				}, 200, TimeUnit.MILLISECONDS);
			}
		});
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		if (args.length != 1) return null;

		String lastArg = args[args.length - 1];
		List<String> playerMatches = new ArrayList<>();
		for (EntityPlayer player : Minecraft.getMinecraft().theWorld.playerEntities) {
			String playerName = player.getName();
			if (playerName.toLowerCase().startsWith(lastArg.toLowerCase())) {
				playerMatches.add(playerName);
			}
		}
		return playerMatches;
	}
}
