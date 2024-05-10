package io.github.moulberry.notenoughupdates.util;

import javafx.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class TwoKeyCache<K1, K2, V> {

	private final Map<Pair<K1, K2>, V> cache = new HashMap<>();

	public V getOrPut(K1 key1, K2 key2, Supplier<V> valueSupplier) {
		Pair<K1, K2> realKey = new Pair<>(key1, key2);
		V value = cache.get(realKey);
		if (value == null) {
			value = valueSupplier.get();
			cache.put(realKey, value);
		}
		return value;
	}
}
