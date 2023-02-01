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

package io.github.moulberry.notenoughupdates.miscfeatures;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.options.NEUConfig;
import io.github.moulberry.notenoughupdates.overlays.MiningOverlay;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.util.vector.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NEUAutoSubscribe
public class DwarvenMinesWaypoints {

	private final HashMap<String, Vector3f> waypointsMap = new HashMap<String, Vector3f>() {{
		put("Dwarven Village", new Vector3f(-37, 199, -122));
		put("Miner's Guild", new Vector3f(-74, 220, -122));
		put("Fetchur", new Vector3f(85, 223, -120));
		put("Palace Bridge", new Vector3f(129, 186, 8));
		put("Royal Palace", new Vector3f(129, 194, 194));
		put("Puzzler", new Vector3f(181, 195, 135));
		put("Grand Library", new Vector3f(183, 195, 181));
		put("Barracks of Heroes", new Vector3f(93, 195, 181));
		put("Royal Mines", new Vector3f(178, 149, 71));
		put("Cliffside Veins", new Vector3f(40, 136, 17));
		put("Forge Basin", new Vector3f(0, 169, -2));
		put("The Forge", new Vector3f(0, 148, -69));
		put("Rampart's Quarry", new Vector3f(-106, 147, 2));
		put("Far Reserve", new Vector3f(-160, 148, 17));
		put("Upper Mines", new Vector3f(-123, 170, -71));
		put("Goblin Burrows", new Vector3f(-138, 143, 141));
		put("Great Ice Wall", new Vector3f(0, 127, 160));
		put("Aristocrat Passage", new Vector3f(129, 150, 137));
		put("Hanging Court", new Vector3f(91, 186, 129));
		put("Divan's Gateway", new Vector3f(0, 127, 87));
		put("Lava Springs", new Vector3f(57, 196, -15));
		put("The Mist", new Vector3f(0, 75, 82));
	}};

	private static final HashSet<String> emissaryNames = new HashSet<String>() {{
		add(EnumChatFormatting.GOLD + "Emissary Ceanna" + EnumChatFormatting.RESET);
		add(EnumChatFormatting.GOLD + "Emissary Carlton" + EnumChatFormatting.RESET);
		add(EnumChatFormatting.GOLD + "Emissary Wilson" + EnumChatFormatting.RESET);
		add(EnumChatFormatting.GOLD + "Emissary Lilith" + EnumChatFormatting.RESET);
		add(EnumChatFormatting.GOLD + "Emissary Frasier" + EnumChatFormatting.RESET);
		add(EnumChatFormatting.GOLD + "Emissary Eliza" + EnumChatFormatting.RESET);
		add(EnumChatFormatting.GOLD.toString() + EnumChatFormatting.BOLD + "King Thormyr" + EnumChatFormatting.RESET);
	}};

	private enum Emissary {
		THORMYR("King Thormyr", 0, new Vector3f(129, 196, 196)),
		CEANNA("Emissary Ceanna", 1, new Vector3f(42, 134, 22)),
		CARLTON("Emissary Carlton", 1, new Vector3f(-73, 153, -11)),
		WILSON("Emissary Wilson", 2, new Vector3f(171, 150, 31)),
		LILITH("Emissary Lilith", 2, new Vector3f(58, 198, -8)),
		FRAISER("Emissary Frasier", 3, new Vector3f(-132, 174, -50)),
		ELIZA("Emissary Eliza", 3, new Vector3f(-37, 200, -131));

		String name;
		int minMilestone;
		Vector3f loc;

		Emissary(String name, int minMilestone, Vector3f loc) {
			this.name = name;
			this.minMilestone = minMilestone;
			this.loc = loc;
		}
	}

	private long dynamicMillis = 0;
	private String dynamicLocation = null;
	private String dynamicName = null;
	private final Pattern ghastRegex = Pattern.compile(
		"\u00A7r\u00A7eFind the \u00A7r\u00A76Powder Ghast\u00A7r\u00A7e near the \u00A7r\u00A7b(.+)!");
	private final Pattern fallenStarRegex = Pattern.compile(
		"\u00A7r\u00A75Fallen Star \u00A7r\u00A7ehas crashed at \u00A7r\u00A7b(.+)\u00A7r\u00A7e!");

	@SubscribeEvent
	public void onChat(ClientChatReceivedEvent event) {
		Matcher matcherGhast = ghastRegex.matcher(event.message.getFormattedText());
		if (matcherGhast.find() && NotEnoughUpdates.INSTANCE.config.mining.powderGhastWaypoint) {
			dynamicLocation = Utils.cleanColour(matcherGhast.group(1).trim());
			dynamicName = EnumChatFormatting.GOLD + "Powder Ghast";
			dynamicMillis = System.currentTimeMillis();
		} else {
			Matcher matcherStar = fallenStarRegex.matcher(event.message.getFormattedText());
			if (matcherStar.find() && NotEnoughUpdates.INSTANCE.config.mining.fallenStarWaypoint) {
				dynamicLocation = Utils.cleanColour(matcherStar.group(1).trim());
				dynamicName = EnumChatFormatting.DARK_PURPLE + "Fallen Star";
				dynamicMillis = System.currentTimeMillis();
			}
		}
	}

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		emissaryRemovedDistSq = -1;

		if (SBInfo.getInstance().getLocation() == null) return;
		if (!SBInfo.getInstance().getLocation().equals("mining_3")) return;

		NEUConfig.HiddenProfileSpecific hidden = NotEnoughUpdates.INSTANCE.config.getProfileSpecific();
		if (hidden == null) return;

