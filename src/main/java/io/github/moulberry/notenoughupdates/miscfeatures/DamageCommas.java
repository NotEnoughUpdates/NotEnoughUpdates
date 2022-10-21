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
import io.github.moulberry.notenoughupdates.core.util.StringUtils;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DamageCommas {
	private static final WeakHashMap<EntityLivingBase, ChatComponentText> replacementMap = new WeakHashMap<>();

	private static final EnumChatFormatting[] coloursHypixel = {
		EnumChatFormatting.WHITE,
		EnumChatFormatting.YELLOW,
		EnumChatFormatting.GOLD,
		EnumChatFormatting.RED,
		EnumChatFormatting.RED,
		EnumChatFormatting.WHITE
	};

	private static final char STAR = '\u2727';
	private static final char OVERLOAD_STAR = '\u272F';
	private static final Pattern PATTERN_CRIT = Pattern.compile(
		"\u00a7f" + STAR + "((?:\u00a7.\\d(?:§.,)?)+)\u00a7." + STAR + "(.*)");
	private static final Pattern PATTERN_NO_CRIT = Pattern.compile("(\u00a7.)([\\d+,]*)(.*)");
	private static final Pattern OVERLOAD_PATTERN = Pattern.compile("(\u00a7.)" + OVERLOAD_STAR + "((?:\u00a7.[\\d,])+)(\u00a7.)" + OVERLOAD_STAR + "\u00a7r");

	public static IChatComponent replaceName(EntityLivingBase entity) {
		if (!entity.hasCustomName()) return entity.getDisplayName();

		IChatComponent name = entity.getDisplayName();
		if (!NotEnoughUpdates.INSTANCE.config.misc.damageIndicatorStyle2) return name;
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) return name;

		if (replacementMap.containsKey(entity)) {
			ChatComponentText component = replacementMap.get(entity);
			if (component == null) return name;
			return component;
		}

		String formatted = name.getFormattedText();

		boolean crit = false;
		String numbers;
		String prefix;
		String suffix;

		Matcher matcherCrit = PATTERN_CRIT.matcher(formatted);
		Matcher matcherOverload = OVERLOAD_PATTERN.matcher(formatted);
		if (matcherCrit.matches()) {
			crit = true;
			numbers = StringUtils.cleanColour(matcherCrit.group(1)).replace(",", "");
			prefix = "\u00a7f" + STAR;
			suffix = "\u00a7f" + STAR + matcherCrit.group(2);
		} else if (matcherOverload.matches()) {
				crit = true;
        numbers = StringUtils.cleanColour(matcherOverload.group(2)).replace(",", "");
        prefix = matcherOverload.group(1) + OVERLOAD_STAR;
        suffix = matcherOverload.group(3) + OVERLOAD_STAR + "\u00a7r";
			} else {
			Matcher matcherNoCrit = PATTERN_NO_CRIT.matcher(formatted);
			if (matcherNoCrit.matches()) {
				numbers = matcherNoCrit.group(2).replace(",", "");
				prefix = matcherNoCrit.group(1);
				suffix = "\u00A7r" + (matcherNoCrit.group(3).contains("♞") ? "\u00A7d" + matcherNoCrit.group(3) : matcherNoCrit.group(3));
			} else {
				replacementMap.put(entity, null);
				return name;
			}
		}

		StringBuilder newFormatted = new StringBuilder();

		try {
			int number = Integer.parseInt(numbers);

			if (number > 999) {
				newFormatted.append(Utils.shortNumberFormat(number, 0));
			} else {
				return name;
			}
		} catch (NumberFormatException e) {
			replacementMap.put(entity, null);
			return name;
		}

		if (crit) {
			StringBuilder newFormattedCrit = new StringBuilder();

			int colourIndex = 0;
			for (char c : newFormatted.toString().toCharArray()) {
				if (c == ',') {
					newFormattedCrit.append(EnumChatFormatting.GRAY);
				} else {
					newFormattedCrit.append(coloursHypixel[colourIndex++ % coloursHypixel.length]);
				}
				newFormattedCrit.append(c);
			}

			newFormatted = newFormattedCrit;
		}

		ChatComponentText finalComponent = new ChatComponentText(prefix + newFormatted + suffix);

		replacementMap.put(entity, finalComponent);
		return finalComponent;

        /*if (formatted.startsWith("\u00A7f\u2727")) System.out.println(formatted);

        if (formatted.length() >= 7 && (formatted.startsWith("\u00A7f\u2727") || formatted.startsWith("\u00A7f\u2694")) &&
                (formatted.endsWith("\u2727\u00a7r") || formatted.endsWith("\u2694\u00a7r"))) {

            if (NotEnoughUpdates.INSTANCE.config.misc.damageIndicatorStyle == 2) {
                String numbers = Utils.cleanColour(formatted.substring(3, formatted.length() - 3)).trim().replaceAll("[^0-9]", "");
                try {
                    int damage = Integer.parseInt(numbers);

                    String damageString;
                    if (damage > 999) {
                        damageString = Utils.shortNumberFormat(damage, 0);
                    } else {
                        damageString = NumberFormat.getIntegerInstance().format(damage);
                    }

                    StringBuilder colouredString = new StringBuilder();
                    int colourIndex = 0;
                    for (int i = 0; i < damageString.length(); i++) {
                        int index = damageString.length() - 1 - i;
                        char c = damageString.charAt(index);
                        if (c >= '0' && c <= '9') {
                            colouredString.insert(0, c);
                            colouredString.insert(0, colours[colourIndex++ % colours.length]);
                        } else {
                            colouredString.insert(0, c);
                        }
                    }

                    ChatComponentText ret = new ChatComponentText("\u00A7f\u2727" + colouredString + "\u00a7r\u2727\u00a7r");
                    replacementMap.put(hashCode, ret);
                    return ret;
                } catch (NumberFormatException ignored) {}
            }

            StringBuilder builder = new StringBuilder();
            boolean numLast = false;
            boolean colLast = false;
            boolean colLastLast;
            int numCount = 0;
            for (int i = formatted.length() - 4; i >= 3; i--) {
                char c = formatted.charAt(i);
                colLastLast = colLast;

                if (c == '\u00a7') {
                    if (numLast) numCount--;
                    numLast = false;
                    colLast = true;
                } else if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f')) {
                    numLast = true;
                    colLast = false;
                    numCount++;
                } else {
                    if (colLast) {
                        replacementMap.put(hashCode, null);
                        return name;
                    }
                    numLast = false;
                }

                if (colLastLast && numLast && numCount > 1 && (numCount - 1) % 3 == 0) builder.append(',');
                builder.append(c);
            }

            ChatComponentText ret = new ChatComponentText("\u00A7f\u2727" + builder.reverse().toString() + "\u2727\u00a7r");
            replacementMap.put(hashCode, ret);
            return ret;
        }

        if (formatted.length() >= 5 && formatted.startsWith(EnumChatFormatting.GRAY.toString()) &&
                formatted.endsWith(EnumChatFormatting.RESET.toString())) {
            String damageS = formatted.substring(2, formatted.length() - 2);

            for (int i = 0; i < damageS.length(); i++) {
                char c = damageS.charAt(i);
                if (c < '0' || c > '9') {
                    replacementMap.put(hashCode, null);
                    return name;
                }
            }

            try {
                int damage = Integer.parseInt(damageS);

                String damageFormatted;
                if (NotEnoughUpdates.INSTANCE.config.misc.damageIndicatorStyle == 2 && damage > 999) {
                    damageFormatted = Utils.shortNumberFormat(damage, 0);
                } else {
                    damageFormatted = NumberFormat.getIntegerInstance().format(damage);
                }

                ChatComponentText ret = new ChatComponentText(EnumChatFormatting.GRAY + damageFormatted + EnumChatFormatting.RESET);
                replacementMap.put(hashCode, ret);
                return ret;
            } catch (Exception e) {
                replacementMap.put(hashCode, null);
                return name;
            }
        }
        replacementMap.put(hashCode, null);
        return name;*/
	}
}
