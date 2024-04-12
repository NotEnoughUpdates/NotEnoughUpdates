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

package io.github.moulberry.notenoughupdates.util;

import com.google.common.base.Splitter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.miscfeatures.tablisttutorial.TablistAPI;
import lombok.var;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NEUAutoSubscribe
public class XPInformation {
	private static final XPInformation INSTANCE = new XPInformation();

	public static XPInformation getInstance() {
		return INSTANCE;
	}

	public static class SkillInfo {
		public int level;
		public double totalXp;
		public double currentXp;
		public double currentXpMax;
		public boolean fromApi = false;
	}

	private final HashMap<String, SkillInfo> skillInfoMap = new HashMap<>();
	public HashMap<String, Float> updateWithPercentage = new HashMap<>();
	public HashMap<String, Float> increment = new HashMap<>();

	private static final Splitter SPACE_SPLITTER = Splitter.on("  ").omitEmptyStrings().trimResults();
	private static final Pattern SKILL_PATTERN = Pattern.compile(
		"\\+(\\d+(?:,\\d+)*(?:\\.\\d+)?) (.+) \\((\\d+(?:,\\d+)*(?:\\.\\d+)?)/(\\d+(?:,\\d+)*(?:\\.\\d+)?)\\)");
	private static final Pattern SKILL_PATTERN_MULTIPLIER =
		Pattern.compile("\\+(\\d+(?:,\\d+)*(?:\\.\\d+)?) (.+) \\((\\d+(?:,\\d+)*(?:\\.\\d+)?)/(\\d+(?:k|m|b))\\)");
	private static final Pattern SKILL_PATTERN_PERCENTAGE =
		Pattern.compile("\\+(\\d+(?:,\\d+)*(?:\\.\\d+)?) (.+) \\((\\d\\d?(?:\\.\\d\\d?)?)%\\)");

	public HashMap<String, SkillInfo> getSkillInfoMap() {
		return skillInfoMap;
	}

	private Set<String> failedSkills = new HashSet<>();

	public @Nullable SkillInfo getSkillInfo(String skillName) {
		return getSkillInfo(skillName, false);
	}

	public @Nullable SkillInfo getSkillInfo(String skillName, boolean isHighlyInterested) {
		var obj = skillInfoMap.get(skillName.toLowerCase());
		if (isHighlyInterested && failedSkills.contains(skillName.toLowerCase())) {
			TablistAPI.getWidgetLines(TablistAPI.WidgetNames.SKILLS);
		}
		return obj;
	}

	private String lastActionBar = null;

	@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
	public void onChatReceived(ClientChatReceivedEvent event) {
		if (event.type != 2) {
			return;
		}
		JsonObject leveling = Constants.LEVELING;
		if (leveling == null) return;

		String actionBar = StringUtils.cleanColour(event.message.getUnformattedText());

		if (lastActionBar != null && lastActionBar.equalsIgnoreCase(actionBar)) {
			return;
		}
		lastActionBar = actionBar;

		List<String> components = SPACE_SPLITTER.splitToList(actionBar);

		for (String component : components) {
			Matcher matcher = SKILL_PATTERN.matcher(component);
			if (matcher.matches()) {
				String skillS = matcher.group(2);
				String currentXpS = matcher.group(3).replace(",", "");
				String maxXpS = matcher.group(4).replace(",", "");

				float currentXp = Float.parseFloat(currentXpS);
				float maxXp = Float.parseFloat(maxXpS);

				SkillInfo skillInfo = new SkillInfo();
				skillInfo.currentXp = currentXp;
				skillInfo.currentXpMax = maxXp;
				skillInfo.totalXp = currentXp;

				JsonArray levelingArray = leveling.getAsJsonArray("leveling_xp");
				for (int i = 0; i < levelingArray.size(); i++) {
					float cap = levelingArray.get(i).getAsFloat();
					if (maxXp > 0 && maxXp <= cap) {
						break;
					}

					skillInfo.totalXp += cap;
					skillInfo.level++;
				}

				skillInfoMap.put(skillS.toLowerCase(), skillInfo);
				return;
			} else {
				matcher = SKILL_PATTERN_PERCENTAGE.matcher(component);
				if (matcher.matches()) {
					String skillS = matcher.group(2);
					String xpPercentageS = matcher.group(3).replace(",", "");

					float xpPercentage = Float.parseFloat(xpPercentageS);
					if (updateWithPercentage.containsKey(skillS.toLowerCase())) {
						failedSkills.add(skillS.toLowerCase());
					}
					updateWithPercentage.put(skillS.toLowerCase(), xpPercentage);
					increment.put(skillS.toLowerCase(), Float.parseFloat(matcher.group(1).replace(",", "")));
				} else {
					matcher = SKILL_PATTERN_MULTIPLIER.matcher(component);

					if (matcher.matches()) {
						String skillS = matcher.group(2);
						String currentXpS = matcher.group(3).replace(",", "");
						String maxXpS = matcher.group(4).replace(",", "");

						float maxMult = 1;
						if (maxXpS.endsWith("k")) {
							maxMult = 1000;
							maxXpS = maxXpS.substring(0, maxXpS.length() - 1);
						} else if (maxXpS.endsWith("m")) {
							maxMult = 1000000;
							maxXpS = maxXpS.substring(0, maxXpS.length() - 1);
						} else if (maxXpS.endsWith("b")) {
							maxMult = 1000000000;
							maxXpS = maxXpS.substring(0, maxXpS.length() - 1);
						}

						float currentXp = Float.parseFloat(currentXpS);
						float maxXp = Float.parseFloat(maxXpS) * maxMult;

						SkillInfo skillInfo = new SkillInfo();
						skillInfo.currentXp = currentXp;
						skillInfo.currentXpMax = maxXp;
						skillInfo.totalXp = currentXp;

						JsonArray levelingArray = leveling.getAsJsonArray("leveling_xp");
						for (int i = 0; i < levelingArray.size(); i++) {
							float cap = levelingArray.get(i).getAsFloat();
							if (maxXp > 0 && maxXp <= cap) {
								break;
							}

							skillInfo.totalXp += cap;
							skillInfo.level++;
						}

						skillInfoMap.put(skillS.toLowerCase(), skillInfo);
						return;
					}
				}
			}
		}
	}

