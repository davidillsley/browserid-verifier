package org.i5y.browserid.verifier;

public abstract class Clock {

	public abstract long millis();

	public static Clock current() {
		return new Clock() {
			@Override
			public long millis() {
				return System.currentTimeMillis();
			}
		};
	}

	public static Clock fixed(final long millis) {
		return new Clock() {
			@Override
			public long millis() {
				return millis;
			}
		};
	}
}
