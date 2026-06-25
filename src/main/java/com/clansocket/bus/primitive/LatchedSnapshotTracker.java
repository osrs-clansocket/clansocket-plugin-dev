package com.clansocket.bus.primitive;

import com.clansocket.bus.AbstractStateTracker;
import com.clansocket.bus.Latched;

public abstract class LatchedSnapshotTracker<T> extends AbstractStateTracker
{
	protected final Latched<T> latch = new Latched<>();

	@Override
	protected void onLoginScreen()
	{
		latch.reset();
	}

	@Override
	protected void emitFreshSnapshot()
	{
		latch.reset();
	}
}