		if (Minecraft.getMinecraft().currentScreen instanceof GuiChest) {
			GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
			ContainerChest container = (ContainerChest) chest.inventorySlots;
			IInventory lower = container.getLowerChestInventory();

			if (lower.getDisplayName().getFormattedText().contains("Commissions")) {
				for (int i = 0; i < lower.getSizeInventory(); i++) {
					ItemStack stack = lower.getStackInSlot(i);
					if (stack == null) continue;
					if (stack.getDisplayName().equals(EnumChatFormatting.YELLOW + "Commission Milestones")) {
						hidden.commissionMilestone = 5;
						String[] lore = NotEnoughUpdates.INSTANCE.manager.getLoreFromNBT(stack.getTagCompound());
						for (String line : lore) {
							String clean = Utils.cleanColour(line);
							switch (clean) {
								case "Tier I Rewards:":
									hidden.commissionMilestone = 0;
									break;
								case "Tier II Rewards:":
									hidden.commissionMilestone = 1;
									break;
								case "Tier III Rewards:":
									hidden.commissionMilestone = 2;
									break;
								case "Tier IV Rewards:":
									hidden.commissionMilestone = 3;
									break;
								case "Tier V Rewards:":
									hidden.commissionMilestone = 4;
									break;
							}
						}
						return;
					}
				}
			}
		}
	}

	private boolean commissionFinished = false;
	private double emissaryRemovedDistSq = 0;

	@SubscribeEvent
	public void onRenderSpecial(RenderLivingEvent.Specials.Pre<EntityArmorStand> event) {
		if (SBInfo.getInstance().getLocation() == null) return;
		if (!SBInfo.getInstance().getLocation().equals("mining_3")) return;

		if (commissionFinished && event.entity instanceof EntityArmorStand) {
			String name = event.entity.getDisplayName().getFormattedText();
			if (emissaryRemovedDistSq > 0 && name.equals(
				EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "CLICK" + EnumChatFormatting.RESET)) {
				EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
				double distSq = event.entity.getDistanceSq(p.posX, p.posY, p.posZ);
				if (Math.abs(distSq - emissaryRemovedDistSq) < 1) {
					event.setCanceled(true);
				}
			} else if (emissaryNames.contains(name)) {
				EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
				double distSq = event.entity.getDistanceSq(p.posX, p.posY, p.posZ);
				if (distSq >= 12 * 12) {
					emissaryRemovedDistSq = distSq;
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	public void onRenderLast(RenderWorldLastEvent event) {
		if (SBInfo.getInstance().getLocation() == null) return;
		if (!SBInfo.getInstance().getLocation().equals("mining_3")) return;

		int locWaypoint = NotEnoughUpdates.INSTANCE.config.mining.locWaypoints;
		if (dynamicLocation != null && dynamicName != null &&
			System.currentTimeMillis() - dynamicMillis < 30 * 1000) {
			for (Map.Entry<String, Vector3f> entry : waypointsMap.entrySet()) {
				if (entry.getKey().equals(dynamicLocation)) {
					RenderUtils.renderWayPoint(
						dynamicName,
						new Vector3f(entry.getValue()).translate(0, 15, 0),
						event.partialTicks
					);
					break;
				}
			}
		}
		String skyblockLocation = SBInfo.getInstance().location.toLowerCase();
		if (locWaypoint >= 1) {
			for (Map.Entry<String, Vector3f> entry : waypointsMap.entrySet()) {
				if (locWaypoint >= 2) {
					RenderUtils.renderWayPoint(EnumChatFormatting.AQUA + entry.getKey(), entry.getValue(), event.partialTicks);
				} else {
					String commissionLocation = entry.getKey().toLowerCase();
					for (String commissionName : MiningOverlay.commissionProgress.keySet()) {
						if (NotEnoughUpdates.INSTANCE.config.mining.hideWaypointIfAtLocation)
							if (commissionLocation.replace("'", "").equals(skyblockLocation)) continue;
						if (commissionName.toLowerCase().contains(commissionLocation)) {
							if (commissionName.contains("Titanium")) {
								RenderUtils.renderWayPoint(
									EnumChatFormatting.WHITE + entry.getKey(),
									entry.getValue(),
									event.partialTicks
								);
							} else {
								RenderUtils.renderWayPoint(
									EnumChatFormatting.AQUA + entry.getKey(),
									entry.getValue(),
									event.partialTicks
								);
							}
						}
					}
				}
			}
		}

		commissionFinished = NotEnoughUpdates.INSTANCE.config.mining.emissaryWaypoints >= 2;

		if (NotEnoughUpdates.INSTANCE.config.mining.emissaryWaypoints == 0) return;

		if (!commissionFinished) {
			for (float f : MiningOverlay.commissionProgress.values()) {
				if (f >= 1) {
					commissionFinished = true;
					break;
				}
			}
		}
		if (commissionFinished) {
			for (Emissary emissary : Emissary.values()) {

				NEUConfig.HiddenProfileSpecific hidden = NotEnoughUpdates.INSTANCE.config.getProfileSpecific();
				if (hidden != null) {
					if (hidden.commissionMilestone >= emissary.minMilestone) {

						EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
						double dX = emissary.loc.x + 0.5f - p.posX;
						double dY = emissary.loc.y + 0.188f - p.posY;
						double dZ = emissary.loc.z + 0.5f - p.posZ;

						double distSq = dX * dX + dY * dY + dZ * dZ;
						if (distSq >= 12 * 12) {
							RenderUtils.renderWayPoint(
								EnumChatFormatting.GOLD + emissary.name,
								new Vector3f(emissary.loc).translate(0.5f, 2.488f, 0.5f),
								event.partialTicks
							);
						}
					}
				}
			}
		}
	}
}
