package com.clansocket.bus.primitive;

import java.util.Optional;

import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;

import com.clansocket.bus.AbstractStateTracker;
import com.clansocket.protocol.common.Payload;

public abstract class AbstractPolledSnapshotTracker extends AbstractStateTracker
{
	private final AbstractSnapshotEmitter emitter;
	private final int warmupTickWait;
	private final int repollInterval;
	private volatile boolean armed;
	private volatile int warmupTicks;
	private volatile int repollTicks;

	protected AbstractPolledSnapshotTracker(final AbstractSnapshotEmitter emitter, final int warmupTickWait,
	        final int repollInterval) {
		this.emitter = emitter;
		this.warmupTickWait = warmupTickWait;
		this.repollInterval = repollInterval;
	}

	public final Optional<Payload> currentSnapshot()
	{
		return emitter.snapshotNow();
	}

	protected final void emitSnapshot()
	{
		emitter.emit();
	}

	@Override
	protected final void onLoginScreen()
	{
		armed = false;
		warmupTicks = 0;
		repollTicks = 0;
		emitter.clearFingerprint();
	}

	@Override
	protected final void onLoggedIn()
	{
		armed = true;
		warmupTicks = 0;
	}

	@Override
	protected final void emitFreshSnapshot()
	{
		emitter.clearFingerprint();
		armed = true;
		warmupTicks = 0;
	}

	@Subscribe
	public final void onGameTick(final GameTick tick)
	{
		if (!isLoggedIn())
		{
			return;
		}
		if (armed)
		{
			if (warmupTicks++ < warmupTickWait)
			{
				return;
			}
			emitSnapshot();
			armed = false;
			repollTicks = 0;
			return;
		}
		if (++repollTicks >= repollInterval)
		{
			repollTicks = 0;
			emitSnapshot();
		}
	}
}
