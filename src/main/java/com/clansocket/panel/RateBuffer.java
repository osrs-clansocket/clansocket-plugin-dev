package com.clansocket.panel;

import java.util.Arrays;

public final class RateBuffer
{
	@SuppressWarnings("checkstyle:StaticFinalOutsideConstants")
	public static final int WINDOW_SECONDS = 60;

	private final Object lock = new Object();
	private final int[] buckets = new int[WINDOW_SECONDS];
	private long lastSecond = PanelConstants.UNINITIALIZED_SECOND;
	private long lastEventAtMs;

	public void bump()
	{
		synchronized (lock)
		{
			final long now = nowSeconds();
			advance(now);
			buckets[(int) (now % WINDOW_SECONDS)]++;
			lastEventAtMs = System.currentTimeMillis();
		}
	}

	public long lastEventAt()
	{
		synchronized (lock)
		{
			return lastEventAtMs;
		}
	}

	public void setLastEventAt(final long ms)
	{
		synchronized (lock)
		{
			lastEventAtMs = ms;
		}
	}

	public int[] snapshot()
	{
		final int[] out = new int[WINDOW_SECONDS];
		fillSnapshot(out);
		return out;
	}

	public void fillSnapshot(final int[] out)
	{
		synchronized (lock)
		{
			final long now = nowSeconds();
			advance(now);
			final int oldest = (int) ((now + 1) % WINDOW_SECONDS);
			for (int i = 0; i < WINDOW_SECONDS; i++)
			{
				out[i] = buckets[(oldest + i) % WINDOW_SECONDS];
			}
		}
	}

	@SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
	private void advance(final long now)
	{
		if (lastSecond == PanelConstants.UNINITIALIZED_SECOND)
		{
			lastSecond = now;
			return;
		}
		final long elapsed = now - lastSecond;
		if (elapsed <= 0L)
		{
			return;
		}
		if (elapsed >= WINDOW_SECONDS)
		{
			Arrays.fill(buckets, 0);
		} else
		{
			for (long s = lastSecond + 1L; s <= now; s++)
			{
				buckets[(int) (s % WINDOW_SECONDS)] = 0;
			}
		}
		lastSecond = now;
	}

	private static long nowSeconds()
	{
		return System.currentTimeMillis() / PanelConstants.MS_PER_SECOND;
	}
}
