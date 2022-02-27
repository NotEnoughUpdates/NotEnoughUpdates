package io.github.moulberry.notenoughupdates.overlays;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.notenoughupdates.options.NEUConfig;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.util.vector.Vector2f;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.util.EnumChatFormatting.DARK_AQUA;

public class TimersOverlay extends TextOverlay {
	private static final Pattern PATTERN_ACTIVE_EFFECTS = Pattern.compile(
		"\u00a7r\u00a7r\u00a77You have a \u00a7r\u00a7cGod Potion \u00a7r\u00a77active! \u00a7r\u00a7d([0-9]*?:?[0-9]*?:?[0-9]*)\u00a7r");

	public TimersOverlay(
		Position position,
		Supplier<List<String>> dummyStrings,
		Supplier<TextOverlayStyle> styleSupplier
	) {
		super(position, dummyStrings, styleSupplier);
	}

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
			case "Daily Mithril Powder":
				icon = NotEnoughUpdates.INSTANCE.manager.jsonToStack(NotEnoughUpdates.INSTANCE.manager
					.getItemInformation()
					.get("MITHRIL_ORE"));
				break;
			case "Daily Gemstone Powder":
				icon = NotEnoughUpdates.INSTANCE.manager.jsonToStack(NotEnoughUpdates.INSTANCE.manager
					.getItemInformation()
					.get("PERFECT_AMETHYST_GEM"));
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

