package com.clansocket.transport.consent;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

public abstract class AbstractConsentRequestStore<T extends ConsentRequestView>
{
	private final Object lock = new Object();
	private final List<Runnable> listeners = new ArrayList<>();
	private volatile T current;
	private Timer expiryTimer;

	public final T current()
	{
		synchronized (lock)
		{
			return current;
		}
	}

	public final void submit(final T req)
	{
		synchronized (lock)
		{
			current = req;
		}
		scheduleExpiry(req.getExpiresAtMs());
		fire();
	}

	public final void cancel(final long requestId)
	{
		synchronized (lock)
		{
			if (current == null || current.getRequestId() != requestId)
			{
				return;
			}
			current = null;
		}
		cancelExpiry();
		fire();
	}

	public final void clear()
	{
		synchronized (lock)
		{
			if (current == null)
			{
				return;
			}
			current = null;
		}
		cancelExpiry();
		fire();
	}

	public final void addListener(final Runnable listener)
	{
		synchronized (lock)
		{
			listeners.add(listener);
		}
	}

	private void scheduleExpiry(final long expiresAtMs)
	{
		cancelExpiry();
		final long now = System.currentTimeMillis();
		if (now >= expiresAtMs)
		{
			clear();
			return;
		}
		final long msUntilExpiry = expiresAtMs - now;
		expiryTimer = new Timer((int) Math.min(msUntilExpiry, (long) Integer.MAX_VALUE), e -> clear());
		expiryTimer.setRepeats(false);
		expiryTimer.start();
	}

	private void cancelExpiry()
	{
		if (expiryTimer != null)
		{
			expiryTimer.stop();
			expiryTimer = null;
		}
	}

	private void fire()
	{
		final List<Runnable> snapshot;
		synchronized (lock)
		{
			snapshot = new ArrayList<>(listeners);
		}
		for (final Runnable l : snapshot)
		{
			l.run();
		}
	}
}