	private Pattern tablistSkillPattern =
		Pattern.compile(
			" (?<type>[^ ]+) (?<level>\\d+): (?:(?<percentage>\\d+(\\.\\d+)?)%|(?<amount>[0-9,]+(\\.\\d+)?)/.*|(?<max>MAX))");

	// Car Pentry
	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		var widgetLines = TablistAPI.getOptionalWidgetLines(TablistAPI.WidgetNames.SKILLS);
		for (String widgetLine : widgetLines) {
			Matcher matcher = tablistSkillPattern.matcher(Utils.cleanColour(widgetLine));
			if (!matcher.matches())
				continue;
			var type = matcher.group("type");
			assert type != null;
			var level = Integer.parseInt(matcher.group("level"));
			var percentage = matcher.group("percentage");
			var percentageAsNumber = percentage != null ? Double.parseDouble(percentage) / 100 : null;
			var amount = matcher.group("amount");
			var amountAsNumber = amount != null ? Double.parseDouble(amount.replace(",", "")) : null;
			var isMax = matcher.group("max") != null;
			// TODO: use this extra information for good (not evil)
			updateLevel(type.toLowerCase(), level);
		}
	}

	public void updateLevel(String skill, int level) {
		if (updateWithPercentage.containsKey(skill)) {
			JsonObject leveling = Constants.LEVELING;
			if (leveling == null) return;

			SkillInfo newSkillInfo = new SkillInfo();
			newSkillInfo.totalXp = 0;
			newSkillInfo.level = level;

			JsonArray levelingArray = leveling.getAsJsonArray("leveling_xp");
			for (int i = 0; i < levelingArray.size(); i++) {
				float cap = levelingArray.get(i).getAsFloat();
				if (i == level) {
					newSkillInfo.currentXp += updateWithPercentage.get(skill) / 100f * cap;
					newSkillInfo.totalXp += newSkillInfo.currentXp;
					newSkillInfo.currentXpMax = cap;
					break;
				} else {
					newSkillInfo.totalXp += cap;
				}
			}

			SkillInfo oldSkillInfo = skillInfoMap.get(skill.toLowerCase());
			float inc = increment.getOrDefault(skill.toLowerCase(), 0F);
			if (oldSkillInfo != null && oldSkillInfo.totalXp + inc > newSkillInfo.totalXp && oldSkillInfo.totalXp - inc * 5 < newSkillInfo.totalXp) {
				SkillInfo incrementedSkillInfo = new SkillInfo();
				incrementedSkillInfo.totalXp = oldSkillInfo.totalXp + inc;
				boolean isNotLevelUp = oldSkillInfo.currentXp + inc < oldSkillInfo.currentXpMax;
				incrementedSkillInfo.level =
					(isNotLevelUp) ? oldSkillInfo.level : oldSkillInfo.level + 1;
				incrementedSkillInfo.currentXp =
					isNotLevelUp ? oldSkillInfo.currentXp + inc : oldSkillInfo.currentXp + inc - oldSkillInfo.currentXpMax;
				incrementedSkillInfo.currentXpMax =
					incrementedSkillInfo.level < levelingArray.size() && incrementedSkillInfo.level >= 0
						? levelingArray.get(incrementedSkillInfo.level).getAsFloat()
						: 0F;
				skillInfoMap.put(skill.toLowerCase(), incrementedSkillInfo);
			} else {
				skillInfoMap.put(skill.toLowerCase(), newSkillInfo);
			}
			failedSkills.remove(skill.toLowerCase());
		}
		updateWithPercentage.clear();
	}
}
