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

package io.github.moulberry.notenoughupdates.miscgui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.core.BackgroundBlur;
import io.github.moulberry.notenoughupdates.util.ItemUtils;
import io.github.moulberry.notenoughupdates.util.SkyBlockTime;
import io.github.moulberry.notenoughupdates.util.Utils;
import kotlin.Pair;
import lombok.var;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.github.moulberry.notenoughupdates.util.GuiTextures.help;

@NEUAutoSubscribe
public class CalendarOverlay {
	private static final ResourceLocation BACKGROUND = new ResourceLocation("notenoughupdates:calendar/background.png");
	private static final ResourceLocation DISPLAYBAR = new ResourceLocation("notenoughupdates:calendar/displaybar.png");
	private static final ResourceLocation TOAST = new ResourceLocation("notenoughupdates:calendar/toast.png");

	private static JsonObject farmingEventTypes = null;

	private static boolean enabled = false;

	public static boolean ableToClickCalendar = true;

	public static void setEnabled(boolean enabled) {
		CalendarOverlay.enabled = enabled;
	}

	public static boolean isEnabled() {
		return enabled;
	}

	private int guiLeft = -1;
	private int guiTop = -1;
	private int xSize = 168;
	private int ySize = 170;

	private static final Pattern CALENDAR_PATTERN = Pattern.compile(
		"((?:Early | Late )?(?:Spring|Summer|Fall|Winter)), Year ([0-9]+)");

	private int jingleIndex = -1;

	private final TreeMap<Long, Set<SBEvent>> eventMap = new TreeMap<>();
	private List<String> jfFavouriteSelect = null;
	private int jfFavouriteSelectIndex = 0;
	private int jfFavouriteSelectX = 0;
	private int jfFavouriteSelectY = 0;
	List<Tuple<Long, SBEvent>> specialEvents = new ArrayList<>();

	private boolean drawTimerForeground = false;

	private static long spookyStart = 0;

	private static final long SECOND = 1000;
	private static final long MINUTE = SECOND * 60;
	private static final long HOUR = MINUTE * 60;
	private static final long DAY = HOUR * 24;

	private static final long DA_OFFSET = 1000 * 60 * 55; // Dark Auction
	private static final long JF_OFFSET = 1000 * 60 * 15; // Jacob's Farming Contest

	private static final ItemStack DA_STACK; // Dark Auction
	private static final ItemStack JF_STACK; // Jacob's Farming Contest

	static {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("event_id", "dark_auction");
		//tag.setTag("ench", new NBTTagList());

		DA_STACK = new ItemStack(Items.netherbrick);
		DA_STACK.setTagCompound(tag);

		tag.setString("event_id", "jacob_farming");
		JF_STACK = new ItemStack(Items.wheat);
		JF_STACK.setTagCompound(tag);
	}

	public long getTimeOffset(String time) {
		long offset = 0;

		StringBuilder numS = new StringBuilder();
		for (int timeIndex = 0; timeIndex < time.length(); timeIndex++) {
			char c = time.charAt(timeIndex);

			if (c >= '0' && c <= '9') {
				numS.append(c);
			} else {
				try {
					int num = Integer.parseInt(numS.toString());
					switch (c) {
						case 'd':
							offset += num * DAY;
							continue;
						case 'h':
							offset += num * HOUR;
							continue;
						case 'm':
							offset += num * MINUTE;
							continue;
						case 's':
							offset += num * SECOND;
							continue;
					}
				} catch (Exception ignored) {
				}
				numS = new StringBuilder();
			}
		}

		return offset;
	}

	private static final long SKYBLOCK_START = 1559829300000L; //Day 0, Year 0

