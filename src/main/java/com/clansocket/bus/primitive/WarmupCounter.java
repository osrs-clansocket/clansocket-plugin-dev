package com.clansocket.bus.primitive;

public final class WarmupCounter
{
	private final int threshold;
	private int ticks;

	public WarmupCounter(final int threshold) {
		this.threshold = threshold;
	}

	public boolean tickAndReady()
	{
		ticks++;
		return ticks >= threshold;
	}

	public void reset()
	{
		ticks = 0;
	}

	public void skipToReady()
	{
		ticks = threshold - 1;
	}
}
