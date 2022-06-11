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

package io.github.moulberry.notenoughupdates.miscfeatures.entityviewer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class HorseModifier extends EntityViewerModifier {
	@Override
	public EntityLivingBase applyModifier(EntityLivingBase base, JsonObject info) {
		if (!(base instanceof EntityHorse))
			return null;
		EntityHorse horse = (EntityHorse) base;
		if (info.has("kind")) {
			String type = info.get("kind").getAsString().intern();
			switch (type) {
				case "skeleton":
					horse.setHorseType(4);
					break;
				case "zombie":
					horse.setHorseType(3);
					break;
				case "mule":
					horse.setHorseType(2);
					break;
				case "donkey":
					horse.setHorseType(1);
					break;
				case "horse":
					horse.setHorseType(0);
					break;
				default:
					throw new IllegalArgumentException("Unknown horse type: " + type);
			}
		}
		if (info.has("armor")) {
			JsonElement el = info.get("armor");
			if (el.isJsonNull()) {
				horse.setHorseArmorStack(null);
			} else {
				Item item;
				switch (el.getAsString().intern()) {
					case "iron":
						item = Items.iron_horse_armor;
						break;
					case "golden":
						item = Items.golden_horse_armor;
						break;
					case "diamond":
						item = Items.diamond_horse_armor;
						break;
					default:
						throw new IllegalArgumentException("Unknown horse armor: " + el.getAsString());
				}
				horse.setHorseArmorStack(new ItemStack(item));
			}
		}
		if (info.has("saddled")) {
			horse.setHorseSaddled(info.get("saddled").getAsBoolean());
		}
		return horse;
	}
}
