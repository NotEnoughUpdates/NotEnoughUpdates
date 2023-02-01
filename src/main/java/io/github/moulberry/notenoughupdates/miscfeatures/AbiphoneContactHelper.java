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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.events.SlotClickEvent;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.Map;

@NEUAutoSubscribe
public class AbiphoneContactHelper {

	private static final AbiphoneContactHelper INSTANCE = new AbiphoneContactHelper();

	public static AbiphoneContactHelper getInstance() {
		return INSTANCE;
	}

	private String selectedWaypointName = "";
	private long lastClick = 0L;

	@SubscribeEvent
	public void onItemTooltip(ItemTooltipEvent event) {
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) return;
		if (!Utils.getOpenChestName().equals("Contacts Directory")) return;

		List<String> list = event.toolTip;
		if (list == null) return;
		if (list.isEmpty()) return;
		String rawNpcName = event.itemStack.getDisplayName();
		String npcName = StringUtils.cleanColour(rawNpcName);

		JsonObject data = getJsonData(npcName);
		if (data == null) return;

		if (NotEnoughUpdates.INSTANCE.config.tooltipTweaks.abiphoneContactRequirements) {
			if (data.has("requirement")) {
				JsonArray requirements = data.get("requirement").getAsJsonArray();
				if (requirements.size() > 0) {
					list.add(" ");
					list.add("§e§lRequirements:");

					for (JsonElement requirementObject : requirements) {
						String requirement = requirementObject.getAsString();
						list.add(requirement);
					}
				}
			}
		}

		if (NotEnoughUpdates.INSTANCE.config.tooltipTweaks.abiphoneContactMarker) {
			if (data.has("x")) {
				if (selectedWaypointName.equals(npcName)) {
					list.set(0, npcName + " §f- §aMarker set!");
				}

				list.add(" ");
				if (selectedWaypointName.equals(npcName)) {
					list.add("§eClick to remove the marker!");
				} else {
					list.add("§eClick to set a marker!");
				}
				list.add("§eShift-Click to teleport to the nearest waypoint!");
			}
		}
	}

	@SubscribeEvent
	public void onStackClick(SlotClickEvent event) {
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) return;
		if (!NotEnoughUpdates.INSTANCE.config.tooltipTweaks.abiphoneContactMarker) return;
		if (!Utils.getOpenChestName().equals("Contacts Directory")) return;

		ItemStack stack = event.slot.getStack();
		if (stack == null || stack.getDisplayName() == null) return;

		String rawNpcName = stack.getDisplayName();
		String npcName = StringUtils.cleanColour(rawNpcName);

		JsonObject data = getJsonData(npcName);
		if (data == null) return;
		if (!data.has("x")) return;

		int x = data.get("x").getAsInt();
		int y = data.get("y").getAsInt();
		int z = data.get("z").getAsInt();

		String rawIslandName = data.get("island").getAsString();
		if (rawIslandName == null) return;

		if (lastClick + 500 > System.currentTimeMillis()) return;
		lastClick = System.currentTimeMillis();

		boolean shiftPressed = Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
		if (selectedWaypointName.equals(npcName) && !shiftPressed) {
			NotEnoughUpdates.INSTANCE.navigation.untrackWaypoint();
			selectedWaypointName = "";
		} else {
			trackWaypoint(rawNpcName, x, y, z, rawIslandName);
			if (shiftPressed) {
				NotEnoughUpdates.INSTANCE.navigation.useWarpCommand();
			}
			selectedWaypointName = npcName;
		}
		Utils.playPressSound();
	}

	private static void trackWaypoint(String rawNpcName, int x, int y, int z, String rawIslandName) {
		JsonObject waypoint = new JsonObject();
		waypoint.add("x", new JsonPrimitive(x));
		waypoint.add("y", new JsonPrimitive(y));
		waypoint.add("z", new JsonPrimitive(z));
		waypoint.add("displayname", new JsonPrimitive(rawNpcName));
		waypoint.add("island", new JsonPrimitive(rawIslandName));
		waypoint.add("internalname", new JsonPrimitive("abiphone-contact-" + rawNpcName));

		NotEnoughUpdates.INSTANCE.navigation.trackWaypoint(waypoint);
	}

	private JsonObject getJsonData(String npcName) {
		JsonObject abiphone = Constants.ABIPHONE;
		if (abiphone == null) return null;

		JsonObject contacts = abiphone.getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : contacts.entrySet()) {
			String repoName = entry.getKey();
			if (repoName.equals(npcName)) {
				return entry.getValue().getAsJsonObject();
			}
		}
		return null;
	}

	public void resetMarker() {
		selectedWaypointName = "";
	}
}
