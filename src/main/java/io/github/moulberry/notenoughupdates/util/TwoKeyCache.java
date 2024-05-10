package io.github.moulberry.notenoughupdates.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class TwoKeyCache<K1, K2, V> {

	private final Map<K1, Map<K2, V>> cache = new HashMap<>();

	public V getOrPut(K1 key1, K2 key2, Supplier<V> valueSupplier) {
		Map<K2, V> innerMap = Utils.getOrPut(cache, key1, HashMap::new);
		return Utils.getOrPut(innerMap, key2, valueSupplier);
	}
}
