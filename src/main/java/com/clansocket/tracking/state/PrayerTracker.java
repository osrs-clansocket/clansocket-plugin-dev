package com.clansocket.tracking.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Prayer;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.Subscribe;

import com.clansocket.ClanSocketConfig;
import com.clansocket.bus.primitive.IntIntMap;
import com.clansocket.bus.primitive.LatchedSnapshotTracker;
import com.clansocket.protocol.common.Payload;
import com.clansocket.protocol.state.PrayerActive;

@Singleton
public class PrayerTracker extends LatchedSnapshotTracker<Long>
{
	@SuppressWarnings("checkstyle:StaticFinalOutsideConstants")
	private static final IntIntMap VARBIT_TO_ORDINAL;
	@SuppressWarnings("checkstyle:StaticFinalOutsideConstants")
	private static final String[] PRAYER_NAMES;

	static
	{
		if (Prayer.values().length > Long.SIZE)
		{
			throw new IllegalStateException("Prayer enum has " + Prayer.values().length
			        + " values, exceeds long bitmask capacity (" + Long.SIZE + ")");
		}
		VARBIT_TO_ORDINAL = new IntIntMap(Prayer.values().length, StateConstants.VARBIT_MISS);
		PRAYER_NAMES = new String[Prayer.values().length];
		for (final Prayer p : Prayer.values())
		{
			VARBIT_TO_ORDINAL.put(p.getVarbit(), p.ordinal());
			PRAYER_NAMES[p.ordinal()] = p.name();
		}
	}

	@Inject
	private ClanSocketConfig config;

	private long activeMask;

	@Override
	protected void onLoginScreen()
	{
		super.onLoginScreen();
		activeMask = 0L;
	}

	@Override
	protected void onLoggedIn()
	{
		seedMaskFromClient();
	}

	@Override
	protected void emitFreshSnapshot()
	{
		super.emitFreshSnapshot();
		seedMaskFromClient();
	}

	@Subscribe
	public void onVarbitChanged(final VarbitChanged event)
	{
		final int ordinal = VARBIT_TO_ORDINAL.get(event.getVarbitId());
		if (ordinal == StateConstants.VARBIT_MISS)
		{
			return;
		}
		final long bit = 1L << ordinal;
		activeMask = event.getValue() == 1 ? activeMask | bit : activeMask & ~bit;
	}

	@Subscribe
	public void onGameTick(final GameTick tick)
	{
		if (!config.streamPrayer() || !isLoggedIn())
		{
			return;
		}
		if (!latch.update(activeMask))
		{
			return;
		}
		emit(activeMask);
	}

	private void seedMaskFromClient()
	{
		long mask = 0L;
		for (final Prayer p : Prayer.values())
		{
			if (client.getVarbitValue(p.getVarbit()) == 1)
			{
				mask |= 1L << p.ordinal();
			}
		}
		activeMask = mask;
	}

	private void emit(final long mask)
	{
		final List<PrayerActive> active = new ArrayList<>(Long.bitCount(mask));
		long m = mask;
		while (m != 0L)
		{
			final int bit = Long.numberOfTrailingZeros(m);
			active.add(new PrayerActive(bit, PRAYER_NAMES[bit]));
			m &= m - 1L;
		}
		batcher.enqueue(
		        new Payload("prayers", "hash", Long.toHexString(mask), "active", Collections.unmodifiableList(active)));
	}
}
