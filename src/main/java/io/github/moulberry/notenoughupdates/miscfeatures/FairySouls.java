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

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import lombok.var;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@NEUAutoSubscribe
public class FairySouls {
	private static FairySouls instance = null;
	private static final String unknownProfile = "unknown";

	private boolean trackSouls;
	private boolean showSouls;
	private HashMap<String, HashMap<String, Set<Integer>>> allProfilesFoundSouls = new HashMap<>();
	private List<BlockPos> allSoulsInCurrentLocation;
	private Set<Integer> foundSoulsInLocation;
	private TreeMap<Double, BlockPos> missingSoulsDistanceSqMap;
	private List<BlockPos> closestMissingSouls;
	private String currentLocation;
	private BlockPos lastPlayerPos;

	public static FairySouls getInstance() {
		if (instance == null) {
			instance = new FairySouls();
		}
		return instance;
	}

	public boolean isTrackSouls() {
		return trackSouls;
	}

	public boolean isShowSouls() {
		return showSouls;
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		currentLocation = null;
		trackSouls = NotEnoughUpdates.INSTANCE.config.misc.trackFairySouls;
		showSouls = NotEnoughUpdates.INSTANCE.config.misc.fariySoul;
	}

	public void initializeLocation() {
		if (!trackSouls || currentLocation == null) {
			return;
		}

		foundSoulsInLocation = null;
		closestMissingSouls = new ArrayList<>();
		missingSoulsDistanceSqMap = new TreeMap<>();
		lastPlayerPos = BlockPos.ORIGIN;

		allSoulsInCurrentLocation = loadLocationFairySoulsFromConfig(currentLocation);
		if (allSoulsInCurrentLocation == null) {
			return;
		}

		foundSoulsInLocation = getFoundSoulsForProfile()
			.computeIfAbsent(currentLocation, k -> new HashSet<>());
		refreshMissingSoulInfo(true);
	}

	private void refreshMissingSoulInfo(boolean force) {
		if (allSoulsInCurrentLocation == null) return;

		BlockPos currentPlayerPos = Minecraft.getMinecraft().thePlayer.getPosition();
		if (lastPlayerPos.equals(currentPlayerPos) && !force) {
			return;
		}
		lastPlayerPos = currentPlayerPos;

		missingSoulsDistanceSqMap.clear();
		for (int i = 0; i < allSoulsInCurrentLocation.size(); i++) {
			if (foundSoulsInLocation.contains(i)) {
				continue;
			}
			BlockPos pos = allSoulsInCurrentLocation.get(i);
			double distSq = pos.distanceSq(lastPlayerPos);
			missingSoulsDistanceSqMap.put(distSq, pos);
		}
		closestMissingSouls.clear();
		if (missingSoulsDistanceSqMap.isEmpty()) {
			return;
		}

		// rebuild the list of the closest ones
		int maxSouls = 15;
		int souls = 0;
		for (BlockPos pos : missingSoulsDistanceSqMap.values()) {
			closestMissingSouls.add(pos);
			if (++souls >= maxSouls) break;
		}
	}

	private int interpolateColors(int color1, int color2, double factor) {
		int r1 = ((color1 >> 16) & 0xff);
		int g1 = ((color1 >> 8) & 0xff);
		int b1 = (color1 & 0xff);

		int r2 = (color2 >> 16) & 0xff;
		int g2 = (color2 >> 8) & 0xff;
		int b2 = color2 & 0xff;

		int r3 = r1 + (int) Math.round(factor * (r2 - r1));
		int g3 = g1 + (int) Math.round(factor * (g2 - g1));
		int b3 = b1 + (int) Math.round(factor * (b2 - b1));

		return (r3 & 0xff) << 16 |
			(g3 & 0xff) << 8 |
			(b3 & 0xff);
	}

	private double normalize(double value, double min, double max) {
		return ((value - min) / (max - min));
	}

	@SubscribeEvent
	public void onRenderLast(RenderWorldLastEvent event) {
		if (!showSouls || !trackSouls || currentLocation == null || closestMissingSouls.isEmpty()) {
			return;
		}

		int closeColor = 0x772991; // 0xa839ce
		int farColor = 0xCEB4D1;
		double farSoulDistSq = lastPlayerPos.distanceSq(closestMissingSouls.get(closestMissingSouls.size() - 1));
		for (BlockPos currentSoul : closestMissingSouls) {
			double currentDistSq = lastPlayerPos.distanceSq(currentSoul);
			double factor = normalize(currentDistSq, 0.0, farSoulDistSq);
			int rgb = interpolateColors(closeColor, farColor, Math.min(0.40, factor));
			RenderUtils.renderBeaconBeamOrBoundingBox(currentSoul, rgb, 1.0f, event.partialTicks);
			if (NotEnoughUpdates.INSTANCE.config.misc.fairySoulWaypointDistance) RenderUtils.renderWayPoint(currentSoul, event.partialTicks);
		}
	}

	public void setShowFairySouls(boolean enabled) {
		NotEnoughUpdates.INSTANCE.config.misc.fariySoul = enabled;
		showSouls = enabled;
	}

	public void setTrackFairySouls(boolean enabled) {
		NotEnoughUpdates.INSTANCE.config.misc.trackFairySouls = enabled;
		trackSouls = enabled;
	}

	public void markClosestSoulFound() {
		if (!trackSouls) return;
		int closestIndex = -1;
		double closestDistSq = 10 * 10;
		for (int i = 0; i < allSoulsInCurrentLocation.size(); i++) {
			BlockPos pos = allSoulsInCurrentLocation.get(i);

			double distSq = pos.distanceSq(Minecraft.getMinecraft().thePlayer.getPosition());

			if (distSq < closestDistSq) {
				closestDistSq = distSq;
				closestIndex = i;
			}
		}
		if (closestIndex != -1) {
			foundSoulsInLocation.add(closestIndex);
			refreshMissingSoulInfo(true);
		}
	}

