/*
 * Copyright (C) 2022-2023 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.overlays;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.notenoughupdates.events.SlotClickEvent;
import io.github.moulberry.notenoughupdates.options.NEUConfig;
import io.github.moulberry.notenoughupdates.util.ItemUtils;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import lombok.var;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.util.vector.Vector2f;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.util.EnumChatFormatting.DARK_AQUA;

public class TimersOverlay extends TextTabOverlay {
	public TimersOverlay(
		Position position,
		Supplier<List<String>> dummyStrings,
		Supplier<TextOverlayStyle> styleSupplier
	) {
		super(position, dummyStrings, styleSupplier);
	}

	private static final Pattern PATTERN_ACTIVE_EFFECTS = Pattern.compile(
		"\u00a7r\u00a7r\u00a77You have a \u00a7r\u00a7cGod Potion \u00a7r\u00a77active! \u00a7r\u00a7d([1-5][0-9]|[0-9])[\\s|^\\S]?(Seconds|Second|Minutes|Minute|Hours|Hour|Day|Days|h|m|s) ?([1-5][0-9]|[0-9])?([ms])?\u00a7r");
	private static final Pattern CAKE_PATTERN = Pattern.compile(
		"\u00a7r\u00a7d\u00a7lYum! \u00a7r\u00a7eYou gain .+ \u00a7r\u00a7efor \u00a7r\u00a7a48 \u00a7r\u00a7ehours!\u00a7r");
	private static final Pattern PUZZLER_PATTERN =
		Pattern.compile("\u00a7r\u00a7dPuzzler\u00a7r\u00a76 gave you .+ \u00a7r\u00a76for solving the puzzle!\u00a7r");
	private static final Pattern FETCHUR_PATTERN =
		Pattern.compile("\u00a7e\\[NPC] Fetchur\u00a7f: \u00a7rthanks thats probably what i needed\u00a7r");
	private static final Pattern FETCHUR2_PATTERN =
		Pattern.compile("\u00a7e\\[NPC] Fetchur\u00a7f: \u00a7rcome back another time, maybe tmrw\u00a7r");
	private static final Pattern DAILY_MITHRIL_POWDER = Pattern.compile(
		"\u00a7r\u00a79\u1805 \u00a7r\u00a7fYou've earned \u00a7r\u00a72.+ Mithril Powder \u00a7r\u00a7ffrom mining your first Mithril Ore of the day!\u00a7r");
	private static final Pattern DAILY_GEMSTONE_POWDER = Pattern.compile(
		"\u00a7r\u00a79\u1805 \u00a7r\u00a7fYou've earned \u00a7r\u00a7d.+ Gemstone Powder \u00a7r\u00a7ffrom mining your first Gemstone of the day!\u00a7r");
	private static final Pattern DAILY_SHOP_LIMIT = Pattern.compile(
		"\u00a7r\u00a7cYou may only buy up to (640|6400) of this item each day!\u00a7r");

	@SubscribeEvent
	public void onClickItem(SlotClickEvent event) {
		NEUConfig.HiddenProfileSpecific hidden = NotEnoughUpdates.INSTANCE.config.getProfileSpecific();
		if (hidden == null) return;

		if (event.slot == null || !event.slot.getHasStack()) return;
		var itemStack = event.slot.getStack();
		if (itemStack.getItem() != Item.getItemFromBlock(Blocks.double_plant) || itemStack.getItemDamage() != 1) return;
		if (ItemUtils.getLore(itemStack).contains("§a§lFREE! §a(Every 4 hours)")) {
			hidden.lastFreeRiftInfusionApplied = System.currentTimeMillis();
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
	public void onChatMessageReceived(ClientChatReceivedEvent event) {
		NEUConfig.HiddenProfileSpecific hidden = NotEnoughUpdates.INSTANCE.config.getProfileSpecific();
		if (hidden == null) return;

		if (event.type == 0) {
			long currentTime = System.currentTimeMillis();
			Matcher cakeMatcher = CAKE_PATTERN.matcher(event.message.getFormattedText());
			if (cakeMatcher.matches()) {
				hidden.firstCakeAte = currentTime;
				return;
			}
			if ("§r§d§lINFUSED! §r§7Used your free dimensional infusion!§r".equals(event.message.getFormattedText())) {
				hidden.lastFreeRiftInfusionApplied = currentTime;
				return;
			}
			Matcher puzzlerMatcher = PUZZLER_PATTERN.matcher(event.message.getFormattedText());
			if (puzzlerMatcher.matches()) {
				hidden.puzzlerCompleted = currentTime;
				return;
			}

			Matcher fetchurMatcher = FETCHUR_PATTERN.matcher(event.message.getFormattedText());
			if (fetchurMatcher.matches()) {
				hidden.fetchurCompleted = currentTime;
				return;
			}

			Matcher fetchur2Matcher = FETCHUR2_PATTERN.matcher(event.message.getFormattedText());
			if (fetchur2Matcher.matches()) {
				hidden.fetchurCompleted = currentTime;
				return;
			}
			Matcher dailyGemstonePowder = DAILY_GEMSTONE_POWDER.matcher(event.message.getFormattedText());
			if (dailyGemstonePowder.matches()) {
				hidden.dailyGemstonePowderCompleted = currentTime;
				return;
			}
			Matcher dailyMithrilPowder = DAILY_MITHRIL_POWDER.matcher(event.message.getFormattedText());
			if (dailyMithrilPowder.matches()) {
				hidden.dailyMithrilPowerCompleted = currentTime;
				return;
			}
			Matcher dailyShopLimit = DAILY_SHOP_LIMIT.matcher(event.message.getFormattedText());
			if (dailyShopLimit.matches()) {
				hidden.dailyShopLimitCompleted = currentTime;
			}
		}
	}

	@Override
	protected Vector2f getSize(List<String> strings) {
		if (NotEnoughUpdates.INSTANCE.config.miscOverlays.todoIcons)
			return super.getSize(strings).translate(12, 0);
		return super.getSize(strings);
	}

	private static final ItemStack CAKES_ICON = new ItemStack(Items.cake);
	private static final ItemStack PUZZLER_ICON = new ItemStack(Items.book);
	private static ItemStack[] FETCHUR_ICONS = null;
	private static final ItemStack COMMISSIONS_ICON = new ItemStack(Items.iron_pickaxe);
	private static final ItemStack EXPERIMENTS_ICON = new ItemStack(Items.enchanted_book);
	private static final ItemStack COOKIE_ICON = new ItemStack(Items.cookie);
	private static final ItemStack QUEST_ICON = new ItemStack(Items.sign);
	private static final ItemStack SHOP_ICON = new ItemStack(Blocks.hopper);

	@Override
	protected void renderLine(String line, Vector2f position, boolean dummy) {
		if (!NotEnoughUpdates.INSTANCE.config.miscOverlays.todoIcons) {
			return;
		}
		GlStateManager.enableDepth();

		ItemStack icon = null;

		String clean = Utils.cleanColour(line);
		String beforeColon = clean.split(":")[0];
		switch (beforeColon) {
			case "Cakes":
				icon = CAKES_ICON;
				break;
			case "Puzzler":
				icon = PUZZLER_ICON;
				break;
			case "Godpot":
				icon = NotEnoughUpdates.INSTANCE.manager.jsonToStack(NotEnoughUpdates.INSTANCE.manager
					.getItemInformation()
					.get("GOD_POTION"));
				break;
			case "Fetchur": {
				if (FETCHUR_ICONS == null) {
					FETCHUR_ICONS = new ItemStack[]{
						new ItemStack(Blocks.wool, 50, 14),
						new ItemStack(Blocks.stained_glass, 20, 4),
						new ItemStack(Items.compass, 1, 0),
						new ItemStack(Items.prismarine_crystals, 20, 0),
						new ItemStack(Items.fireworks, 1, 0),
						NotEnoughUpdates.INSTANCE.manager.jsonToStack(NotEnoughUpdates.INSTANCE.manager
							.getItemInformation()
							.get("CHEAP_COFFEE")),
						new ItemStack(Items.oak_door, 1, 0),
						new ItemStack(Items.rabbit_foot, 3, 0),
						NotEnoughUpdates.INSTANCE.manager.jsonToStack(NotEnoughUpdates.INSTANCE.manager
							.getItemInformation()
							.get("SUPERBOOM_TNT")),
						new ItemStack(Blocks.pumpkin, 1, 0),
						new ItemStack(Items.flint_and_steel, 1, 0),
						new ItemStack(Blocks.quartz_ore, 50, 0),
						//new ItemStack(Items.ender_pearl, 16, 0)
					};
				}

				ZonedDateTime currentTimeEST = ZonedDateTime.now(ZoneId.of("America/Atikokan"));

				long fetchurIndex = ((currentTimeEST.getDayOfMonth() + 1) % 12) - 1;
				//Added because disabled fetchur and enabled it again but it was showing the wrong item
				//Lets see if this stays correct

				if (fetchurIndex < 0) fetchurIndex += 12;

				icon = FETCHUR_ICONS[(int) fetchurIndex];
				break;
			}
			case "Commissions":
				icon = COMMISSIONS_ICON;
				break;
			case "Experiments":
				icon = EXPERIMENTS_ICON;
				break;
			case "Cookie Buff":
				icon = COOKIE_ICON;
				break;
			case "Mithril Powder":
				icon = NotEnoughUpdates.INSTANCE.manager.jsonToStack(NotEnoughUpdates.INSTANCE.manager
					.getItemInformation()
					.get("MITHRIL_ORE"));
				break;
			case "Gemstone Powder":
				icon = NotEnoughUpdates.INSTANCE.manager.jsonToStack(NotEnoughUpdates.INSTANCE.manager
					.getItemInformation()
					.get("PERFECT_AMETHYST_GEM"));
				break;
			case "Heavy Pearls":
				icon = NotEnoughUpdates.INSTANCE.manager.jsonToStack(NotEnoughUpdates.INSTANCE.manager
					.getItemInformation()
					.get("HEAVY_PEARL"));
				break;
			case "Free Rift Infusion":
				icon = new ItemStack(Blocks.double_plant, 1, 1);
				break;
			case "Crimson Isle Quests":
				icon = QUEST_ICON;
				break;
			case "NPC Buy Daily Limit":
				icon = SHOP_ICON;
				break;
		}

		if (icon != null) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(position.x, position.y, 0);
			GlStateManager.scale(0.5f, 0.5f, 1f);
			Utils.drawItemStack(icon, 0, 0);
			GlStateManager.popMatrix();

			position.x += 12;
		}

		super.renderLine(line, position, dummy);
	}

	boolean hasErrorMessage = false;

	@Override
	public boolean isEnabled() {
		return NotEnoughUpdates.INSTANCE.config.miscOverlays.todoOverlay2;
	}

	@Override
	public void update() {

		long currentTime = System.currentTimeMillis();

		NEUConfig.HiddenProfileSpecific hidden = NotEnoughUpdates.INSTANCE.config.getProfileSpecific();
		if (hidden == null) return;

		if (NotEnoughUpdates.INSTANCE.config.miscOverlays.todoOverlayOnlyShowTab &&
			!lastTabState) {
			overlayStrings = null;
			return;
		}

		if (Minecraft.getMinecraft().currentScreen instanceof GuiChest) {
			GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
			ContainerChest container = (ContainerChest) chest.inventorySlots;
			IInventory lower = container.getLowerChestInventory();
			String containerName = lower.getDisplayName().getUnformattedText();
			ItemStack stack = lower.getStackInSlot(0);
			switch (containerName.intern()) {
				case "Dimensional Infusion":
					if (lower.getSizeInventory() != 9 * 4) break;
					var freeInfusionSlot = lower.getStackInSlot(13);
					if (freeInfusionSlot == null || freeInfusionSlot.stackSize != 1 ||
						freeInfusionSlot.getItem() != Item.getItemFromBlock(Blocks.double_plant) ||
						freeInfusionSlot.getItemDamage() != 1) {
						break;
					}
					if (ItemUtils.getLore(freeInfusionSlot).contains("§a§lFREE! §a(Every 4 hours)")) {
						hidden.lastFreeRiftInfusionApplied = 0L;
					}
					break;
				case "Commissions":
					if (lower.getSizeInventory() < 18) {
						break;
					}
					if (hidden.commissionsCompleted == 0) {
						hidden.commissionsCompleted = currentTime + TimeEnums.DAY.time;
					}
					for (int i = 9; i < 18; i++) {
						stack = lower.getStackInSlot(i);
						if (stack != null && stack.hasTagCompound()) {
							String[] lore = NotEnoughUpdates.INSTANCE.manager.getLoreFromNBT(stack.getTagCompound());
							for (String line : lore) {
								if (line.contains("(Daily")) {
									hidden.commissionsCompleted = 0;
									break;
								}
							}
						}
					}
					break;
				case "Experimentation Table":
					if (lower.getSizeInventory() < 36) {
						break;
					}
					stack = lower.getStackInSlot(31);
					if (stack != null) {
						if (stack.getItem() == Items.blaze_powder) {
							if (hidden.experimentsCompleted == 0) {
								hidden.experimentsCompleted = currentTime;
								return;
							}
						}
					}
					ItemStack stackSuperPairs = lower.getStackInSlot(22);
					if (stackSuperPairs != null && stackSuperPairs.getItem() == Items.skull &&
						stackSuperPairs.getTagCompound() != null) {
						String[] lore = NotEnoughUpdates.INSTANCE.manager.getLoreFromNBT(stackSuperPairs.getTagCompound());
						String text = lore[lore.length - 1];
						String cleanText = Utils.cleanColour(text);
						if (cleanText.equals("Experiments on cooldown!")) {
							hidden.experimentsCompleted = currentTime;
							return;
						}
					}
					hidden.experimentsCompleted = 0;
					break;
				case "Superpairs Rewards":
					if (lower.getSizeInventory() < 27) {
						break;
					}
					stack = lower.getStackInSlot(13);
					if (stack != null && Utils.cleanColour(stack.getDisplayName()).equals("Superpairs")) {
						hidden.experimentsCompleted = currentTime;
					}
				case "SkyBlock Menu":
					if (lower.getSizeInventory() < 54) {
						break;
					}
					stack = lower.getStackInSlot(51);
				case "Booster Cookie":
					if (lower.getSizeInventory() < 54) {
						break;
					}
					if (stack != lower.getStackInSlot(51)) {//if we didn't go into this case from the skyblock menu
						stack = lower.getStackInSlot(13);
					}

					if (stack != null && Utils.cleanColour(stack.getDisplayName()).equals("Booster Cookie") &&
						stack.getTagCompound() != null) {
						String[] lore = NotEnoughUpdates.INSTANCE.manager.getLoreFromNBT(stack.getTagCompound());
						for (String line : lore) {
							if (line.contains("Duration: ")) {
								String clean = line.replaceAll("(\u00a7.)", "");
								clean = clean.replaceAll("(\\d)([smhdy])", "$1 $2");
								String[] cleanSplit = clean.split(" ");
								String[] removeDuration = Arrays.copyOfRange(cleanSplit, 1, cleanSplit.length);
								hidden.cookieBuffRemaining = currentTime;
								for (int i = 0; i + 1 < removeDuration.length; i++) {
									if (i % 2 == 1) continue;

									String number = removeDuration[i];
									String unit = removeDuration[i + 1];
									try {
										long val = Integer.parseInt(number);
										switch (unit) {
											case "Years":
											case "Year":
												hidden.cookieBuffRemaining += val * 365 * 24 * 60 * 60 * 1000;
												break;
											case "Months":
											case "Month":
												hidden.cookieBuffRemaining += val * 30 * 24 * 60 * 60 * 1000;
												break;
											case "Days":
											case "Day":
											case "d":
												hidden.cookieBuffRemaining += val * 24 * 60 * 60 * 1000;
												break;
											case "Hours":
											case "Hour":
											case "h":
												hidden.cookieBuffRemaining += val * 60 * 60 * 1000;
												break;
											case "Minutes":
											case "Minute":
											case "m":
												hidden.cookieBuffRemaining += val * 60 * 1000;
												break;
											case "Seconds":
											case "Second":
											case "s":
												hidden.cookieBuffRemaining += val * 1000;
												break;
										}
									} catch (NumberFormatException e) {
										e.printStackTrace();
										hidden.cookieBuffRemaining = 0;
										if (!hasErrorMessage) {
											Utils.addChatMessage(
												EnumChatFormatting.YELLOW + "[NEU] Unable to work out your cookie buff timer");
											hasErrorMessage = true;
										}
										break;
									}
								}
								break;
							}
						}
					}
					break;
			}
		}

		boolean foundGodPotText = false;
		boolean foundEffectsText = false;
		if (SBInfo.getInstance().getLocation() != null && !SBInfo.getInstance().getLocation().equals("dungeon") &&
			SBInfo.getInstance().footer != null) {
			String formatted = SBInfo.getInstance().footer.getFormattedText();
			for (String line : formatted.split("\n")) {
				if (line.contains("Active Effects")) {
					foundEffectsText = true;
				}
				Matcher activeEffectsMatcher = PATTERN_ACTIVE_EFFECTS.matcher(line);
				if (activeEffectsMatcher.matches()) {
					foundGodPotText = true;
					long godPotDuration = 0;
					try {
						long godpotRemainingTime;
						for (int i = 1; i < activeEffectsMatcher.groupCount(); i += 2) {
							if (activeEffectsMatcher.group(i) == null) {
								continue;
							}
							godpotRemainingTime = Integer.parseInt(activeEffectsMatcher.group(i));
							String godpotRemainingTimeType = activeEffectsMatcher.group(i + 1);
							switch (godpotRemainingTimeType) {
								case "Days":
								case "Day":
									godPotDuration += godpotRemainingTime * 24 * 60 * 60 * 1000;
									break;
								case "Hours":
								case "Hour":
								case "h":
									godPotDuration += godpotRemainingTime * 60 * 60 * 1000;
									break;
								case "Minutes":
								case "Minute":
								case "m":
									godPotDuration += godpotRemainingTime * 60 * 1000;
									break;
								case "Seconds":
								case "Second":
								case "s":
									godPotDuration += godpotRemainingTime * 1000;
									break;
							}
						}
					} catch (Exception e) {
						if (!hasErrorMessage) {
							Utils.addChatMessage(EnumChatFormatting.YELLOW + "[NEU] Unable to work out your god pot timer");
							e.printStackTrace();
							hasErrorMessage = true;
						}
						break;
					}

					hidden.godPotionDuration = godPotDuration;

				}
			}
		}

		if (!foundGodPotText && foundEffectsText) {
			hidden.godPotionDuration = 0;
		}

		if (SBInfo.getInstance().completedQuests != null && SBInfo.getInstance().completedQuests.size() == 5) {
			hidden.questBoardCompleted = currentTime;
		}

		if (!NotEnoughUpdates.INSTANCE.config.miscOverlays.todoOverlay2) {
			overlayStrings = null;
			return;
		}

		HashMap<Integer, String> map = new HashMap<>();

		long cakeEnd = hidden.firstCakeAte + 1000 * 60 * 60 * 48 - currentTime;

		//Cake Display
		if (cakeEnd <= 0) {
			map.put(
				0,
				DARK_AQUA + "Cakes: " + EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.goneColour] +
					"Inactive!"
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.cakesDisplay >= DISPLAYTYPE.VERYSOON.ordinal() &&
			cakeEnd < TimeEnums.HOUR.time) {
			map.put(
				0,
				DARK_AQUA + "Cakes: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.verySoonColour] +
					Utils.prettyTime(cakeEnd)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.cakesDisplay >= DISPLAYTYPE.SOON.ordinal() &&
			cakeEnd < TimeEnums.HALFDAY.time) {
			map.put(
				0,
				DARK_AQUA + "Cakes: " + EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.soonColour] +
					Utils.prettyTime(cakeEnd)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.cakesDisplay >= DISPLAYTYPE.KINDASOON.ordinal() &&
			cakeEnd < TimeEnums.DAY.time) {
			map.put(
				0,
				DARK_AQUA + "Cakes: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.kindaSoonColour] +
					Utils.prettyTime(cakeEnd)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.cakesDisplay >= DISPLAYTYPE.ALWAYS.ordinal()) {
			map.put(
				0,
				DARK_AQUA + "Cakes: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.defaultColour] +
					Utils.prettyTime(cakeEnd)
			);
		}

		//CookieBuff Display
		if (hidden.cookieBuffRemaining <= 0) {
			map.put(
				1,
				DARK_AQUA + "Cookie Buff: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.goneColour] + "Inactive!"
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.cookieBuffDisplay >= DISPLAYTYPE.VERYSOON.ordinal() &&
			hidden.cookieBuffRemaining < TimeEnums.HOUR.time) {
			map.put(
				1,
				DARK_AQUA + "Cookie Buff: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.verySoonColour] +
					Utils.prettyTime(hidden.cookieBuffRemaining - currentTime)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.cookieBuffDisplay >= DISPLAYTYPE.SOON.ordinal() &&
			hidden.cookieBuffRemaining < TimeEnums.HALFDAY.time) {
			map.put(
				1,
				DARK_AQUA + "Cookie Buff: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.soonColour] +
					Utils.prettyTime(hidden.cookieBuffRemaining - currentTime)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.cookieBuffDisplay >= DISPLAYTYPE.KINDASOON.ordinal() &&
			hidden.cookieBuffRemaining < TimeEnums.DAY.time) {
			map.put(
				1,
				DARK_AQUA + "Cookie Buff: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.kindaSoonColour] +
					Utils.prettyTime(hidden.cookieBuffRemaining - currentTime)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.cookieBuffDisplay >= DISPLAYTYPE.ALWAYS.ordinal()) {
			map.put(
				1,
				DARK_AQUA + "Cookie Buff: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.defaultColour] +
					Utils.prettyTime(hidden.cookieBuffRemaining - currentTime)
			);
		}

		//Godpot Display
		//do not display in dungeons due to dungeons not having
		if (!(SBInfo.getInstance().getLocation() != null && SBInfo.getInstance().getLocation().equals("dungeon"))) {
			if (hidden.godPotionDuration <= 0) {
				map.put(
					2,
					DARK_AQUA + "Godpot: " +
						EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.goneColour] + "Inactive!"
				);
			} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.godpotDisplay >= DISPLAYTYPE.VERYSOON.ordinal() &&
				hidden.godPotionDuration < TimeEnums.HOUR.time) {
				map.put(
					2,
					DARK_AQUA + "Godpot: " +
						EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.verySoonColour] +
						Utils.prettyTime(hidden.godPotionDuration)
				);
			} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.godpotDisplay >= DISPLAYTYPE.SOON.ordinal() &&
				hidden.godPotionDuration < TimeEnums.HALFDAY.time) {
				map.put(
					2,
					DARK_AQUA + "Godpot: " +
						EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.soonColour] +
						Utils.prettyTime(hidden.godPotionDuration)
				);
			} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.godpotDisplay >= DISPLAYTYPE.KINDASOON.ordinal() &&
				hidden.godPotionDuration < TimeEnums.DAY.time) {
				map.put(
					2,
					DARK_AQUA + "Godpot: " +
						EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.kindaSoonColour] +
						Utils.prettyTime(hidden.godPotionDuration)
				);
			} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.godpotDisplay >= DISPLAYTYPE.ALWAYS.ordinal()) {
				map.put(
					2,
					DARK_AQUA + "Godpot: " +
						EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.defaultColour] +
						Utils.prettyTime(hidden.godPotionDuration)
				);
			}
		}

		// Free Rift Infusion
		var miscOverlay = NotEnoughUpdates.INSTANCE.config.miscOverlays;
		long riftAvailableAgainIn = hidden.lastFreeRiftInfusionApplied + 1000 * 60 * 60 * 4 - currentTime;
		if (riftAvailableAgainIn < 0) {
			map.put(
				12,
				DARK_AQUA + "Free Rift Infusion: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.readyColour] + "Ready!"
			);
		} else if ((miscOverlay.freeRiftInfusionDisplay == 1 && riftAvailableAgainIn < TimeEnums.HALFANHOUR.time) ||
			(miscOverlay.freeRiftInfusionDisplay == 2)) {
			map.put(
				12,
				DARK_AQUA + "Free Rift Infusion: " +
					EnumChatFormatting.values()[riftAvailableAgainIn < TimeEnums.HALFANHOUR.time
						? miscOverlay.verySoonColour
						: miscOverlay.defaultColour] + Utils.prettyTime(riftAvailableAgainIn)
			);
		}

		long puzzlerEnd = hidden.puzzlerCompleted + 1000 * 60 * 60 * 24 - currentTime;
		//Puzzler Display
		if ((hidden.puzzlerCompleted + TimeEnums.DAY.time) < currentTime) {
			map.put(
				3,
				DARK_AQUA + "Puzzler: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.readyColour] + "Ready!"
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.puzzlerDisplay >= DISPLAYTYPE.VERYSOON.ordinal() &&
			(hidden.puzzlerCompleted + (TimeEnums.DAY.time - TimeEnums.HALFANHOUR.time)) < currentTime) {
			map.put(
				3,
				DARK_AQUA + "Puzzler: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.verySoonColour] +
					Utils.prettyTime(puzzlerEnd)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.puzzlerDisplay >= DISPLAYTYPE.SOON.ordinal() &&
			(hidden.puzzlerCompleted + (TimeEnums.DAY.time - TimeEnums.HOUR.time)) < currentTime) {
			map.put(
				3,
				DARK_AQUA + "Puzzler: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.soonColour] +
					Utils.prettyTime(puzzlerEnd)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.puzzlerDisplay >= DISPLAYTYPE.KINDASOON.ordinal() &&
			(hidden.puzzlerCompleted + (TimeEnums.DAY.time - (TimeEnums.HOUR.time) * 3)) < currentTime) {
			map.put(
				3,
				DARK_AQUA + "Puzzler: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.kindaSoonColour] +
					Utils.prettyTime(puzzlerEnd)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.puzzlerDisplay >= DISPLAYTYPE.ALWAYS.ordinal()) {
			map.put(
				3,
				DARK_AQUA + "Puzzler: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.defaultColour] +
					Utils.prettyTime(puzzlerEnd)
			);
		}

		long midnightReset = (currentTime - 18000000) / TimeEnums.DAY.time * TimeEnums.DAY.time + 18000000; // 12am est
		long pearlsReset = midnightReset - 18000000; //8pm est
		long catacombsReset = currentTime / TimeEnums.DAY.time * TimeEnums.DAY.time; // 7pm est
		long timeDiffMidnightNow = midnightReset + TimeEnums.DAY.time - currentTime;
		long catacombsDiffNow = catacombsReset + TimeEnums.DAY.time - currentTime;
		long fetchurComplete = hidden.fetchurCompleted;

		//Fetchur Display
		if (fetchurComplete < midnightReset) {
			map.put(
				4,
				DARK_AQUA + "Fetchur: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.readyColour] + "Ready!"
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.fetchurDisplay >= DISPLAYTYPE.VERYSOON.ordinal() &&
			(fetchurComplete < (midnightReset - TimeEnums.HALFANHOUR.time))) {
			map.put(
				4,
				DARK_AQUA + "Fetchur: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.verySoonColour] +
					Utils.prettyTime(timeDiffMidnightNow)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.fetchurDisplay >= DISPLAYTYPE.SOON.ordinal() &&
			(fetchurComplete < (midnightReset - TimeEnums.HOUR.time))) {
			map.put(
				4,
				DARK_AQUA + "Fetchur: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.soonColour] +
					Utils.prettyTime(timeDiffMidnightNow)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.fetchurDisplay >= DISPLAYTYPE.KINDASOON.ordinal() &&
			(fetchurComplete < (midnightReset - (TimeEnums.HOUR.time * 3)))) {
			map.put(
				4,
				DARK_AQUA + "Fetchur: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.kindaSoonColour] +
					Utils.prettyTime(timeDiffMidnightNow)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.fetchurDisplay >= DISPLAYTYPE.ALWAYS.ordinal()) {
			map.put(
				4,
				DARK_AQUA + "Fetchur: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.defaultColour] +
					Utils.prettyTime(timeDiffMidnightNow)
			);
		}

		//Commissions Display
		if (hidden.commissionsCompleted < midnightReset) {
			map.put(
				5,
				DARK_AQUA + "Commissions: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.readyColour] + "Ready! "
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.commissionDisplay >= DISPLAYTYPE.VERYSOON.ordinal() &&
			(hidden.commissionsCompleted < (midnightReset - TimeEnums.HALFANHOUR.time))) {
			map.put(
				5,
				DARK_AQUA + "Commissions: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.verySoonColour] +
					Utils.prettyTime(timeDiffMidnightNow)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.commissionDisplay >= DISPLAYTYPE.SOON.ordinal() &&
			(hidden.commissionsCompleted < (midnightReset - TimeEnums.HOUR.time))) {
			map.put(
				5,
				DARK_AQUA + "Commissions: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.soonColour] +
					Utils.prettyTime(timeDiffMidnightNow)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.commissionDisplay >= DISPLAYTYPE.KINDASOON.ordinal() &&
			(hidden.commissionsCompleted < (midnightReset - (TimeEnums.HOUR.time * 3)))) {
			map.put(
				5,
				DARK_AQUA + "Commissions: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.kindaSoonColour] +
					Utils.prettyTime(timeDiffMidnightNow)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.commissionDisplay >= DISPLAYTYPE.ALWAYS.ordinal()) {
			map.put(
				5,
				DARK_AQUA + "Commissions: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.defaultColour] +
					Utils.prettyTime(timeDiffMidnightNow)
			);
		}

		//Experiment Display
		if (hidden.experimentsCompleted < catacombsReset) {
			map.put(
				6,
				DARK_AQUA + "Experiments: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.readyColour] + "Ready!"
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.experimentationDisplay >= DISPLAYTYPE.VERYSOON.ordinal() &&
			(hidden.experimentsCompleted < (catacombsReset - TimeEnums.HALFANHOUR.time))) {
			map.put(
				6,
				DARK_AQUA + "Experiments: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.verySoonColour] +
					Utils.prettyTime(catacombsDiffNow)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.experimentationDisplay >= DISPLAYTYPE.SOON.ordinal() &&
			(hidden.experimentsCompleted < (catacombsReset - TimeEnums.HOUR.time))) {
			map.put(
				6,
				DARK_AQUA + "Experiments: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.soonColour] +
					Utils.prettyTime(catacombsDiffNow)
			);
		} else if (
			NotEnoughUpdates.INSTANCE.config.miscOverlays.experimentationDisplay >= DISPLAYTYPE.KINDASOON.ordinal() &&
				(hidden.experimentsCompleted < (catacombsReset - (TimeEnums.HOUR.time * 3)))) {
			map.put(
				6,
				DARK_AQUA + "Experiments: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.kindaSoonColour] +
					Utils.prettyTime(catacombsDiffNow)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.experimentationDisplay >= DISPLAYTYPE.ALWAYS.ordinal()) {
			map.put(
				6,
				DARK_AQUA + "Experiments: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.defaultColour] +
					Utils.prettyTime(catacombsDiffNow)
			);
		}

		// Daily Mithril Powder display
		if (hidden.dailyMithrilPowerCompleted < catacombsReset) {
			map.put(
				7,
				DARK_AQUA + "Mithril Powder: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.readyColour] + "Ready!"
			);
		} else if (
			NotEnoughUpdates.INSTANCE.config.miscOverlays.dailyMithrilPowderDisplay >= DISPLAYTYPE.VERYSOON.ordinal() &&
				(hidden.dailyMithrilPowerCompleted < (catacombsReset - TimeEnums.HALFANHOUR.time))) {
			map.put(
				7,
				DARK_AQUA + "Mithril Powder: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.verySoonColour] +
					Utils.prettyTime(catacombsDiffNow)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.dailyMithrilPowderDisplay >= DISPLAYTYPE.SOON.ordinal() &&
			(hidden.dailyMithrilPowerCompleted < (catacombsReset - TimeEnums.HOUR.time))) {
			map.put(
				7,
				DARK_AQUA + "Mithril Powder: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.soonColour] +
					Utils.prettyTime(catacombsDiffNow)
			);
		} else if (
			NotEnoughUpdates.INSTANCE.config.miscOverlays.dailyMithrilPowderDisplay >= DISPLAYTYPE.KINDASOON.ordinal() &&
				(hidden.dailyMithrilPowerCompleted < (catacombsReset - (TimeEnums.HOUR.time * 3)))) {
			map.put(
				7,
				DARK_AQUA + "Mithril Powder: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.kindaSoonColour] +
					Utils.prettyTime(catacombsDiffNow)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.dailyMithrilPowderDisplay >=
			DISPLAYTYPE.ALWAYS.ordinal()) {
			map.put(
				7,
				DARK_AQUA + "Mithril Powder: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.defaultColour] +
					Utils.prettyTime(catacombsDiffNow)
			);
		}

		// Daily Gemstone Powder Display
		if (hidden.dailyGemstonePowderCompleted < catacombsReset) {
			map.put(
				8,
				DARK_AQUA + "Gemstone Powder: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.readyColour] + "Ready!"
			);
		} else if (
			NotEnoughUpdates.INSTANCE.config.miscOverlays.dailyGemstonePowderDisplay >= DISPLAYTYPE.VERYSOON.ordinal() &&
				(hidden.dailyGemstonePowderCompleted < (catacombsReset - TimeEnums.HALFANHOUR.time))) {
			map.put(
				8,
				DARK_AQUA + "Gemstone Powder: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.verySoonColour] +
					Utils.prettyTime(catacombsDiffNow)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.dailyGemstonePowderDisplay >= DISPLAYTYPE.SOON.ordinal() &&
			(hidden.dailyGemstonePowderCompleted < (catacombsReset - TimeEnums.HOUR.time))) {
			map.put(
				8,
				DARK_AQUA + "Gemstone Powder: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.soonColour] +
					Utils.prettyTime(catacombsDiffNow)
			);
		} else if (
			NotEnoughUpdates.INSTANCE.config.miscOverlays.dailyGemstonePowderDisplay >= DISPLAYTYPE.KINDASOON.ordinal() &&
				(hidden.dailyGemstonePowderCompleted < (catacombsReset - (TimeEnums.HOUR.time * 3)))) {
			map.put(
				8,
				DARK_AQUA + "Gemstone Powder: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.kindaSoonColour] +
					Utils.prettyTime(catacombsDiffNow)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.dailyGemstonePowderDisplay >=
			DISPLAYTYPE.ALWAYS.ordinal()) {
			map.put(
				8,
				DARK_AQUA + "Gemstone Powder: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.defaultColour] +
					Utils.prettyTime(catacombsDiffNow)
			);
		}

		//Daily Heavy Pearl Display
		if (hidden.dailyHeavyPearlCompleted < pearlsReset) {
			map.put(
				9,
				DARK_AQUA + "Heavy Pearls: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.readyColour] + "Ready!"
			);
		} else if (
			NotEnoughUpdates.INSTANCE.config.miscOverlays.dailyHeavyPearlDisplay >= DISPLAYTYPE.VERYSOON.ordinal() &&
				(hidden.dailyHeavyPearlCompleted < (pearlsReset - TimeEnums.HALFANHOUR.time))) {
			map.put(
				9,
				DARK_AQUA + "Heavy Pearls: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.verySoonColour] +
					Utils.prettyTime(pearlsReset + 86400000 - currentTime)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.dailyHeavyPearlDisplay >= DISPLAYTYPE.SOON.ordinal() &&
			(hidden.dailyHeavyPearlCompleted < (pearlsReset - TimeEnums.HOUR.time))) {
			map.put(
				9,
				DARK_AQUA + "Heavy Pearls: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.soonColour] +
					Utils.prettyTime(pearlsReset + 86400000 - currentTime)
			);
		} else if (
			NotEnoughUpdates.INSTANCE.config.miscOverlays.dailyHeavyPearlDisplay >= DISPLAYTYPE.KINDASOON.ordinal() &&
				(hidden.dailyHeavyPearlCompleted < (pearlsReset - (TimeEnums.HOUR.time * 3)))) {
			map.put(
				9,
				DARK_AQUA + "Heavy Pearls: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.kindaSoonColour] +
					Utils.prettyTime(pearlsReset + 86400000 - currentTime)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.dailyHeavyPearlDisplay >=
			DISPLAYTYPE.ALWAYS.ordinal()) {
			map.put(
				9,
				DARK_AQUA + "Heavy Pearls: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.defaultColour] +
					Utils.prettyTime(pearlsReset + 86400000 - currentTime)
			);
		}
		//Daily Crimson Isle Quests
		if (hidden.questBoardCompleted < midnightReset) {
			map.put(
				10,
				DARK_AQUA + "Crimson Isle Quests: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.readyColour] +
					(5 - SBInfo.getInstance().completedQuests.size()) + " left!"
			);
		} else if (
			NotEnoughUpdates.INSTANCE.config.miscOverlays.questBoardDisplay >= DISPLAYTYPE.VERYSOON.ordinal() &&
				(hidden.questBoardCompleted < (midnightReset - TimeEnums.HALFANHOUR.time))) {
			map.put(
				10,
				DARK_AQUA + "Crimson Isle Quests: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.verySoonColour] +
					Utils.prettyTime(timeDiffMidnightNow)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.questBoardDisplay >= DISPLAYTYPE.SOON.ordinal() &&
			(hidden.questBoardCompleted < (midnightReset - TimeEnums.HOUR.time))) {
			map.put(
				10,
				DARK_AQUA + "Crimson Isle Quests: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.soonColour] +
					Utils.prettyTime(timeDiffMidnightNow)
			);
		} else if (
			NotEnoughUpdates.INSTANCE.config.miscOverlays.questBoardDisplay >= DISPLAYTYPE.KINDASOON.ordinal() &&
				(hidden.questBoardCompleted < (midnightReset - (TimeEnums.HOUR.time * 3)))) {
			map.put(
				10,
				DARK_AQUA + "Crimson Isle Quests: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.kindaSoonColour] +
					Utils.prettyTime(timeDiffMidnightNow)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.questBoardDisplay >= DISPLAYTYPE.ALWAYS.ordinal()) {
			map.put(
				10,
				DARK_AQUA + "Crimson Isle Quests: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.defaultColour] +
					Utils.prettyTime(timeDiffMidnightNow)
			);
		}

		//Daily Shop Limit
		if (hidden.dailyShopLimitCompleted < catacombsReset) {
			map.put(
				11,
				DARK_AQUA + "NPC Buy Daily Limit: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.readyColour] + "Ready!"
			);
		} else if (
			NotEnoughUpdates.INSTANCE.config.miscOverlays.shopLimitDisplay >= DISPLAYTYPE.VERYSOON.ordinal() &&
				(hidden.dailyShopLimitCompleted < (catacombsReset - TimeEnums.HALFANHOUR.time))) {
			map.put(
				11,
				DARK_AQUA + "NPC Buy Daily Limit: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.verySoonColour] +
					Utils.prettyTime(catacombsDiffNow)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.shopLimitDisplay >= DISPLAYTYPE.SOON.ordinal() &&
			(hidden.dailyShopLimitCompleted < (catacombsReset - TimeEnums.HOUR.time))) {
			map.put(
				11,
				DARK_AQUA + "NPC Buy Daily Limit: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.soonColour] +
					Utils.prettyTime(catacombsDiffNow)
			);
		} else if (
			NotEnoughUpdates.INSTANCE.config.miscOverlays.shopLimitDisplay >= DISPLAYTYPE.KINDASOON.ordinal() &&
				(hidden.dailyShopLimitCompleted < (catacombsReset - (TimeEnums.HOUR.time * 3)))) {
			map.put(
				11,
				DARK_AQUA + "NPC Buy Daily Limit: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.kindaSoonColour] +
					Utils.prettyTime(catacombsDiffNow)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.shopLimitDisplay >= DISPLAYTYPE.ALWAYS.ordinal()) {
			map.put(
				11,
				DARK_AQUA + "NPC Buy Daily Limit: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.defaultColour] +
					Utils.prettyTime(catacombsDiffNow)
			);
		}

		overlayStrings = new ArrayList<>();
		for (int index : NotEnoughUpdates.INSTANCE.config.miscOverlays.todoText2) {
			if (map.containsKey(index)) {
				String text = map.get(index);
				if (hideBecauseOfBingo(text)) continue;
				overlayStrings.add(text);
			}
		}
		if (overlayStrings.isEmpty()) overlayStrings = null;
	}

	private boolean hideBecauseOfBingo(String text) {
		if (!SBInfo.getInstance().bingo) return false;
		if (!NotEnoughUpdates.INSTANCE.config.miscOverlays.todoOverlayHideAtBingo) return false;

		if (text.contains("Cookie Buff")) return true;
		if (text.contains("Godpot")) return true;
		if (text.contains("Heavy Pearls")) return true;
		if (text.contains("Crimson Isle Quests")) return true;

		return false;
	}

	public static int beforePearls = -1;
	public static int afterPearls = -1;
	public static int availablePearls = -1;

	public static int heavyPearlCount() {
		int heavyPearls = 0;

		List<ItemStack> inventory = Minecraft.getMinecraft().thePlayer.inventoryContainer.getInventory();
		for (ItemStack item : inventory) {
			if (item == null) {
				continue;
			} else if (!item.hasTagCompound()) {
				continue;
			}
			NBTTagCompound itemData = item.getSubCompound("ExtraAttributes", false);
			if (itemData == null) {
				continue;
			}
			if (itemData.getString("id").equals("HEAVY_PEARL")) {
				heavyPearls += item.stackSize;
			}
		}
		return heavyPearls;
	}

	public static void processActionBar(String msg) {
		if (SBInfo.getInstance().location.equals("Belly of the Beast") && msg.contains("Pearls Collected")) {
			try {
				msg = Utils.cleanColour(msg);
				msg = msg.substring(msg.indexOf("Pearls Collected: ") + 18);
				availablePearls = Integer.parseInt(msg.substring(msg.indexOf("/") + 1));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private enum TimeEnums {
		DAY(86400000),
		HALFDAY(43200000),
		HOUR(3600000),
		HALFANHOUR(1800000);

		TimeEnums(long time) {
			this.time = time;
		}

		public final long time;
	}

	private enum DISPLAYTYPE {
		NOW,
		VERYSOON,
		SOON,
		KINDASOON,
		ALWAYS,
	}
}
