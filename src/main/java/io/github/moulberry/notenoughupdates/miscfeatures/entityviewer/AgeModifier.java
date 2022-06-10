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

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.mixins.AccessorEntityAgeable;
import io.github.moulberry.notenoughupdates.mixins.AccessorEntityArmorStand;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityZombie;

public class AgeModifier extends EntityViewerModifier {
	@Override
	public EntityLivingBase applyModifier(EntityLivingBase base, JsonObject info) {
		boolean baby = info.has("baby") && info.get("baby").getAsBoolean();
		if (base instanceof EntityAgeable) {
			((AccessorEntityAgeable) base).setGrowingAgeDirect(baby ? -1 : 1);
			return base;
		}
		if (base instanceof EntityZombie) {
			((EntityZombie) base).setChild(baby);
			return base;
		}
		if (base instanceof EntityArmorStand) {
			((AccessorEntityArmorStand) base).setSmallDirect(baby);
			return base;
		}
		System.out.println("Cannot apply age to a non ageable entity: " + base);
		return null;
	}
}
