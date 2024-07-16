/*
 * Copyright (C) 2024 NotEnoughUpdates contributors
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

import cc.polyfrost.oneconfig.gui.elements.config.ConfigDropdown;
import io.github.moulberry.moulconfig.observer.Property;

import java.lang.reflect.Field;

public class WrappedConfigDropdown extends ConfigDropdown {

	public WrappedConfigDropdown(
		Field field,
		Object parent,
		String name,
		String description,
		String category,
		String subcategory,
		int size,
		String[] options
	) {
		super(field, parent, name, description, category, subcategory, size, options);
		if (field.getType() != Property.class) {
			throw new IllegalArgumentException("field must be of type Property");
		}
	}

	@Override
	public Object get() throws IllegalAccessException {
		if (field == null) return null;
		Property<Enum> property = (Property<Enum>) field.get(parent);
		return property.get().ordinal();
	}

	@Override
	protected void set(Object object) throws IllegalAccessException {
		if (field == null) return;
		Property<Enum> property = (Property<Enum>) field.get(parent);
		property.set(((Enum[]) property.get().getDeclaringClass().getEnumConstants())[(int) object]);
	}
}