	public void handleJinglePlayer() {
		if (jingleIndex == 0) {
			if (NotEnoughUpdates.INSTANCE.config.calendar.eventNotificationSounds) {
				Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(
					new ResourceLocation("notenoughupdates:calendar_notif_jingle")
				));
				Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(
					new ResourceLocation("notenoughupdates:calendar_notif_in")
				));
			}
			jingleIndex = -15 * 20;
		} else if (jingleIndex >= 1) {
			if (NotEnoughUpdates.INSTANCE.config.calendar.eventNotificationSounds) {
				Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(
					new ResourceLocation("notenoughupdates:calendar_notif_in")
				));
			}
			jingleIndex = -15 * 20;
		} else if (jingleIndex < -1) {
			jingleIndex++;
		}
		if (jingleIndex == -20 * 6 - 10) {
			if (NotEnoughUpdates.INSTANCE.config.calendar.eventNotificationSounds) {
				Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(
					new ResourceLocation("notenoughupdates:calendar_notif_out")
				));
			}
		}
	}

	public Set<SBEvent> getEventsAt(long timestamp) {
		return eventMap.computeIfAbsent(timestamp, k -> new HashSet<>());
	}

	JsonObject getFarmingEventTypes() {
		if (farmingEventTypes == null) {
			farmingEventTypes = NotEnoughUpdates.INSTANCE.manager.getJsonFromFile(new File(
				NotEnoughUpdates.INSTANCE.manager.configLocation,
				"farmingEventTypes.json"
			));
			if (farmingEventTypes == null) {
				farmingEventTypes = new JsonObject();
			}
		}
		return farmingEventTypes;
	}

	public void fillRepeatingEvents(int nextHours) {
		long currentTime = System.currentTimeMillis();
		long floorHour = (currentTime / HOUR) * HOUR;
		for (int i = 0; i < nextHours; i++) {
			long daEvent = floorHour + i * HOUR + DA_OFFSET;
			long jfEvent = floorHour + i * HOUR + JF_OFFSET;

			if (daEvent > currentTime) {
				getEventsAt(daEvent).add(new SBEvent("dark_auction",
					EnumChatFormatting.DARK_PURPLE + "Dark Auction", false, DA_STACK, null, MINUTE * 5
				));
			}
			if (jfEvent > currentTime) {
				SBEvent jf = new SBEvent("jacob_farming",
					EnumChatFormatting.YELLOW + "Jacob's Farming Contest", false, JF_STACK, null, MINUTE * 20
				);
				if (farmingEventTypes != null && farmingEventTypes.has("" + jfEvent) &&
					farmingEventTypes.get("" + jfEvent).isJsonArray()) {
					JsonArray arr = farmingEventTypes.get("" + jfEvent).getAsJsonArray();
					jf.desc = new ArrayList<>();
					for (JsonElement e : arr) {
						jf.desc.add(EnumChatFormatting.YELLOW + "\u25CB " + e.getAsString());
						jf.id += ":" + e.getAsString();
					}
				}
				getEventsAt(jfEvent).add(jf);
			}
		}
	}

	public void populateDefaultEvents() {
		if (eventMap.isEmpty() || eventMap.size() <= 20) {
			fillRepeatingEvents(25 - eventMap.size());
			fillSpecialMayors(4);
			fillWeather();
		}
	}

	private void fillWeather() {
		long rainInterval = 4850 * 1000L;
		long rainingTime = 1000 * 1000L;
		long thunderStormEpoch = 1668551956000L - rainingTime;
		long currentTime = System.currentTimeMillis();
		long timeSinceLastThunderStart = (currentTime - thunderStormEpoch) % (rainInterval * 4);
		long lastThunderStart = currentTime - timeSinceLastThunderStart;
		for (int i = 0; i < 11; i++) {
			long eventTimer = lastThunderStart + rainInterval * i;
			if (i % 4 == 0) {
				addEvent(SkyBlockTime.Companion.fromInstant(Instant.ofEpochMilli(eventTimer)), new SBEvent(
					"spiders_den_thunder",
					"§9Spider's Den Thunder",
					true,
					new ItemStack(Blocks.slime_block),
					Arrays.asList("§aIt will rain in the Spider's Den", "§aand Toxic Rain Slimes will spawn"),
					rainingTime,
					true
				));
			} else {
				addEvent(
					SkyBlockTime.Companion.fromInstant(Instant.ofEpochMilli(eventTimer)),
					new SBEvent(
						"spiders_den_rain",
						"§9Spider's Den Rain",
						false,
						new ItemStack(Items.slime_ball),
						Arrays.asList("§aIt will rain in the Spider's Den", "§aand Rain Slimes will spawn"),
						rainingTime,
						true
					)
				);
			}

		}
	}

	@SubscribeEvent
	public void tick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.START) return;
		handleJinglePlayer();

		getFarmingEventTypes();

		if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) {
			jfFavouriteSelect = null;
			populateDefaultEvents();
			return;
		}

		GuiChest eventGui = (GuiChest) Minecraft.getMinecraft().currentScreen;
		ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
		String containerName = cc.getLowerChestInventory().getDisplayName().getUnformattedText();

		Matcher matcher = CALENDAR_PATTERN.matcher(Utils.cleanColour(containerName));
		if (farmingEventTypes != null && matcher.matches()) scrapeMonthlyCalendar(matcher, cc);

		if (!enabled) {
			jfFavouriteSelect = null;
			populateDefaultEvents();
			return;
		}

		if (!containerName.trim().equals("Calendar and Events")) {
			setEnabled(false);
			return;
		}

		eventMap.clear();

		populateDefaultEvents();
		scrapeOverviewPage(cc);
	}

	public void addEvent(SkyBlockTime time, SBEvent event) {
		if (time.toInstant().isBefore(Instant.now())&&
			time.toInstant().plus(event.lastsFor, ChronoUnit.MILLIS).isBefore(Instant.now())) return;
		getEventsAt(time.toMillis()).add(event);
	}

	private void fillSpecialMayors(int cycles) {
		int currentYear = SkyBlockTime.now().getYear();
		int baseYear = currentYear - currentYear % 24;
		int scorpiusOffset = 192 % 24;
		int derpyOffset = 200 % 24;
		int jerryOffset = 208 % 24;
		for (int i = 0; i < cycles; i++) {
			int thisBaseYear = baseYear + i * 24 + 1;
			addEvent(
				new SkyBlockTime(thisBaseYear + scorpiusOffset, 3, 27, 0, 0, 0),
				new SBEvent("special_mayor:scorpius",
					"§dScorpius",
					true,
					Utils.createSkull(
						"Scorpius",
						"ba2cd37d-a0e4-4dc5-b15c-d79ee1051aae",
						"ewogICJ0aW1lc3RhbXAiIDogMTU5Nzc4MTc1NzIxOSwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGYyNmZhMGM0NzUzNmU3OGUzMzcyNTdkODk4YWY4YjFlYmM4N2MwODk0NTAzMzc1MjM0MDM1ZmYyYzdlZjhmMCIKICAgIH0KICB9Cn0"
					),
					Arrays.asList("§eScorpius is a special Mayor candidate"), -1, true
				)
			);
			addEvent(
				new SkyBlockTime(thisBaseYear + derpyOffset, 3, 27, 0, 0, 0),
				new SBEvent(
					"special_mayor:derpy",
					"§dDerpy",
					true,
					Utils.createSkull(
						"Derpy",
						"ab36a707-96d3-3db1-ab36-a70796d3adb1",
						"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjQ1MGQxMjY5Mjg4NmM0N2IwNzhhMzhmNGQ0OTJhY2ZlNjEyMTZlMmQwMjM3YWI4MjQzMzQwOWIzMDQ2YjQ2NCJ9fX0"
					),
					Arrays.asList("§eDerpy is a special Mayor candidate"), -1, true
				)
			);
			addEvent(
				new SkyBlockTime(thisBaseYear + jerryOffset, 3, 27, 0, 0, 0),
				new SBEvent(
					"special_mayor:jerry",
					"§dJerry",
					true,
					Utils.createSkull(
						"Jerry",
						"0a9e8efb-9191-4c81-80f5-e27ca5433156",
						"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODIyZDhlNzUxYzhmMmZkNGM4OTQyYzQ0YmRiMmY1Y2E0ZDhhZThlNTc1ZWQzZWIzNGMxOGE4NmU5M2IifX19"
					),
					Arrays.asList("§eJerry is a special Mayor candidate"), -1, true
				)
			);
		}
	}

	private void scrapeOverviewPage(ContainerChest cc) {
		long currentTime = System.currentTimeMillis();
		String lastsForText = EnumChatFormatting.GRAY + "Event lasts for " + EnumChatFormatting.YELLOW;
		String startsInText = EnumChatFormatting.GRAY + "Starts in: " + EnumChatFormatting.YELLOW;
		for (int i = 0; i < 21; i++) {
			int itemIndex = 10 + i + (i / 7) * 2;
			ItemStack item = cc.getLowerChestInventory().getStackInSlot(itemIndex);
			if (item == null) continue;
			List<String> lore = ItemUtils.getLore(item);
			if (lore.isEmpty()) continue;
			String first = lore.get(0);
			if (first.startsWith(startsInText)) {
				String time = Utils.cleanColour(first.substring(startsInText.length()));
				long eventTime = currentTime + getTimeOffset(time);

				long lastsFor = -1;

				List<String> desc = new ArrayList<>();
				boolean foundBreak = false;
				for (String line : lore) {
					if (foundBreak) {
						desc.add(line);
					} else {
						if (line.startsWith(lastsForText)) {
							String lastsForS = Utils.cleanColour(line.substring(lastsForText.length()));
							lastsFor = getTimeOffset(lastsForS);
						}
						if (Utils.cleanColour(line).trim().length() == 0) {
							foundBreak = true;
						}
					}
				}
				getEventsAt(eventTime).add(new SBEvent(
					getIdForDisplayName(item.getDisplayName()), item.getDisplayName(),
					true, item, desc, lastsFor
				));
			}
		}
	}

	private void scrapeMonthlyCalendar(Matcher matcher, ContainerChest cc) {
		try {
			int year = Integer.parseInt(matcher.group(2));

			String month = matcher.group(1);
			boolean changed = false;
			for (int i = 0; i < 31; i++) {
				ItemStack item = cc.getLowerChestInventory().getStackInSlot(1 + (i % 7) + (i / 7) * 9);
				if (item == null) continue;
				SkyBlockTime sbt = SkyBlockTime.Companion.fromDayMonthYear(i + 1, month, year);
				if (sbt == null) continue;
				JsonArray array = new JsonArray();

				for (String line : ItemUtils.getLore(item)) {
					if (line.startsWith(EnumChatFormatting.YELLOW + "\u25CB")) {
						array.add(new JsonPrimitive(Utils.cleanColour(line.substring(4))));
					}
				}
				if (array.size() == 3) {
					String prop = String.valueOf(sbt.toMillis());
					changed |= (!farmingEventTypes.has(prop) || !farmingEventTypes.get(prop).isJsonArray() ||
						farmingEventTypes.get(prop).getAsJsonArray().equals(array));
					farmingEventTypes.add(prop, array);
				}
			}
			if (changed) {
				File f = new File(
					NotEnoughUpdates.INSTANCE.manager.configLocation,
					"farmingEventTypes.json"
				);
				NotEnoughUpdates.INSTANCE.manager.writeJson(farmingEventTypes, f);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SubscribeEvent
	public void onGuiDraw(GuiScreenEvent.DrawScreenEvent.Pre event) {
		if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) {
			return;
		}

		if (!enabled) {
			return;
		}

		GuiChest eventGui = (GuiChest) Minecraft.getMinecraft().currentScreen;
		ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
		String containerName = cc.getLowerChestInventory().getDisplayName().getUnformattedText();
		if (!containerName.trim().equals("Calendar and Events")) {
			setEnabled(false);
			return;
		}

		event.setCanceled(true);

		List<String> tooltipToDisplay = null;
		int mouseX = event.mouseX;
		int mouseY = event.mouseY;
		long currentTime = System.currentTimeMillis();

		xSize = 168;
		ySize = 170;

		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		int width = scaledResolution.getScaledWidth();
		int height = scaledResolution.getScaledHeight();
		guiLeft = (width - xSize) / 2;
		guiTop = (height - ySize) / 2;

		Utils.drawGradientRect(0, 0, width, height, -1072689136, -804253680);

		renderBlurredBackground(10, width, height, guiLeft + 3, guiTop + 3, 162, 14);
		renderBlurredBackground(10, width, height, guiLeft + 3, guiTop + 26, 14, 141);
		renderBlurredBackground(10, width, height, guiLeft + 151, guiTop + 26, 14, 141);
		renderBlurredBackground(10, width, height, guiLeft + 26, guiTop + 26, 116, 141);

		Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND);
		Utils.drawTexturedRect(guiLeft, guiTop, xSize, ySize, GL11.GL_NEAREST);

		GlStateManager.translate(0, 0, 10);

		FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

		fr.drawString("Daily", guiLeft + 29, guiTop + 30, 0xffffaa00);
		int specialLen = fr.getStringWidth("Special");
		fr.drawString("Special", guiLeft + 139 - specialLen, guiTop + 30, 0xffffaa00);

		ItemStack mayorStack = cc.getLowerChestInventory().getStackInSlot(37);
		if (mayorStack != null) {
			String mayor = mayorStack.getDisplayName();
			float verticalHeight = Utils.getVerticalHeight(mayor);
			Utils.drawStringVertical(mayor, guiLeft + 8, guiTop + 96 - verticalHeight / 2,
				false, -1
			);
		}

		String calendar = EnumChatFormatting.GREEN + "Calendar";
		float calendarHeight = Utils.getVerticalHeight(calendar);
		Utils.drawStringVertical(calendar, guiLeft + xSize - 12, guiTop + 60 - calendarHeight / 2,
			false, -1
		);

		String rewards = EnumChatFormatting.GOLD + "Rewards";
		float rewardsHeight = Utils.getVerticalHeight(rewards);
		Utils.drawStringVertical(rewards, guiLeft + xSize - 12, guiTop + 132 - rewardsHeight / 2,
			false, -1
		);

		if (mouseY >= guiTop + 26 && mouseY <= guiTop + 26 + 141) {
			if (mouseX >= guiLeft + 3 && mouseX <= guiLeft + 3 + 14) {
				if (mayorStack != null)
					tooltipToDisplay = mayorStack.getTooltip(Minecraft.getMinecraft().thePlayer, false);
			} else if (mouseX >= guiLeft + 151 && mouseX <= guiLeft + 151 + 14) {
				if (mouseY <= guiTop + 26 + 70) {
					ItemStack calendarStack = cc.getLowerChestInventory().getStackInSlot(41);
					if (calendarStack != null)
						tooltipToDisplay = calendarStack.getTooltip(Minecraft.getMinecraft().thePlayer, false);
				} else {
					ItemStack rewardsStack = cc.getLowerChestInventory().getStackInSlot(36);
					if (rewardsStack != null)
						tooltipToDisplay = rewardsStack.getTooltip(Minecraft.getMinecraft().thePlayer, false);
				}
			}
		}

		long timeUntilNext = 0;
		SBEvent nextEvent = null;
		long timeUntilFirst = 0;
		SBEvent firstEvent = null;
		List<String> eventFavourites = NotEnoughUpdates.INSTANCE.config.hidden.eventFavourites;

		//Daily Events
		int index = 0;
		out:
		for (Map.Entry<Long, Set<SBEvent>> sbEvents : eventMap.entrySet()) {
			for (SBEvent sbEvent : sbEvents.getValue()) {
				long timeUntilMillis = sbEvents.getKey() - currentTime;

				int x = guiLeft + 29 + 17 * (index % 3);
				int y = guiTop + 44 + 17 * (index / 3);

				if (sbEvent.id.equals("spooky_festival")) {
					if (sbEvents.getKey() > currentTime - HOUR && (sbEvents.getKey() < spookyStart || spookyStart == 0)) {
						spookyStart = sbEvents.getKey();
					}
				}

				if (index >= 21) {
					if (nextEvent != null) break;
					if (eventFavourites.isEmpty()) {
						nextEvent = sbEvent;
						timeUntilNext = timeUntilMillis;
					} else if (eventFavourites.contains(sbEvent.id)) {
						nextEvent = sbEvent;
						timeUntilNext = timeUntilMillis;
					}
					continue;
				}

				if (firstEvent == null) {
					firstEvent = sbEvent;
					timeUntilFirst = timeUntilMillis;
				}

				String[] split = sbEvent.id.split(":");
				boolean containsId = false;
				for (int i = 1; i < split.length; i++) {
					if (eventFavourites.contains(split[0] + ":" + split[i])) {
						containsId = true;
						break;
					}
				}
				if (eventFavourites.isEmpty()) {
					if (nextEvent == null) {
						nextEvent = sbEvent;
						timeUntilNext = timeUntilMillis;
					}
				} else if (eventFavourites.contains(split[0]) || containsId) {
					if (nextEvent == null) {
						nextEvent = sbEvent;
						timeUntilNext = timeUntilMillis;
					}

					GlStateManager.depthMask(false);
					GlStateManager.translate(0, 0, -2);
					Gui.drawRect(x, y, x + 16, y + 16, 0xcfffbf49);
					GlStateManager.translate(0, 0, 2);
					GlStateManager.depthMask(true);
				}

				Utils.drawItemStackWithText(sbEvent.stack, x, y, "" + (index + 1));

				if (mouseX >= x && mouseX <= x + 16) {
					if (mouseY >= y && mouseY <= y + 16) {
						tooltipToDisplay = Utils.createList(
							sbEvent.display,
							EnumChatFormatting.GRAY + "Starts in: " + EnumChatFormatting.YELLOW + prettyTime(timeUntilMillis, false)
						);
						if (sbEvent.lastsFor >= 0) {
							tooltipToDisplay.add(EnumChatFormatting.GRAY + "Lasts for: " + EnumChatFormatting.YELLOW +
								prettyTime(sbEvent.lastsFor, true));
							if (timeUntilMillis < 0) {
								tooltipToDisplay.add(EnumChatFormatting.GRAY + "Time left: " + EnumChatFormatting.YELLOW +
									prettyTime(sbEvent.lastsFor + timeUntilMillis, true));
							}
						}
						if (sbEvent.desc != null) {
							tooltipToDisplay.add("");
							tooltipToDisplay.addAll(sbEvent.desc);
						}
					}
				}

				index++;
			}
		}

		//Special Events
		specialEvents = eventMap.entrySet().stream()
														.flatMap(pair -> pair
															.getValue()
															.stream()
															.map(it -> new Tuple<>(pair.getKey(), it)))
														.filter(it -> it.getSecond().isSpecial)
														.limit(21)
														.collect(Collectors.toList());

		for (int i = 0; i < 21 && i < specialEvents.size(); i++) {
			Tuple<Long, SBEvent> pair = specialEvents.get(i);
			SBEvent sbEvent = pair.getSecond();

			ItemStack stack = sbEvent.getStack();
			if (stack == null) continue;

			int x = guiLeft + 89 + 17 * (i % 3);
			int y = guiTop + 44 + 17 * (i / 3);

			if (eventFavourites.contains(sbEvent.id)) {
				GlStateManager.depthMask(false);
				GlStateManager.translate(0, 0, -2);
				Gui.drawRect(x, y, x + 16, y + 16, 0xcfffbf49);
				GlStateManager.translate(0, 0, 2);
				GlStateManager.depthMask(true);
			}

			Utils.drawItemStackWithText(stack, x, y, "" + (i + 1));

			if (mouseX >= x && mouseX <= x + 16) {
				if (mouseY >= y && mouseY <= y + 16) {
					tooltipToDisplay = new ArrayList<>(sbEvent.desc);
					tooltipToDisplay.add(Utils.prettyTime(Duration.between(
						Instant.now(),
						Instant.ofEpochMilli(pair.getFirst())
					)));
				}
			}
		}

		if (nextEvent == null) {
			nextEvent = firstEvent;
			timeUntilNext = timeUntilFirst;
		}

		if (nextEvent != null) {
			String nextS = EnumChatFormatting.YELLOW + "Next: ";
			int nextSLen = fr.getStringWidth(nextS);
			fr.drawString(nextS, guiLeft + 8, guiTop + 6, -1, false);

			String until = " " + EnumChatFormatting.YELLOW + prettyTime(timeUntilNext, false);
			int untilLen = fr.getStringWidth(until);

			fr.drawString(until, guiLeft + xSize - 8 - untilLen, guiTop + 6, -1, false);

			int eventTitleLen = xSize - 16 - untilLen - nextSLen;
			int displayWidth = fr.getStringWidth(nextEvent.display);
			int spaceLen = fr.getCharWidth(' ');
			if (displayWidth > eventTitleLen) {
				GL11.glEnable(GL11.GL_SCISSOR_TEST);
				GL11.glScissor(
					(guiLeft + 8 + nextSLen) * scaledResolution.getScaleFactor(),
					0,
					eventTitleLen * scaledResolution.getScaleFactor(),
					Minecraft.getMinecraft().displayHeight
				);
				fr.drawString(nextEvent.display + " " + nextEvent.display,
					guiLeft + 8 + nextSLen - (float) (currentTime / 50.0 % (displayWidth + spaceLen)), guiTop + 6, -1, false
				);
				GL11.glDisable(GL11.GL_SCISSOR_TEST);
			} else {
				fr.drawString(nextEvent.display, guiLeft + 8 + nextSLen, guiTop + 6, -1, false);
			}

			if (mouseX > guiLeft && mouseX < guiLeft + 168) {
				if (mouseY > guiTop && mouseY < guiTop + 20) {
					tooltipToDisplay = Utils.createList(
						nextEvent.display,
						EnumChatFormatting.GRAY + "Starts in: " + EnumChatFormatting.YELLOW + prettyTime(timeUntilNext, false)
					);
					if (nextEvent.lastsFor >= 0) {
						tooltipToDisplay.add(EnumChatFormatting.GRAY + "Lasts for: " + EnumChatFormatting.YELLOW +
							prettyTime(nextEvent.lastsFor, true));
						if (timeUntilNext < 0) {
							tooltipToDisplay.add(EnumChatFormatting.GRAY + "Time left: " + EnumChatFormatting.YELLOW +
								prettyTime(nextEvent.lastsFor + timeUntilNext, true));
						}

					}
					if (nextEvent.desc != null) {
						tooltipToDisplay.add("");
						tooltipToDisplay.addAll(nextEvent.desc);
					}
				}
			}
		}

		GlStateManager.color(1, 1, 1, 1);
		Minecraft.getMinecraft().getTextureManager().bindTexture(help);
		Utils.drawTexturedRect(guiLeft + xSize - 18, guiTop + ySize + 2, 16, 16, GL11.GL_LINEAR);

		if (mouseX >= guiLeft + xSize - 18 && mouseX < guiLeft + xSize - 2) {
			if (mouseY >= guiTop + ySize + 2 && mouseY <= guiTop + ySize + 18) {
				tooltipToDisplay = new ArrayList<>();
				tooltipToDisplay.add(EnumChatFormatting.AQUA + "NEU Calendar Help");
				tooltipToDisplay.add(EnumChatFormatting.YELLOW + "This calendar displays various SkyBlock events");
				tooltipToDisplay.add(EnumChatFormatting.YELLOW + "'Daily' events are events that happen frequently");
				tooltipToDisplay.add(EnumChatFormatting.YELLOW + "'Special' events are events that happen infrequently");
				tooltipToDisplay.add(EnumChatFormatting.YELLOW + "");
				tooltipToDisplay.add(EnumChatFormatting.YELLOW + "The eventbar at the top will also show in your inventory");
				tooltipToDisplay.add(EnumChatFormatting.YELLOW + "");
				tooltipToDisplay.add(EnumChatFormatting.YELLOW + "Press 'F' on an event to mark it as a favourite");
				tooltipToDisplay.add(EnumChatFormatting.YELLOW + "Favourited events will show over normal events");
				tooltipToDisplay.add(EnumChatFormatting.YELLOW + "Favourited events will also give a notification when it");
				tooltipToDisplay.add(EnumChatFormatting.YELLOW + "is about to start and when it does start");
				tooltipToDisplay.add(EnumChatFormatting.YELLOW + "");
				tooltipToDisplay.add(EnumChatFormatting.DARK_GRAY + "In order to show crop types for Jacob's Farming");
				tooltipToDisplay.add(EnumChatFormatting.DARK_GRAY + "contest, visit the full SkyBlock calendar and go all");
				tooltipToDisplay.add(EnumChatFormatting.DARK_GRAY + "the way to the end of the SkyBlock year");
				Utils.drawHoveringText(tooltipToDisplay, mouseX, mouseY, width, height, -1);
				tooltipToDisplay = null;
			}
		}

		if (jfFavouriteSelect != null) {
			int arrowLen = fr.getStringWidth("> ");
			int selectSizeX = 0;
			int selectStringIndex = 0;
			for (String s : jfFavouriteSelect) {
				int sWidth = fr.getStringWidth(s);
				if (selectStringIndex + 1 == jfFavouriteSelectIndex) sWidth += arrowLen;
				if (sWidth > selectSizeX) {
					selectSizeX = sWidth;
				}
				selectStringIndex++;
			}
			selectSizeX += +10;

			GlStateManager.translate(0, 0, 19);

			Gui.drawRect(jfFavouriteSelectX + 2, jfFavouriteSelectY + 2, jfFavouriteSelectX + selectSizeX + 2,
				jfFavouriteSelectY + 18 + jfFavouriteSelect.size() * 10 + 2, 0xa0000000
			);

			GlStateManager.depthFunc(GL11.GL_LESS);
			GlStateManager.translate(0, 0, 1);
			Gui.drawRect(jfFavouriteSelectX + 1, jfFavouriteSelectY + 1, jfFavouriteSelectX + selectSizeX - 1,
				jfFavouriteSelectY + 18 + jfFavouriteSelect.size() * 10 - 1, 0xffc0c0c0
			);
			Gui.drawRect(jfFavouriteSelectX, jfFavouriteSelectY, jfFavouriteSelectX + selectSizeX - 1,
				jfFavouriteSelectY + 18 + jfFavouriteSelect.size() * 10 - 1, 0xfff0f0f0
			);
			Gui.drawRect(jfFavouriteSelectX, jfFavouriteSelectY, jfFavouriteSelectX + selectSizeX,
				jfFavouriteSelectY + 18 + jfFavouriteSelect.size() * 10, 0xff909090
			);
			GlStateManager.depthFunc(GL11.GL_LEQUAL);

			String all = (NotEnoughUpdates.INSTANCE.config.hidden.eventFavourites.contains("jacob_farming") ?
				EnumChatFormatting.DARK_GREEN : EnumChatFormatting.DARK_GRAY) + "All";
			if (jfFavouriteSelectIndex == 0) {
				fr.drawString(
					EnumChatFormatting.BLACK + "> " + all,
					jfFavouriteSelectX + 5,
					jfFavouriteSelectY + 5,
					0xff000000
				);
			} else {
				fr.drawString(all, jfFavouriteSelectX + 5, jfFavouriteSelectY + 5, 0xff000000);
			}

			fr.drawString(EnumChatFormatting.BLACK + "> ", jfFavouriteSelectX + 6,
				jfFavouriteSelectY + 10 * jfFavouriteSelectIndex + 5, 0xff000000
			);

			selectStringIndex = 0;
			for (String s : jfFavouriteSelect) {
				EnumChatFormatting colour = NotEnoughUpdates.INSTANCE.config.hidden.eventFavourites.contains(
					"jacob_farming:" + s)
					? EnumChatFormatting.DARK_GREEN : EnumChatFormatting.DARK_GRAY;
				s = (selectStringIndex + 1 == jfFavouriteSelectIndex ? EnumChatFormatting.BLACK + "> " : "") + colour + s;
				fr.drawString(s, jfFavouriteSelectX + 5, jfFavouriteSelectY + 10 * selectStringIndex + 15, 0xff000000);
				selectStringIndex++;
			}
			GlStateManager.translate(0, 0, -20);
		} else if (tooltipToDisplay != null) {
			Utils.drawHoveringText(tooltipToDisplay, mouseX, mouseY, width, height, -1);
		}

		GlStateManager.translate(0, 0, -10);

	}

	private static String getIdForDisplayName(String displayName) {
		return Utils.cleanColour(displayName)
								.toLowerCase()
								.replaceAll("[0-9]+th", "")
								.replaceAll("[0-9]+nd", "")
								.replaceAll("[0-9]+rd", "")
								.replaceAll("[0-9]+st", "")
								.replaceAll("[^a-z ]", "")
								.trim()
								.replace(" ", "_");
	}

	@SubscribeEvent
	public void onGuiScreenMouse(GuiScreenEvent.MouseInputEvent.Pre event) {
		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		int width = scaledResolution.getScaledWidth();
		int height = scaledResolution.getScaledHeight();
		int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
		int mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;

		if (!enabled) {
			if (Mouse.getEventButtonState() && NotEnoughUpdates.INSTANCE.config.calendar.showEventTimerInInventory &&
				Minecraft.getMinecraft().currentScreen instanceof GuiContainer) {
				xSize = 168;
				ySize = 20;

				guiLeft = (width - xSize) / 2;
				guiTop = 5;
				if (mouseX >= guiLeft && mouseX <= guiLeft + xSize && ableToClickCalendar) {
					if (mouseY >= guiTop && mouseY <= guiTop + ySize) {
						ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, "/neucalendar");
					}
				}
			}

			return;
		}

		if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) {
			return;
		}

		GuiChest eventGui = (GuiChest) Minecraft.getMinecraft().currentScreen;
		ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
		String containerName = cc.getLowerChestInventory().getDisplayName().getUnformattedText();
		if (!containerName.trim().equals("Calendar and Events")) {
			setEnabled(false);
			return;
		}

		event.setCanceled(true);

		xSize = 168;
		ySize = 170;
		guiLeft = (width - xSize) / 2;
		guiTop = (height - ySize) / 2;

		if (Mouse.getEventButtonState()) {
			if (jfFavouriteSelect != null) {
				FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
				int arrowLen = fr.getStringWidth("> ");
				int selectSizeX = 0;
				int selectStringIndex = 0;
				for (String s : jfFavouriteSelect) {
					int sWidth = fr.getStringWidth(s);
					if (selectStringIndex + 1 == jfFavouriteSelectIndex) sWidth += arrowLen;
					if (sWidth > selectSizeX) {
						selectSizeX = sWidth;
					}
					selectStringIndex++;
				}
				selectSizeX += +10;

				if (mouseX > jfFavouriteSelectX && mouseX < jfFavouriteSelectX + selectSizeX &&
					mouseY > jfFavouriteSelectY && mouseY < jfFavouriteSelectY + 18 + jfFavouriteSelect.size() * 10) {
					jfFavouriteSelectIndex = Math.max(0, (mouseY - jfFavouriteSelectY - 5) / 10);

					List<String> eventFavourites = NotEnoughUpdates.INSTANCE.config.hidden.eventFavourites;
					String id = null;
					if (jfFavouriteSelectIndex == 0) {
						id = "jacob_farming";
					} else if (jfFavouriteSelectIndex - 1 < jfFavouriteSelect.size()) {
						id = "jacob_farming:" + jfFavouriteSelect.get(jfFavouriteSelectIndex - 1);
					}
					if (id != null) {
						if (eventFavourites.contains(id)) {
							eventFavourites.remove(id);
						} else {
							eventFavourites.add(id);
						}
					}
				} else {
					jfFavouriteSelect = null;
				}
			}
			if (mouseY >= guiTop + 26 && mouseY <= guiTop + 26 + 141) {
				if (mouseX >= guiLeft + 151 && mouseX <= guiLeft + 151 + 14) {
					if (mouseY <= guiTop + 26 + 70) {
						Minecraft.getMinecraft().playerController.windowClick(cc.windowId,
							41, 2, 3, Minecraft.getMinecraft().thePlayer
						);
					} else {
						Minecraft.getMinecraft().playerController.windowClick(cc.windowId,
							36, 2, 3, Minecraft.getMinecraft().thePlayer
						);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onGuiScreenKeyboard(GuiScreenEvent.KeyboardInputEvent.Pre event) {
		if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
			if (jfFavouriteSelect != null) {
				jfFavouriteSelect = null;
				event.setCanceled(true);
			}
		} else {
			if (!enabled) {
				return;
			}

			if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) {
				return;
			}

			GuiChest eventGui = (GuiChest) Minecraft.getMinecraft().currentScreen;
			ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
			String containerName = cc.getLowerChestInventory().getDisplayName().getUnformattedText();
			if (!containerName.trim().equals("Calendar and Events")) {
				setEnabled(false);
				return;
			}

			event.setCanceled(true);
			xSize = 168;
			ySize = 170;

			ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
			int width = scaledResolution.getScaledWidth();
			int height = scaledResolution.getScaledHeight();
			int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
			int mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;
			guiLeft = (width - xSize) / 2;
			guiTop = (height - ySize) / 2;

			int keyPressed = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
			if (Keyboard.getEventKeyState()) {
				if (jfFavouriteSelect != null) {
					if (keyPressed == Keyboard.KEY_DOWN) {
						jfFavouriteSelectIndex++;
						jfFavouriteSelectIndex %= jfFavouriteSelect.size() + 1;
					} else if (keyPressed == Keyboard.KEY_UP) {
						jfFavouriteSelectIndex--;
						if (jfFavouriteSelectIndex < 0) jfFavouriteSelectIndex = jfFavouriteSelect.size();
					} else if (keyPressed == Keyboard.KEY_RIGHT || keyPressed == Keyboard.KEY_RETURN) {
						List<String> eventFavourites = NotEnoughUpdates.INSTANCE.config.hidden.eventFavourites;
						String id = null;
						if (jfFavouriteSelectIndex == 0) {
							id = "jacob_farming";
						} else if (jfFavouriteSelectIndex - 1 < jfFavouriteSelect.size()) {
							id = "jacob_farming:" + jfFavouriteSelect.get(jfFavouriteSelectIndex - 1);
						}
						if (id != null) {
							if (eventFavourites.contains(id)) {
								eventFavourites.remove(id);
							} else {
								eventFavourites.add(id);
							}
						}
					} else if (keyPressed == Keyboard.KEY_LEFT ||
						keyPressed == NotEnoughUpdates.INSTANCE.manager.keybindFavourite.getKeyCode()) {
						jfFavouriteSelect = null;
					}
				} else if (keyPressed == NotEnoughUpdates.INSTANCE.manager.keybindFavourite.getKeyCode()) {
					String id = null;

					//Daily Events
					int index = 0;
					out:
					for (Map.Entry<Long, Set<SBEvent>> sbEvents : eventMap.entrySet()) {
						for (SBEvent sbEvent : sbEvents.getValue()) {
							int x = guiLeft + 29 + 17 * (index % 3);
							int y = guiTop + 44 + 17 * (index / 3);

							if (mouseX >= x && mouseX <= x + 16) {
								if (mouseY >= y && mouseY <= y + 16) {
									id = sbEvent.id;
								}
							}

							if (++index >= 21) break out;
						}
					}

					//Special Events
					List<Tuple<Long, SBEvent>> specialEventsLocal = specialEvents;
					for (int i = 0; specialEventsLocal != null && i < 21 && i < specialEventsLocal.size(); i++) {
						int x = guiLeft + 89 + 17 * (i % 3);
						int y = guiTop + 44 + 17 * (i / 3);

						if (mouseX >= x && mouseX <= x + 16) {
							if (mouseY >= y && mouseY <= y + 16) {
								id = specialEventsLocal.get(i).getSecond().id;
							}
						}
					}

					if (id != null) {
						String[] split = id.split(":");
						if (split.length > 1 && split[0].equals("jacob_farming")) {
							jfFavouriteSelect = new ArrayList<>();
							for (int i = 1; i < split.length; i++) {
								jfFavouriteSelect.add(split[i]);
							}
							jfFavouriteSelectIndex = 0;
							jfFavouriteSelectX = mouseX;
							jfFavouriteSelectY = mouseY;
						} else {
							List<String> eventFavourites = NotEnoughUpdates.INSTANCE.config.hidden.eventFavourites;
							if (eventFavourites.contains(id)) {
								eventFavourites.remove(id);
							} else {
								eventFavourites.add(id);
							}
						}
					}
				} else {
					Minecraft.getMinecraft().dispatchKeypresses();
				}
			}
		}
	}

	public Optional<Pair<SBEvent, Long>> getNextFavouriteEvent(boolean orGetFirst) {
		populateDefaultEvents();
		long currentTime = System.currentTimeMillis();
		List<String> eventFavourites = NotEnoughUpdates.INSTANCE.config.hidden.eventFavourites;
		SBEvent nextAnyEvent = null;
		long timeUntilNextAny = 0L;
		//Daily Events
		for (Map.Entry<Long, Set<SBEvent>> sbEvents : eventMap.entrySet()) {
			for (SBEvent sbEvent : sbEvents.getValue()) {
				long timeUntilMillis = sbEvents.getKey() - currentTime;

				if (timeUntilMillis < -10 * SECOND) {
					continue;
				}
				if (nextAnyEvent == null) {
					timeUntilNextAny = timeUntilMillis;
					nextAnyEvent = sbEvent;
				}

				String[] split = sbEvent.id.split(":");
				boolean containsId = false;
				for (int i = 1; i < split.length; i++) {
					if (eventFavourites.contains(split[0] + ":" + split[i])) {
						containsId = true;
						break;
					}
				}
				if (eventFavourites.isEmpty() || eventFavourites.contains(split[0]) || containsId) {
					return Optional.of(new Pair<>(sbEvent, timeUntilMillis));
				}
			}
		}
		if (orGetFirst && nextAnyEvent != null)
			return Optional.of(new Pair<>(nextAnyEvent, timeUntilNextAny));
		return Optional.empty();
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onGuiDraw(RenderGameOverlayEvent.Post event) {
		if (NotEnoughUpdates.INSTANCE.config.calendar.eventNotifications &&
			event.type == RenderGameOverlayEvent.ElementType.ALL) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, 10);
			if (!(Minecraft.getMinecraft().currentScreen instanceof GuiContainer) &&
				NotEnoughUpdates.INSTANCE.isOnSkyblock()) {
				var nextFavouriteEvent = getNextFavouriteEvent(false);
				nextFavouriteEvent.ifPresent((nextEvent) -> {
					renderToast(nextEvent.component1(), nextEvent.component2());
				});
			}
			GlStateManager.translate(0, 0, -10);
			GlStateManager.popMatrix();
		}
	}

	public boolean renderToast(SBEvent event, long timeUntil) {
		if (!NotEnoughUpdates.INSTANCE.config.calendar.eventNotifications) {
			return false;
		}

		long currentTime = System.currentTimeMillis();
		if (currentTime - spookyStart > 0 && currentTime - spookyStart < HOUR &&
			NotEnoughUpdates.INSTANCE.config.calendar.spookyNightNotification) {
			long delta = (currentTime - SKYBLOCK_START) % (20 * MINUTE) - 19 * 50 * SECOND - 10 * SECOND;
			if (delta < 500 && delta > -8500) {
				event = new SBEvent("spooky_festival_7pm", "Spooky Festival 7pm", true, new ItemStack(Items.bone), null);
				timeUntil = delta;
			}
		}

		if (event.id.equals("dark_auction")) {
			timeUntil -= 30 * 1000;
		}

		FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		int width = scaledResolution.getScaledWidth();
		int height = scaledResolution.getScaledHeight();

		int ySize = 32;
		int xSize = 160;
		int guiLeft = (width - xSize) / 2;
		int guiTop = 5;

		boolean preNotification = false;
		long preNotificationTime = SECOND * NotEnoughUpdates.INSTANCE.config.calendar.startingSoonTime;

		if (preNotificationTime > 500 && timeUntil > 500) {
			timeUntil = timeUntil - preNotificationTime;
			preNotification = true;
		}

		if (timeUntil < 500 && timeUntil > -8500) {
			if (jingleIndex == -1) {
				if (preNotification) {
					jingleIndex = 1;
				} else {
					jingleIndex = 0;
				}
			}

			float offset;
			float factor = 0;
			if (timeUntil > 0) {
				factor = (timeUntil / 500f);
			} else if (timeUntil < -8000) {
				factor = -((timeUntil + 8000) / 500f);
			}
			factor = (float) (1.06f / (1 + Math.exp(-7 * (factor - 0.5f))) - 0.03f);
			offset = -(ySize + 5) * factor;
			float y = guiTop + offset;

			GlStateManager.color(1, 1, 1, 1);
			Minecraft.getMinecraft().getTextureManager().bindTexture(TOAST);
			Utils.drawTexturedRect(guiLeft, y, xSize, ySize, GL11.GL_NEAREST);

			GlStateManager.translate(0, y, 0);
			Utils.drawItemStack(event.stack, guiLeft + 6, 8);
			GlStateManager.translate(0, -y, 0);

			if (preNotification) {
				String starting = EnumChatFormatting.YELLOW + "Event Starting in " + prettyTime(preNotificationTime, true) +
					"!";
				int startingWidth = fr.getStringWidth(starting);
				fr.drawString(starting, Math.max(guiLeft + 23, width / 2f - startingWidth / 2f), y + 7, -1, false);
			} else {
				Utils.drawStringCentered(EnumChatFormatting.YELLOW + "Event Starting Now!", width / 2, y + 11, false, -1);
			}

			int displayWidth = fr.getStringWidth(event.display);
			fr.drawString(event.display, Math.max(guiLeft + 23, width / 2f - displayWidth / 2f), y + 17, -1, false);

			return true;
		}
		return false;
	}

	@SubscribeEvent
	public void onGuiScreenDrawTimer(GuiScreenEvent.BackgroundDrawnEvent event) {
		if (!drawTimerForeground) {
			drawTimer();
		}
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.enableBlend();
	}

	@SubscribeEvent
	public void onGuiScreenDrawTimer(GuiScreenEvent.DrawScreenEvent.Post event) {
		if (drawTimerForeground) {
			drawTimer();
		}
	}

	public void drawTimer() {
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, 10);
		if (Minecraft.getMinecraft().currentScreen instanceof GuiContainer && NotEnoughUpdates.INSTANCE.isOnSkyblock()) {
			ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
			int width = scaledResolution.getScaledWidth();
			int height = scaledResolution.getScaledHeight();
			int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
			int mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;
			long currentTime = System.currentTimeMillis();

			xSize = 168;
			ySize = 20;

			long timeUntilNext = 0;
			SBEvent nextEvent = null;
			long timeUntilFirst = 0;
			SBEvent firstEvent = null;
			List<SBEvent> nextFavourites = new ArrayList<>();
			List<Long> nextFavouritesTime = new ArrayList<>();
			long timeUntilMayor = 0;
			SBEvent nextMayorEvent = null;

			List<String> eventFavourites = NotEnoughUpdates.INSTANCE.config.hidden.eventFavourites;

			guiLeft = (width - xSize) / 2;
			guiTop = 5;

			populateDefaultEvents();
			//Daily Events
			out:
			for (Map.Entry<Long, Set<SBEvent>> sbEvents : eventMap.entrySet()) {
				for (SBEvent sbEvent : sbEvents.getValue()) {
					long timeUntilMillis = sbEvents.getKey() - currentTime;

					if (timeUntilMillis < -10 * SECOND) {
						continue;
					}

					if (sbEvent.id.equals("spooky_festival")) {
						if (sbEvents.getKey() > currentTime - HOUR && (sbEvents.getKey() < spookyStart || spookyStart == 0)) {
							spookyStart = sbEvents.getKey();
						}
					}

					if (nextMayorEvent == null && !sbEvent.id.split(":")[0].equals("jacob_farming") &&
						!sbEvent.id.equals("dark_auction") && !sbEvent.isArtificial) {
						nextMayorEvent = sbEvent;
						timeUntilMayor = timeUntilMillis;
					}

					if (firstEvent == null) {
						firstEvent = sbEvent;
						timeUntilFirst = timeUntilMillis;
					}

					String[] split = sbEvent.id.split(":");
					boolean containsId = false;
					for (int i = 1; i < split.length; i++) {
						if (eventFavourites.contains(split[0] + ":" + split[i])) {
							containsId = true;
							break;
						}
					}
					if (eventFavourites.isEmpty() || eventFavourites.contains(split[0]) || containsId) {
						if (nextEvent == null) {
							nextEvent = sbEvent;
							timeUntilNext = timeUntilMillis;
						}
						if (nextFavourites.size() < 3) {
							nextFavourites.add(sbEvent);
							nextFavouritesTime.add(timeUntilMillis);
						}
					}

					if (nextFavourites.size() >= 3 && nextMayorEvent != null) {
						break out;
					}
				}
			}


			if (nextEvent != null) {
				GlStateManager.translate(0, 0, 50);
				boolean toastRendered = renderToast(nextEvent, timeUntilNext);
				GlStateManager.translate(0, 0, -50);
				if (!toastRendered && !enabled && NotEnoughUpdates.INSTANCE.config.calendar.showEventTimerInInventory) {
					List<String> tooltipToDisplay = null;
					FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

					GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
					GlStateManager.disableFog();
					GlStateManager.disableLighting();
					GlStateManager.disableColorMaterial();

					renderBlurredBackground(10, width, height, guiLeft + 3, guiTop + 3, xSize - 6, ySize - 6);

					Minecraft.getMinecraft().getTextureManager().bindTexture(DISPLAYBAR);
					Utils.drawTexturedRect(guiLeft, guiTop, xSize, 20, GL11.GL_NEAREST);

					String nextS = EnumChatFormatting.YELLOW + "Next: ";
					int nextSLen = fr.getStringWidth(nextS);
					fr.drawString(nextS, guiLeft + 8, guiTop + 6, -1, false);

					String until = " " + EnumChatFormatting.YELLOW + prettyTime(timeUntilNext, false);
					int untilLen = fr.getStringWidth(until);

					fr.drawString(until, guiLeft + xSize - 8 - untilLen, guiTop + 6, -1, false);

					int eventTitleLen = xSize - 16 - untilLen - nextSLen;
					int displayWidth = fr.getStringWidth(nextEvent.display);
					int spaceLen = fr.getCharWidth(' ');
					if (displayWidth > eventTitleLen) {
						GL11.glEnable(GL11.GL_SCISSOR_TEST);
						GL11.glScissor(
							(guiLeft + 8 + nextSLen) * scaledResolution.getScaleFactor(),
							0,
							eventTitleLen * scaledResolution.getScaleFactor(),
							Minecraft.getMinecraft().displayHeight
						);
						fr.drawString(nextEvent.display + " " + nextEvent.display,
							guiLeft + 8 + nextSLen - (float) (currentTime / 50.0 % (displayWidth + spaceLen)), guiTop + 6, -1, false
						);
						GL11.glDisable(GL11.GL_SCISSOR_TEST);
					} else {
						if (guiLeft + xSize - 8 - untilLen > (width + displayWidth) / 2) {
							Utils.drawStringCentered(nextEvent.display, width / 2f, guiTop + 10, false, -1);
						} else {
							fr.drawString(nextEvent.display, guiLeft + 8 + nextSLen, guiTop + 6, -1, false);
						}
					}

					if (mouseX > guiLeft && mouseX < guiLeft + 168) {
						if (mouseY > guiTop && mouseY < guiTop + 20) {
							tooltipToDisplay = new ArrayList<>();
							for (int i = 0; i < nextFavourites.size(); i++) {
								SBEvent sbEvent = nextFavourites.get(i);
								long timeUntil = nextFavouritesTime.get(i);

								tooltipToDisplay.add(sbEvent.display);
								tooltipToDisplay.add(
									EnumChatFormatting.GRAY + "Starts in: " + EnumChatFormatting.YELLOW + prettyTime(timeUntil, false));
								if (sbEvent.lastsFor >= 0) {
									tooltipToDisplay.add(EnumChatFormatting.GRAY + "Lasts for: " + EnumChatFormatting.YELLOW +
										prettyTime(sbEvent.lastsFor, true));
									if (timeUntil < 0) {
										tooltipToDisplay.add(EnumChatFormatting.GRAY + "Time left: " + EnumChatFormatting.YELLOW +
											prettyTime(sbEvent.lastsFor + timeUntil, true));
									}
								}
								if (sbEvent.id.split(":")[0].equals("jacob_farming") && sbEvent.desc != null) {
									tooltipToDisplay.addAll(sbEvent.desc);
								}
								if (nextMayorEvent != null || i < nextFavourites.size() - 1) {
									tooltipToDisplay.add("");
								}
							}
							if (nextMayorEvent != null) {
								tooltipToDisplay.add(EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "Next Mayor:");
								tooltipToDisplay.add(nextMayorEvent.display);
								tooltipToDisplay.add(EnumChatFormatting.GRAY + "Starts in: " + EnumChatFormatting.YELLOW +
									prettyTime(timeUntilMayor, false));
								if (nextMayorEvent.lastsFor >= 0) {
									tooltipToDisplay.add(EnumChatFormatting.GRAY + "Lasts for: " + EnumChatFormatting.YELLOW +
										prettyTime(nextMayorEvent.lastsFor, true));
								}
							}

						}
					}

					drawTimerForeground = false;
					if (tooltipToDisplay != null) {
						drawTimerForeground = true;
						GlStateManager.translate(0, 0, 100);
						Utils.drawHoveringText(tooltipToDisplay, mouseX, Math.max(17, mouseY), width, height, -1);
						GlStateManager.translate(0, 0, -100);
					}
				}
			} else if (!enabled && NotEnoughUpdates.INSTANCE.config.calendar.showEventTimerInInventory) {
				FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				GlStateManager.disableFog();
				GlStateManager.disableLighting();
				GlStateManager.disableColorMaterial();

				renderBlurredBackground(10, width, height, guiLeft + 3, guiTop + 3, xSize - 6, ySize - 6);

				Minecraft.getMinecraft().getTextureManager().bindTexture(DISPLAYBAR);
				Utils.drawTexturedRect(guiLeft, guiTop, xSize, 20, GL11.GL_NEAREST);

				String nextS = EnumChatFormatting.RED + "Open calendar to see events";
				fr.drawString(nextS, guiLeft + 8, guiTop + 6, -1, false);
			}
		}
		GlStateManager.translate(0, 0, -10);
		GlStateManager.popMatrix();
	}

	private void renderBlurredBackground(
		float blurStrength,
		int screenWidth,
		int screenHeight,
		int x,
		int y,
		int blurWidth,
		int blurHeight
	) {
		BackgroundBlur.renderBlurredBackground(blurStrength, screenWidth, screenHeight, x, y, blurWidth, blurHeight);
		Gui.drawRect(x, y, x + blurWidth, y + blurHeight, 0xc8101010);
		GlStateManager.color(1, 1, 1, 1);
	}

	private static class SBEvent {
		String id;
		String display;
		ItemStack stack;
		List<String> desc;
		long lastsFor;
		boolean isSpecial;
		boolean isArtificial;

		public SBEvent(String id, String display, boolean isSpecial, ItemStack stack, List<String> desc) {
			this(id, display, isSpecial, stack, desc, -1);
		}

		public SBEvent(String id, String display, boolean isSpecial, ItemStack stack, List<String> desc, long lastsFor) {
			this(id, display, isSpecial, stack, desc, lastsFor, false);
		}

		public SBEvent(
			String id,
			String display,
			boolean isSpecial,
			ItemStack stack,
			List<String> desc,
			long lastsFor,
			boolean isArtificial
		) {
			this.id = id;
			this.isSpecial = isSpecial;
			this.isArtificial = isArtificial;
			this.display = display;
			this.stack = stack;
			this.desc = desc;
			this.lastsFor = lastsFor;
		}

		public ItemStack getStack() {
			if (stack != null) {
				NBTTagCompound tag = ItemUtils.getOrCreateTag(stack);
				tag.setString("event_id", id);
			}
			return stack;
		}
	}

	private String prettyTime(long millis, boolean trimmed) {
		long seconds = millis / 1000 % 60;
		long minutes = (millis / 1000 / 60) % 60;
		long hours = (millis / 1000 / 60 / 60) % 24;
		long days = (millis / 1000 / 60 / 60 / 24);

		String endsIn = "";
		if (millis < 0) {
			endsIn += "Now!";
		} else if (minutes == 0 && hours == 0 && days == 0) {
			endsIn += seconds + "s";
		} else if (hours == 0 && days == 0) {
			if (trimmed && seconds == 0) {
				endsIn += minutes + "m";
			} else {
				endsIn += minutes + "m" + seconds + "s";
			}
		} else if (days == 0) {
			if (hours <= 6) {
				if (trimmed && seconds == 0) {
					if (minutes == 0) {
						endsIn += hours + "h";
					} else {
						endsIn += hours + "h" + minutes + "m";
					}
				} else {
					endsIn += hours + "h" + minutes + "m" + seconds + "s";
				}
			} else {
				endsIn += hours + "h";
			}
		} else {
			endsIn += days + "d" + hours + "h";
		}

		return endsIn;
	}
}
