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

/**
 * This debouncer always triggers on the leading edge.
 * <p>
 * Calling {@link #trigger} will only result in a truthy return value the first time it is called
 * within {@link #getDelayInNanoSeconds()} nanoseconds.
 */
public class Debouncer {
	private long lastPinged = 0L;
	private final long delay;

	public Debouncer(long minimumDelayInNanoSeconds) {
		this.delay = minimumDelayInNanoSeconds;
	}

	public long getDelayInNanoSeconds() {
		return delay;
	}

	public synchronized long timePassed() {
		// longs are technically not atomic reads since they use two 32 bit registers
		// so, yes, this technically has to be synchronized
		return System.nanoTime() - lastPinged;
	}

	public synchronized boolean trigger() {
		long newPingTime = System.nanoTime();
		long newDelay = newPingTime - lastPinged;
		lastPinged = newPingTime;
		return newDelay >= this.delay;
	}

}
