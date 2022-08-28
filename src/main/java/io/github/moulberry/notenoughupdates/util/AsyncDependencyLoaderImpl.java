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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

class AsyncDependencyLoaderImpl<R, T> implements AsyncDependencyLoader<T> {

	private final Supplier<R> supplyDependency;
	private final Function<R, CompletableFuture<T>> generator;
	private final BiFunction<R, R, Boolean> isDifferent;
	private volatile CompletableFuture<T> lastValue;
	private volatile R lastDependency;
	private volatile boolean isFirstFire = true;

	@Override
	public synchronized Optional<T> peekValue() {
		R nextDependency = supplyDependency.get();
		if (isFirstFire || isDifferent.apply(nextDependency, lastDependency)) {
			isFirstFire = false;
			if (lastValue != null)
				lastValue.cancel(true);
			lastValue = generator.apply(nextDependency);
		}
		lastDependency = nextDependency;
		return Optional.ofNullable(lastValue.getNow(null));
	}

	AsyncDependencyLoaderImpl(
		Supplier<R> supplyDependency,
		Function<R, CompletableFuture<T>> generator,
		BiFunction<R, R, Boolean> isDifferent
	) {
		this.supplyDependency = supplyDependency;
		this.generator = generator;
		this.isDifferent = isDifferent;
	}
}
