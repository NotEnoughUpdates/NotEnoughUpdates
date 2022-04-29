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
