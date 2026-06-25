package com.clansocket.bus.primitive;

import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;

import com.clansocket.bus.AbstractStateTracker;

public abstract class AbstractWarmupSnapshotTracker extends AbstractStateTracker
{
	protected final WarmupCounter warmup;

	protected AbstractWarmupSnapshotTracker(final int warmupTickThreshold) {
		this.warmup = new WarmupCounter(warmupTickThreshold);
	}

	protected abstract ArmedState state();

	protected abstract boolean configAllows();

	protected abstract void buildAndEmit();

	@Override
	protected final void onLoginScreen()
	{
		warmup.reset();
		state().clear();
	}

	@Override
	protected final void emitFreshSnapshot()
	{
		if (!configAllows() || !isLoggedIn())
		{
			return;
		}
		state().disarm();
		warmup.skipToReady();
		buildAndEmit();
		state().arm();
	}

	@Subscribe
	public final void onGameTick(final GameTick tick)
	{
		if (!configAllows() || state().isArmed() || !isLoggedIn())
		{
			return;
		}
		if (!warmup.tickAndReady())
		{
			return;
		}
		buildAndEmit();
		state().arm();
	}
}
