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

package io.github.moulberry.notenoughupdates.miscfeatures.item;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.miscfeatures.item.enchants.EnchantMatcher;
import io.github.moulberry.notenoughupdates.util.LRUCache;
import io.github.moulberry.notenoughupdates.util.Utils;
import lombok.var;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

public class EnchantStyleCustomizer {

	public static EnchantStyleCustomizer INSTANCE = new EnchantStyleCustomizer();

	LRUCache<String, String> enchantLineCache = LRUCache.memoize(this::replaceEnchantLine, 1000);
	List<String> lastEnchant = new ArrayList<>();

	public String replaceEnchantLine(String originalLine) {
		String line = originalLine;
		for (String enchantMatcherStr : NotEnoughUpdates.INSTANCE.config.hidden.enchantColours) {
			Optional<EnchantMatcher> enchantMatcherP = EnchantMatcher.fromSaveFormatMemoized.apply(enchantMatcherStr);
			if (!enchantMatcherP.isPresent()) continue;
			EnchantMatcher enchantMatcher = enchantMatcherP.get();
			Matcher matcher = enchantMatcher.getPatternWithLevels().matcher(line);
			int matchIterations = 0;
			while (matcher.find() && matchIterations++ < 5) {
				String enchantName = matcher.group(EnchantMatcher.GROUP_ENCHANT_NAME);
				String levelText = matcher.group(EnchantMatcher.GROUP_LEVEL);
				if (enchantName == null || levelText == null
					|| levelText.isEmpty() || enchantName.isEmpty()) continue;
				if (Utils.cleanColour(enchantName).startsWith(" ")) continue;

				int level = Utils.parseIntOrRomanNumeral(levelText);
				if (!enchantMatcher.doesLevelMatch(level)) continue;

				int startMatch = matcher.start();
				int endLevel = matcher.end(EnchantMatcher.GROUP_LEVEL);
				line = line.substring(0, startMatch)
					+ enchantMatcher.getFormatting() + enchantName + " " + levelText
					+ (endLevel >= line.length() ? "" : line.substring(endLevel));
			}
		}
		return line;
	}

	public void cacheInvalidate() {
		enchantLineCache.clearCache();
	}

	@SubscribeEvent
	public void onItemTooltip(ItemTooltipEvent event) {
		var nbt = event.itemStack.getTagCompound();
		if (nbt == null) return;
		var extraAttributes = nbt.getCompoundTag("ExtraAttributes");
		var enchantments = extraAttributes.getCompoundTag("enchantments");
		var attributes = extraAttributes.getCompoundTag("attributes");
		enchantments.merge(attributes);
		if (!lastEnchant.equals(NotEnoughUpdates.INSTANCE.config.hidden.enchantColours)) {
			cacheInvalidate();
			lastEnchant = new ArrayList<>(NotEnoughUpdates.INSTANCE.config.hidden.enchantColours);
		}
		if (enchantments.getKeySet().isEmpty()) return;
		for (var iterator = event.toolTip.listIterator(); iterator.hasNext(); ) {
			var nextLine = iterator.next();
			var replacedLine = enchantLineCache.apply(nextLine);
			if (!nextLine.equals(replacedLine)) {
				iterator.set(replacedLine);
			}
		}
	}

}
