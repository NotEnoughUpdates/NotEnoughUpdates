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

package io.github.moulberry.notenoughupdates.miscfeatures.item.enchants;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.LRUCache;
import io.github.moulberry.notenoughupdates.util.LateBindingChroma;
import io.github.moulberry.notenoughupdates.util.Utils;
import lombok.var;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class EnchantStyleCustomizer {

	public static EnchantStyleCustomizer INSTANCE = new EnchantStyleCustomizer();

	LRUCache<String, LateBindingChroma> enchantLineCache = LRUCache.memoize(this::replaceEnchantLine, 1000);
	List<String> lastEnchant = new ArrayList<>();

	public LateBindingChroma replaceEnchantLine(String originalLine) {
		var line = originalLine;
		for (String enchantMatcherStr : NotEnoughUpdates.INSTANCE.config.hidden.enchantColours) {
			var enchantMatcherP = EnchantMatcher.fromSaveFormatMemoized.apply(enchantMatcherStr);
			if (!enchantMatcherP.isPresent()) continue;
			var enchantMatcher = enchantMatcherP.get();
			var matcher = enchantMatcher.getPatternWithLevels().matcher(line);
			var matchIterations = 0;
			while (matcher.find() && matchIterations++ < 5) {
				var enchantName = matcher.group(EnchantMatcher.GROUP_ENCHANT_NAME);
				var levelText = matcher.group(EnchantMatcher.GROUP_LEVEL);
				if (enchantName == null || levelText == null
					|| levelText.isEmpty() || enchantName.isEmpty()) continue;
				if (Utils.cleanColour(enchantName).startsWith(" ")) continue;

				var level = Utils.parseIntOrRomanNumeral(levelText);
				if (!enchantMatcher.doesLevelMatch(level)) continue;

				var startMatch = matcher.start();
				var endLevel = matcher.end(EnchantMatcher.GROUP_LEVEL);

				line = line.substring(0, startMatch)
					+ enchantMatcher.getFormatting() + enchantName + " " + levelText
					+ (endLevel >= line.length() ? "" : line.substring(endLevel));
			}
		}
		return LateBindingChroma.of(line);
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
		var lineIndex = 0;
		for (var iterator = event.toolTip.listIterator(); iterator.hasNext(); ) {
			var nextLine = iterator.next();
			var replacedLine = enchantLineCache.apply(nextLine).render(lineIndex++);
			if (!nextLine.equals(replacedLine)) {
				iterator.set(replacedLine);
			}
		}
	}

}