	public void markAllAsFound() {
		if (!trackSouls) {
			print(EnumChatFormatting.RED + "Fairy soul tracking is turned off, turn it on using /neu");
			return;
		}
		if (allSoulsInCurrentLocation == null) {
			print(EnumChatFormatting.RED + "No fairy souls found in your current world");
			return;
		}
		for (int i = 0; i < allSoulsInCurrentLocation.size(); i++) {
			foundSoulsInLocation.add(i);
		}
		refreshMissingSoulInfo(true);

		print(EnumChatFormatting.DARK_PURPLE + "Marked all fairy souls as found");
	}

	public void markAllAsMissing() {
		if (!trackSouls) {
			print(EnumChatFormatting.RED + "Fairy soul tracking is turned off, turn it on using /neu");
			return;
		}
		if (allSoulsInCurrentLocation == null) {
			print(EnumChatFormatting.RED + "No fairy souls found in your current world");
			return;
		}
		foundSoulsInLocation.clear();
		refreshMissingSoulInfo(true);

		print(EnumChatFormatting.DARK_PURPLE + "Marked all fairy souls as not found");
	}

	private HashMap<String, Set<Integer>> getFoundSoulsForProfile() {
		String profile = SBInfo.getInstance().currentProfile;
		if (profile == null) {
			if (allProfilesFoundSouls.containsKey(unknownProfile))
				return allProfilesFoundSouls.get(unknownProfile);
		} else {
			profile = profile.toLowerCase(Locale.getDefault());
			if (allProfilesFoundSouls.containsKey(unknownProfile)) {
				HashMap<String, Set<Integer>> unknownProfileData = allProfilesFoundSouls.remove(unknownProfile);
				allProfilesFoundSouls.put(profile, unknownProfileData);
				return unknownProfileData;
			}
			if (allProfilesFoundSouls.containsKey(profile)) {
				return allProfilesFoundSouls.get(profile);
			} else {
				// Create a new entry for this profile
				HashMap<String, Set<Integer>> profileData = new HashMap<>();
				allProfilesFoundSouls.put(profile, profileData);
				return profileData;
			}
		}
		return new HashMap<>();
	}

	private static List<BlockPos> loadLocationFairySoulsFromConfig(String currentLocation) {
		JsonObject fairySoulList = Constants.FAIRYSOULS;
		if (fairySoulList == null) {
			return null;
		}

		if (!fairySoulList.has(currentLocation) || !fairySoulList.get(currentLocation).isJsonArray()) {
			return null;
		}

		JsonArray locations = fairySoulList.get(currentLocation).getAsJsonArray();
		List<BlockPos> locationSouls = new ArrayList<>();
		for (int i = 0; i < locations.size(); i++) {
			try {
				String coord = locations.get(i).getAsString();

				String[] split = coord.split(",");
				if (split.length == 3) {
					String xS = split[0];
					String yS = split[1];
					String zS = split[2];

					int x = Integer.parseInt(xS);
					int y = Integer.parseInt(yS);
					int z = Integer.parseInt(zS);

					locationSouls.add(new BlockPos(x, y, z));
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		if (locationSouls.size() == 0) {
			return null;
		}

		return locationSouls;
	}

	public void loadFoundSoulsForAllProfiles(File file, Gson gson) {
		allProfilesFoundSouls = new HashMap<>();
		String fileContent;
		try {
			fileContent = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))
				.lines()
				.collect(Collectors.joining(System.lineSeparator()));
		} catch (FileNotFoundException e) {
			// it is possible that the collected_fairy_souls.json won't exist
			return;
		}

		try {
			//noinspection UnstableApiUsage
			Type multiProfileSoulsType = new TypeToken<HashMap<String, HashMap<String, Set<Integer>>>>() {}.getType();
			allProfilesFoundSouls = gson.fromJson(fileContent, multiProfileSoulsType);
			if (allProfilesFoundSouls == null){
				allProfilesFoundSouls = new HashMap<>();
			}
		} catch (JsonSyntaxException e) {
			//The file is in the old format, convert it to the new one and set the profile to unknown
			try {
				//noinspection UnstableApiUsage
				Type singleProfileSoulsType = new TypeToken<HashMap<String, Set<Integer>>>() {}.getType();
				allProfilesFoundSouls.put(unknownProfile, gson.fromJson(fileContent, singleProfileSoulsType));
			} catch (JsonSyntaxException e2) {
				System.err.println("Can't read file containing collected fairy souls, resetting.");
			}
		}
	}

	public void saveFoundSoulsForAllProfiles(File file, Gson gson) {
		try {
			//noinspection ResultOfMethodCallIgnored
			file.createNewFile();

			try (
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file),
					StandardCharsets.UTF_8
				))
			) {
				writer.write(gson.toJson(allProfilesFoundSouls));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void tick() {
		if (!trackSouls) return;
		String location = SBInfo.getInstance().getLocation();
		if (location == null || location.isEmpty()) return;

		if (!location.equals(currentLocation)) {
			currentLocation = location;
			initializeLocation();
		}

		refreshMissingSoulInfo(false);
	}

	private static void print(String s) {
		Utils.addChatMessage(s);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
	public void onChatReceived(ClientChatReceivedEvent event) {
		if (!trackSouls || event.type == 2) return;

		var cleanString = StringUtils.cleanColour(event.message.getUnformattedText());
 		if (cleanString.equals("You have already found that Fairy Soul!") || cleanString.equals("SOUL! You found a Fairy Soul!")) {
			markClosestSoulFound();
		}
	}
}
