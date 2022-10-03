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

package io.github.moulberry.notenoughupdates.miscfeatures.killswitch;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Optional;

public class Shimmy {

	static final Gson gson = new Gson();
	final MethodHandle getter, setter;
	final Class<?> clazz;

	public Shimmy(MethodHandle getter, MethodHandle setter, Class<?> clazz) {
		this.getter = getter;
		this.setter = setter;
		this.clazz = clazz;
	}

	public Object get() {
		try {
			return getter.invoke();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public void set(Object value) {
		try {
			setter.invoke(value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public JsonElement getJson() {
		return gson.toJsonTree(get());
	}

	public void setJson(JsonElement element) {
		set(gson.fromJson(element, clazz));
	}

	private static Object shimmy(Object source, String path) {
		if (source == null) return null;
		Class<?> aClass = source.getClass();
		try {
			Field declaredField = aClass.getDeclaredField(path);
			return declaredField.get(source);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			return null;
		}
	}

	public static Optional<Shimmy> of(Object source, String... path) {
		if (path.length == 0)
			return Optional.empty();
		for (int i = 0; i < path.length - 1; i++) {
			source = shimmy(source, path[i]);
		}
		if (source == null)
			return Optional.empty();
		MethodHandles.Lookup lookup = MethodHandles.publicLookup();
		try {
			String lastName = path[path.length - 1];
			Field field = source.getClass().getDeclaredField(lastName);
			return Optional.of(new Shimmy(
				lookup.unreflectGetter(field).bindTo(source),
				lookup.unreflectSetter(field).bindTo(source),
				field.getType()
			));
		} catch (NoSuchFieldException | IllegalAccessException e) {
			return Optional.empty();
		}
	}

}
