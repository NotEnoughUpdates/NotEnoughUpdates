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

public class NameModifier extends EntityViewerModifier {
	@Override
	public EntityLivingBase applyModifier(EntityLivingBase base, JsonObject info) {
		if (base instanceof GUIClientPlayer) {
			((GUIClientPlayer) base).setName(info.get("name").getAsString());
		}
		base.setCustomNameTag(info.get("name").getAsString());
		return base;
	}
}
