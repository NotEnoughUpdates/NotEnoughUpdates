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

package io.github.moulberry.notenoughupdates.util;

/**
 * Wrapper around a {@link T} that implements hashing and equality according to object identity instead of the objects
 * default equals implementation.
 */
public final class IdentityCharacteristics<T> {
	private final T object;

	public IdentityCharacteristics(T object) {
		this.object = object;
	}

	public static <T> IdentityCharacteristics<T> of(T object) {
		return new IdentityCharacteristics<>(object);
	}

	public T getObject() {
		return object;
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(object);
	}

	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	@Override
	public boolean equals(Object obj) {
		return obj == object;
	}

}