	@Override
	public void update() {

		long currentTime = System.currentTimeMillis();

		NEUConfig.HiddenProfileSpecific hidden = NotEnoughUpdates.INSTANCE.config.getProfileSpecific();
		if (hidden == null) return;

		if (Minecraft.getMinecraft().currentScreen instanceof GuiChest) {
			GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
			ContainerChest container = (ContainerChest) chest.inventorySlots;
			IInventory lower = container.getLowerChestInventory();
			String containerName = lower.getDisplayName().getUnformattedText();

			if (containerName.equals("Commissions") && lower.getSizeInventory() >= 18) {
				if (hidden.commissionsCompleted == 0) {
					hidden.commissionsCompleted = currentTime;
				}
				for (int i = 9; i < 18; i++) {
					ItemStack stack = lower.getStackInSlot(i);
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
			} else if (containerName.equals("Experimentation Table") && lower.getSizeInventory() >= 36) {
				ItemStack stack = lower.getStackInSlot(31);
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
				return;
			} else if (containerName.equals("Superpairs Rewards") && lower.getSizeInventory() >= 27) {
				ItemStack stack = lower.getStackInSlot(13);
				if (stack != null && Utils.cleanColour(stack.getDisplayName()).equals("Superpairs")) {
					hidden.experimentsCompleted = currentTime;
				}
			}
		}

		boolean foundCookieBuffText = false;
		boolean foundGodPotText = false;
		if (SBInfo.getInstance().getLocation() != null && !SBInfo.getInstance().getLocation().equals("dungeon") &&
			SBInfo.getInstance().footer != null) {
			String formatted = SBInfo.getInstance().footer.getFormattedText();
			for (String line : formatted.split("\n")) {
				Matcher activeEffectsMatcher = PATTERN_ACTIVE_EFFECTS.matcher(line);
				if (activeEffectsMatcher.matches()) {
					foundGodPotText = true;
					String[] godpotRemaingTimeUnformatted = activeEffectsMatcher.group(1).split(":");
					long godPotDuration = 0;
					try {
						int i = 0;
						if (godpotRemaingTimeUnformatted.length == 4) {
							godPotDuration =
								godPotDuration + (long) Integer.parseInt(godpotRemaingTimeUnformatted[i]) * 24 * 60 * 60 * 1000;
							i++;
						}
						if (godpotRemaingTimeUnformatted.length >= 3) {
							godPotDuration =
								godPotDuration + (long) Integer.parseInt(godpotRemaingTimeUnformatted[i]) * 60 * 60 * 1000;
							i++;
						}
						if (godpotRemaingTimeUnformatted.length >= 2) {
							godPotDuration = godPotDuration + (long) Integer.parseInt(godpotRemaingTimeUnformatted[i]) * 60 * 1000;
							i++;
						}
						if (godpotRemaingTimeUnformatted.length >= 1) {
							godPotDuration = godPotDuration + (long) Integer.parseInt(godpotRemaingTimeUnformatted[i]) * 1000;
						}
					} catch (Exception ignored) {
					}

					hidden.godPotionDuration = godPotDuration;

				} else if (line.contains("\u00a7d\u00a7lCookie Buff")) {
					foundCookieBuffText = true;
				} else if (foundCookieBuffText) {
					String cleanNoSpace = line.replaceAll("(\u00a7.| )", "");

					hidden.cookieBuffRemaining = 0;
					StringBuilder number = new StringBuilder();
					for (int i = 0; i < cleanNoSpace.length(); i++) {
						char c = cleanNoSpace.charAt(i);

						if (c >= '0' && c <= '9') {
							number.append(c);
						} else {
							if (number.length() == 0) {
								hidden.cookieBuffRemaining = 0;
								break;
							}
							if ("ydhms".contains("" + c)) {
								try {
									long val = Integer.parseInt(number.toString());
									switch (c) {
										case 'y':
											hidden.cookieBuffRemaining += val * 365 * 24 * 60 * 60 * 1000;
											break;
										case 'd':
											hidden.cookieBuffRemaining += val * 24 * 60 * 60 * 1000;
											break;
										case 'h':
											hidden.cookieBuffRemaining += val * 60 * 60 * 1000;
											break;
										case 'm':
											hidden.cookieBuffRemaining += val * 60 * 1000;
											break;
										case 's':
											hidden.cookieBuffRemaining += val * 1000;
											break;
									}
								} catch (NumberFormatException e) {
									hidden.cookieBuffRemaining = 0;
									break;
								}

								number = new StringBuilder();
							} else {
								hidden.cookieBuffRemaining = 0;
								break;
							}
						}
					}

					break;
				}
			}
		}

		if (!foundGodPotText) {
			hidden.godPotionDuration = 0;
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
					Utils.prettyTime(hidden.cookieBuffRemaining)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.cookieBuffDisplay >= DISPLAYTYPE.SOON.ordinal() &&
			hidden.cookieBuffRemaining < TimeEnums.HALFDAY.time) {
			map.put(
				1,
				DARK_AQUA + "Cookie Buff: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.soonColour] +
					Utils.prettyTime(hidden.cookieBuffRemaining)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.cookieBuffDisplay >= DISPLAYTYPE.KINDASOON.ordinal() &&
			hidden.cookieBuffRemaining < TimeEnums.DAY.time) {
			map.put(
				1,
				DARK_AQUA + "Cookie Buff: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.kindaSoonColour] +
					Utils.prettyTime(hidden.cookieBuffRemaining)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.cookieBuffDisplay >= DISPLAYTYPE.ALWAYS.ordinal()) {
			map.put(
				1,
				DARK_AQUA + "Cookie Buff: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.defaultColour] +
					Utils.prettyTime(hidden.cookieBuffRemaining)
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

		long midnightReset = (currentTime - 18000000) / 86400000 * 86400000 + 18000000; // 12am est
		long catacombsReset = currentTime / 86400000 * 86400000; // 7pm est
		long timeDiffMidnightNow = midnightReset + 86400000 - currentTime;
		long catacombsDiffNow = catacombsReset + 86400000 - currentTime;
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
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.readyColour] + "Ready!"
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
		if (hidden.experimentsCompleted < midnightReset) {
			map.put(
				6,
				DARK_AQUA + "Experiments: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.readyColour] + "Ready!"
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.experimentationDisplay >= DISPLAYTYPE.VERYSOON.ordinal() &&
			(hidden.experimentsCompleted < (midnightReset - TimeEnums.HALFANHOUR.time))) {
			map.put(
				6,
				DARK_AQUA + "Experiments: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.verySoonColour] +
					Utils.prettyTime(catacombsReset)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.experimentationDisplay >= DISPLAYTYPE.SOON.ordinal() &&
			(hidden.experimentsCompleted < (midnightReset - TimeEnums.HOUR.time))) {
			map.put(
				6,
				DARK_AQUA + "Experiments: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.soonColour] +
					Utils.prettyTime(catacombsReset)
			);
		} else if (
			NotEnoughUpdates.INSTANCE.config.miscOverlays.experimentationDisplay >= DISPLAYTYPE.KINDASOON.ordinal() &&
				(hidden.experimentsCompleted < (midnightReset - (TimeEnums.HOUR.time * 3)))) {
			map.put(
				6,
				DARK_AQUA + "Experiments: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.kindaSoonColour] +
					Utils.prettyTime(catacombsReset)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.experimentationDisplay >= DISPLAYTYPE.ALWAYS.ordinal()) {
			map.put(
				6,
				DARK_AQUA + "Experiments: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.defaultColour] +
					Utils.prettyTime(catacombsReset)
			);
		}

		// Daily Mithril Powder display
		long mithrilPowderCompleted = hidden.dailyMithrilPowerCompleted + 1000 * 60 * 60 * 24 - currentTime;

		if (hidden.dailyMithrilPowerCompleted < midnightReset) {
			map.put(
				7,
				DARK_AQUA + "Daily Mithril Powder: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.readyColour] + "Ready!"
			);
		} else if (
			NotEnoughUpdates.INSTANCE.config.miscOverlays.dailyMithrilPowderDisplay >= DISPLAYTYPE.VERYSOON.ordinal() &&
				(hidden.dailyMithrilPowerCompleted < (midnightReset - TimeEnums.HALFANHOUR.time))) {
			map.put(
				7,
				DARK_AQUA + "Daily Mithril Powder: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.verySoonColour] +
					Utils.prettyTime(timeDiffMidnightNow)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.dailyMithrilPowderDisplay >= DISPLAYTYPE.SOON.ordinal() &&
			(hidden.dailyMithrilPowerCompleted < (midnightReset - TimeEnums.HOUR.time))) {
			map.put(
				7,
				DARK_AQUA + "Daily Mithril Powder: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.soonColour] +
					Utils.prettyTime(timeDiffMidnightNow)
			);
		} else if (
			NotEnoughUpdates.INSTANCE.config.miscOverlays.dailyMithrilPowderDisplay >= DISPLAYTYPE.KINDASOON.ordinal() &&
				(hidden.dailyMithrilPowerCompleted < (midnightReset - (TimeEnums.HOUR.time * 3)))) {
			map.put(
				7,
				DARK_AQUA + "Daily Mithril Powder: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.kindaSoonColour] +
					Utils.prettyTime(timeDiffMidnightNow)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.dailyMithrilPowderDisplay >=
			DISPLAYTYPE.ALWAYS.ordinal()) {
			map.put(
				7,
				DARK_AQUA + "Daily Mithril Powder: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.defaultColour] +
					Utils.prettyTime(timeDiffMidnightNow)
			);
		}

		// Daily Gemstone Powder Display
		if (hidden.dailyGemstonePowderCompleted < midnightReset) {
			map.put(
				8,
				DARK_AQUA + "Daily Gemstone Powder: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.readyColour] + "Ready!"
			);
		} else if (
			NotEnoughUpdates.INSTANCE.config.miscOverlays.dailyGemstonePowderDisplay >= DISPLAYTYPE.VERYSOON.ordinal() &&
				(hidden.dailyGemstonePowderCompleted < (midnightReset - TimeEnums.HALFANHOUR.time))) {
			map.put(
				8,
				DARK_AQUA + "Daily Gemstone Powder: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.verySoonColour] +
					Utils.prettyTime(timeDiffMidnightNow)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.dailyGemstonePowderDisplay >= DISPLAYTYPE.SOON.ordinal() &&
			(hidden.dailyGemstonePowderCompleted < (midnightReset - TimeEnums.HOUR.time))) {
			map.put(
				8,
				DARK_AQUA + "Daily Gemstone Powder: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.soonColour] +
					Utils.prettyTime(timeDiffMidnightNow)
			);
		} else if (
			NotEnoughUpdates.INSTANCE.config.miscOverlays.dailyGemstonePowderDisplay >= DISPLAYTYPE.KINDASOON.ordinal() &&
				(hidden.dailyGemstonePowderCompleted < (midnightReset - (TimeEnums.HOUR.time * 3)))) {
			map.put(
				8,
				DARK_AQUA + "Daily Gemstone Powder: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.kindaSoonColour] +
					Utils.prettyTime(timeDiffMidnightNow)
			);
		} else if (NotEnoughUpdates.INSTANCE.config.miscOverlays.dailyGemstonePowderDisplay >=
			DISPLAYTYPE.ALWAYS.ordinal()) {
			map.put(
				8,
				DARK_AQUA + "Daily Gemstone Powder: " +
					EnumChatFormatting.values()[NotEnoughUpdates.INSTANCE.config.miscOverlays.defaultColour] +
					Utils.prettyTime(timeDiffMidnightNow)
			);
		}

		overlayStrings = new ArrayList<>();
		for (int index : NotEnoughUpdates.INSTANCE.config.miscOverlays.todoText2) {
			if (map.containsKey(index)) {
				overlayStrings.add(map.get(index));
			}
		}
		if (overlayStrings.isEmpty()) overlayStrings = null;
	}

	public String compactRemaining(int amount) {
		return (5 - amount) + " remaining";
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

