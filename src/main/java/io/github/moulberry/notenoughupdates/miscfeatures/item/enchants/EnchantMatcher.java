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

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.miscgui.GuiEnchantColour;
import io.github.moulberry.notenoughupdates.util.LRUCache;
import lombok.Value;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.Loader;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Value
public class EnchantMatcher {
	public static final String GROUP_ENCHANT_NAME = "enchantName";
	public static final String GROUP_LEVEL = "level";

	private final static String romanNumerals =
		"(I|II|III|IV|V|VI|VII|VIII|IX|X|XI|XII|XIII|XIV|XV|XVI|XVII|XVIII|XIX|XX)";
	private final static Supplier<String> validColors = Suppliers.memoize(() ->
		"0123456789abcdefz" + (Loader.isModLoaded("skyblockaddons") ? "Z" : ""));

	public static LRUCache<String, Optional<EnchantMatcher>> fromSaveFormatMemoized =
		LRUCache.memoize(
			EnchantMatcher::fromSaveFormat,
			() -> NotEnoughUpdates.INSTANCE.config.hidden.enchantColours.size() + 1
		);

	Pattern patternWithLevels;
	int compareWith;
	String formatting;
	char compareUsing;

	/**
	 * Use {@link #fromSaveFormatMemoized} instead.
	 */
	@Deprecated
	public static Optional<EnchantMatcher> fromSaveFormat(String saveFormat) {
		List<String> colourOps = GuiEnchantColour.splitter.splitToList(saveFormat);
		String enchantName = GuiEnchantColour.getColourOpIndex(colourOps, 0);
		String comparator = GuiEnchantColour.getColourOpIndex(colourOps, 1);
		String comparison = GuiEnchantColour.getColourOpIndex(colourOps, 2);
		String colourCode = GuiEnchantColour.getColourOpIndex(colourOps, 3);
		String modifier = GuiEnchantColour.getColourOpIndex(colourOps, 4);

		int intModifier = GuiEnchantColour.getIntModifier(modifier);

		if (comparator.length() != 1
			|| colourCode.length() != 1
			|| comparison.isEmpty()
			|| enchantName.isEmpty()) return Optional.empty();

		if (!">=<".contains(comparator)) return Optional.empty();
		int compareWith;
		try {
			compareWith = Integer.parseInt(comparison);
		} catch (NumberFormatException e) {
			return Optional.empty();
		}

		if (!validColors.get().contains(colourCode)) return Optional.empty();
		Pattern patternWithLevels;
		try {
			patternWithLevels = Pattern.compile(
				"(§b|§9|§([b9l])§d§l)(?<" + GROUP_ENCHANT_NAME + ">" + enchantName + ") " +
					"(?<" + GROUP_LEVEL + ">\\d+|" + romanNumerals + ")(?:(?:§9)?,|(?: §8(?:,?[0-9]+)*)?$)");
		} catch (PatternSyntaxException e) {
			NotEnoughUpdates.LOGGER.warn("Invalid pattern constructed for enchant matching", e);
			return Optional.empty();
		}

		String formatting = "§" + colourCode;

		if ((intModifier & GuiEnchantColour.BOLD_MODIFIER) != 0) {
			formatting += EnumChatFormatting.BOLD;
		}
		if ((intModifier & GuiEnchantColour.ITALIC_MODIFIER) != 0) {
			formatting += EnumChatFormatting.ITALIC;
		}
		if ((intModifier & GuiEnchantColour.UNDERLINE_MODIFIER) != 0) {
			formatting += EnumChatFormatting.UNDERLINE;
		}
		if ((intModifier & GuiEnchantColour.OBFUSCATED_MODIFIER) != 0) {
			formatting += EnumChatFormatting.OBFUSCATED;
		}
		if ((intModifier & GuiEnchantColour.STRIKETHROUGH_MODIFIER) != 0) {
			formatting += EnumChatFormatting.STRIKETHROUGH;
		}

		return Optional.of(new EnchantMatcher(patternWithLevels, compareWith, formatting, comparator.charAt(0)));
	}

	public boolean doesLevelMatch(int level) {
		switch (compareUsing) {
			case '>':
				return level > compareWith;
			case '=':
				return level == compareWith;
			case '<':
				return level < compareWith;
		}
		return false;
	}

}
