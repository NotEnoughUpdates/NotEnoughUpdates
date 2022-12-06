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

package io.github.moulberry.notenoughupdates.compat.oneconfig;

import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigKeyBind;

import java.lang.reflect.Field;

public class OneFancyKeybind extends ConfigKeyBind {
	public OneFancyKeybind(
		Field field,
		Object parent,
		String name,
		String description,
		String category,
		String subcategory,
		int size
	) {
		super(field, parent, name, description, category, subcategory, size);
	}

	@Override
	public Object get() throws IllegalAccessException {
		OneWrappedKeyBind owk = new OneWrappedKeyBind();
		owk.value = (int) super.get();
		return owk;
	}

	@Override
	protected void set(Object object) throws IllegalAccessException {
		OneWrappedKeyBind keyBind = (OneWrappedKeyBind) object;
		super.set(keyBind.value);
	}
}
