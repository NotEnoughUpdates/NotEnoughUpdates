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

package io.github.moulberry.notenoughupdates.util;

import com.google.common.base.Objects;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface AsyncDependencyLoader<T> {

	Optional<T> peekValue();

	public static <R, T> AsyncDependencyLoader<T> withObjectIdentity(
		Supplier<R> supplier,
		Function<R, CompletableFuture<T>> generator
	) {
		return new AsyncDependencyLoaderImpl<>(supplier, generator, (a, b) -> a != b);
	}

	public static <R, T> AsyncDependencyLoader<T> withEqualsInvocation(
		Supplier<R> supplier,
		Function<R, CompletableFuture<T>> generator
	) {
		return new AsyncDependencyLoaderImpl<>(supplier, generator, (a, b) -> !Objects.equal(a, b));
	}

	public static <R, T> AsyncDependencyLoader<T> withEqualityFunction(
		Supplier<R> supplier,
		Function<R, CompletableFuture<T>> generator,
		BiFunction<R, R, Boolean> isEqual
	) {
		return new AsyncDependencyLoaderImpl<>(supplier, generator, (a, b) -> !isEqual.apply(a, b));
	}
}
