package com.pxworlds.game.io;

public class Timer {
	/** The number of nanoseconds in a second. */
	private static final long NANOSECONDS_PER_SECOND = 1000000000L;

	public static double getTime() {
		return (double) System.nanoTime() / (double) NANOSECONDS_PER_SECOND;
	}
}
