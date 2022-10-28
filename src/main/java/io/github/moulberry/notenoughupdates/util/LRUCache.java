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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntSupplier;

public interface LRUCache<K, V> extends Function<K, V> {

	static <K, V> LRUCache<K, V> memoize(Function<K, V> mapper, int maxCacheSize) {
		return memoize(mapper, () -> maxCacheSize);
	}

	static <K, V> LRUCache<K, V> memoize(Function<K, V> mapper, IntSupplier maxCacheSize) {
		Map<K, V> cache = new LinkedHashMap<K, V>(10, 0.75F, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
				return this.size() > maxCacheSize.getAsInt();
			}
		};
		Map<K, V> synchronizedCache = Collections.synchronizedMap(cache);
		return new LRUCache<K, V>() {
			@Override
			public void clearCache() {
				synchronizedCache.clear();
			}

			@Override
			public int size() {
				return synchronizedCache.size();
			}

			@Override
			public V apply(K k) {
				return synchronizedCache.computeIfAbsent(k, mapper);
			}
		};
	}

	int size();

	void clearCache();
}
