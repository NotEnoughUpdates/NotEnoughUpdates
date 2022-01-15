package io.github.moulberry.notenoughupdates.util;

import java.time.Duration;
import java.time.Instant;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class RecencyList<T> {

	public class Entry<T> {
		private final T value;
		private Instant timestamp;
		private boolean toRemove;

		public Entry(T value, Instant timestamp) {
			this.value = value;
			this.timestamp = timestamp;
		}

		public Instant timesOutAt() {
			return timestamp.plus(timeout);
		}

		public void refresh() {
			timestamp = Instant.now();
			toRemove = false;
		}

		public boolean isTimedOut(Instant at) {
			return toRemove || timesOutAt().isBefore(at);
		}

		@Override
		@SuppressWarnings({"unchecked"})
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Entry<?> entry = (Entry<?>) o;
			return Objects.equals(timestamp, entry.timestamp) && Objects.equals(value, entry.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(timestamp, value);
		}

		public T getValue() {
			return value;
		}
	}

	public class Finalized<T> extends AbstractList<T> {
		private final List<RecencyList<T>.Entry<T>> unmodifiedCopy;

		public Finalized(List<RecencyList<T>.Entry<T>> unmodifiedCopy) {this.unmodifiedCopy = unmodifiedCopy;}

		@Override
		public T get(int index) {
			return unmodifiedCopy.get(index).value;
		}

		@Override
		public int size() {
			return unmodifiedCopy.size();
		}

		public List<RecencyList<T>.Entry<T>> getTimedList() {
			return unmodifiedCopy;
		}
	}

	public RecencyList(Duration keepAliveDuration) {
		this.timeout = keepAliveDuration;
	}

	// I feel like a Map<T, Entry<T>> would be faster, but I really don't care.
	private final Duration timeout;
	private final List<Entry<T>> entries = new ArrayList<>();
	private List<Entry<T>> unmodifiedCopy = new ArrayList<>();
	private Instant nextCheckAt = null;
	private boolean checkNow = false;

	public void ensureTimedOrdering() {
		Instant now = Instant.now();
		if (checkNow || (nextCheckAt != null && nextCheckAt.isBefore(now))) {
			entries.removeIf(it -> it.isTimedOut(now));
			entries.sort(Comparator.comparing(Entry::timesOutAt));
			unmodifiedCopy = new ArrayList<>(entries);

			nextCheckAt = entries.isEmpty() ? null : entries.get(entries.size() - 1).timesOutAt();
			checkNow = false;
		}
	}

	public Finalized<T> getList() {
		ensureTimedOrdering();
		return new Finalized<>(unmodifiedCopy);
	}

	public void add(T value) {
		for (Entry<T> entry : entries) {
			if (Objects.equals(entry.value, value)) {
				entry.refresh();
				return;
			}
		}
		checkNow = true;
		entries.add(new Entry<>(value, Instant.now()));
	}

	public void remove(T value) {
		for (Entry<T> entry : entries) {
			if (Objects.equals(entry.value, value)) {
				entry.toRemove = true;
				checkNow = true;
				return;
			}
		}
	}

	public void addAll(Collection<? extends T> collection) {
		for (T t : collection) {
			add(t);
		}
	}
}
