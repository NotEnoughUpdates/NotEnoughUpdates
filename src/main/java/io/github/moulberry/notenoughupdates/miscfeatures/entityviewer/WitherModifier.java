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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;

public class WitherModifier extends EntityViewerModifier {
	@Override
	public EntityLivingBase applyModifier(EntityLivingBase base, JsonObject info) {
		if (!(base instanceof EntityWither))
			return null;
		EntityWither wither = (EntityWither) base;
		if (info.has("tiny")) {
			if (info.get("tiny").getAsBoolean()) {
				wither.setInvulTime(800);
			} else {
				wither.setInvulTime(0);
			}
		}
		if (info.has("armored")) {
			if (info.get("armored").getAsBoolean()) {
				wither.setHealth(1);
			} else {
				wither.setHealth(wither.getMaxHealth());
			}
		}
		return base;
	}
}
