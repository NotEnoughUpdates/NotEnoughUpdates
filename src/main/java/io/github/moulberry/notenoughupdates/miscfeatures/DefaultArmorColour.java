/*
 * Copyright (C) 2023 NotEnoughUpdates contributors
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

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public class DefaultArmorColour {

	private static boolean shownError = false;

	public static int getDefaultArmorColour(ItemArmor item, ItemStack stack) {
		JsonObject itemJson = NotEnoughUpdates.INSTANCE.manager
			.createItemResolutionQuery()
			.withItemStack(stack)
			.resolveToItemListJson();

		if (itemJson != null && itemJson.has("nbttag")) {
			String[] nbttag = itemJson.get("nbttag").getAsString().split(",");

			for (String tag : nbttag) {
				if (tag.contains("color")) {

					try {
						return Integer.parseInt(tag.split(":")[1]);
					} catch (NumberFormatException exception) {
						if (!shownError) {
							Utils.addChatMessage("§e[NEU] §cNEU ran into an error trying to parse an integer! " +
								"Report this in the discord along with the latest.txt log file.");
							shownError = true;
						}

						System.err.println("[NEU] Tried parsing: " + tag.split(":")[1] + " to Integer");
						exception.printStackTrace();
					}
				}
			}
		}

		return item.getColor(stack);
	}
}
