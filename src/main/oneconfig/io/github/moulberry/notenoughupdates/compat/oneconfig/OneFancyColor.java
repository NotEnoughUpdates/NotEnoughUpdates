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

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigColorElement;
import io.github.moulberry.notenoughupdates.core.ChromaColour;
import lombok.var;

import java.awt.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public class OneFancyColor extends ConfigColorElement {
	public OneFancyColor(
		Field field,
		Object parent,
		String name,
		String description,
		String category,
		String subcategory,
		int size,
		boolean allowAlpha
	) {
		super(field, parent, name, description, category, subcategory, size, allowAlpha);
	}

	@Override
	public Object get() throws IllegalAccessException {
		String chromaString = (String) super.get();

		int[] decompose = ChromaColour.decompose(chromaString);
		int r = decompose[2];
		int g = decompose[1];
		int b = decompose[0];
		int a = decompose[3];
		int chr = decompose[4];

		short[] hsba = OneColor.ARGBtoHSBA(new Color(r, g, b, a).getRGB());
		if (chr > 0) {
			return new OneColor(hsba[0], hsba[1], hsba[2], hsba[3], (int) ChromaColour.getSecondsForSpeed(chr));
		}

		return new OneColor(r, g, b, a);
	}

	private static MethodHandle hsbaAccessor;
	private static MethodHandle databitAccessor;

	static {
		try {
			Field f = OneColor.class.getDeclaredField("hsba");
			Field f2 = OneColor.class.getDeclaredField("dataBit");
			f.setAccessible(true);
			f2.setAccessible(true);
			var lookup = MethodHandles.lookup();
			hsbaAccessor = lookup.unreflectGetter(f);
			databitAccessor = lookup.unreflectGetter(f2);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private static short[] getHsba(OneColor color) {
		try {
			return (short[]) hsbaAccessor.invokeExact(color);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private static int getDataBit(OneColor color) {
		try {
			return (int) databitAccessor.invokeExact(color);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void set(Object object) throws IllegalAccessException {
		OneColor color = (OneColor) object;
		float dataBit = getDataBit(color) / 1000F;
		short[] hsba = getHsba(color);
		int argb = OneColor.HSBAtoARGB(hsba[0], hsba[1], hsba[2], hsba[3]);
		Color color1 = new Color(argb, true);
		super.set(ChromaColour.special(
			dataBit > 0 ? ChromaColour.getSpeedForSeconds(dataBit) : 0, // TODO chroma still sucks
			color1.getAlpha(),
			color1.getRed(),
			color1.getGreen(),
			color1.getBlue()
		));
	}
}
