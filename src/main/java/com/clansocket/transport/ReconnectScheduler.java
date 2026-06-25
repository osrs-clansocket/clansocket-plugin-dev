package com.clansocket.transport;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.clansocket.ClanSocketConstants;

@Singleton
@SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
public class ReconnectScheduler
{
	@Inject
	private ScheduledExecutorService executor;
	private long delayMs = ClanSocketConstants.RECONNECT_INITIAL_MS;
	private ScheduledFuture<?> task;

	public synchronized void reset()
	{
		delayMs = ClanSocketConstants.RECONNECT_INITIAL_MS;
	}

	public synchronized void cancel()
	{
		if (task != null)
		{
			task.cancel(false);
			task = null;
		}
	}

	public synchronized void schedule(final Runnable reopen)
	{
		task = executor.schedule(reopen, delayMs, TimeUnit.MILLISECONDS);
		delayMs = Math.min(delayMs * 2, ClanSocketConstants.RECONNECT_MAX_MS);
	}
}
