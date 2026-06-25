package com.clansocket.tracking.progression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.Subscribe;

import com.clansocket.ClanSocketConfig;
import com.clansocket.bus.AbstractStateTracker;
import com.clansocket.bus.Hashes;
import com.clansocket.protocol.common.Payload;
import com.clansocket.protocol.diaries.DiaryEntry;
import com.clansocket.tracking.movement.LocationContext;

@Singleton
public class DiaryTracker extends AbstractStateTracker
{
	@Inject
	private ClanSocketConfig config;
	@Inject
	private LocationContext locationContext;

	private final Map<Integer, Boolean> lastState = new HashMap<>();
	private int loginTicks;
	private boolean dataSettled;

	@Override
	protected void onLoginScreen()
	{
		dataSettled = false;
		loginTicks = 0;
		lastState.clear();
	}

	@Override
	protected void emitFreshSnapshot()
	{
		dataSettled = false;
		loginTicks = 0;
	}

	@Subscribe
	public void onGameTick(final GameTick tick)
	{
		if (!config.streamDiaries() || !isLoggedIn() || dataSettled)
		{
			return;
		}
		loginTicks++;
		if (loginTicks >= ProgressionConstants.LOGIN_WARMUP_TICKS)
		{
			emitSnapshot();
			dataSettled = true;
		}
	}

	@Subscribe
	public void onVarbitChanged(final VarbitChanged event)
	{
		if (!config.streamDiaries())
		{
			return;
		}
		final DiaryEntries.Entry e = DiaryEntries.BY_VARBIT.get(event.getVarbitId());
		if (e == null || !isLoggedIn())
		{
			return;
		}
		final boolean now = client.getVarbitValue(e.varbit) > 0;
		final Boolean prev = lastState.put(e.varbit, now);
		if (dataSettled && now && prev != null && !prev)
		{
			batcher.enqueue(new Payload("diary_completed", "region", e.region, "tier", e.tier, "diariesCompletedBefore",
			        countTierCompletedBefore(e), "where", locationContext.capture()));
		}
	}

	private int countTierCompletedBefore(final DiaryEntries.Entry justCompleted)
	{
		int count = 0;
		for (final DiaryEntries.Entry other : DiaryEntries.ALL)
		{
			if (other.varbit == justCompleted.varbit || !other.tier.equals(justCompleted.tier))
			{
				continue;
			}
			final Boolean state = lastState.get(other.varbit);
			if (state != null && state)
			{
				count++;
			}
		}
		return count;
	}

	private void emitSnapshot()
	{
		if (!config.streamDiaries() || !isLoggedIn())
		{
			return;
		}
		final List<DiaryEntry> out = new ArrayList<>(DiaryEntries.ALL.length);
		for (final DiaryEntries.Entry e : DiaryEntries.ALL)
		{
			final boolean complete = client.getVarbitValue(e.varbit) > 0;
			lastState.put(e.varbit, complete);
			out.add(new DiaryEntry(e.region, e.tier, complete));
		}
		final String hash = Hashes.ofMapped(out, e -> new Object[]{e.complete});
		batcher.enqueue(new Payload("diaries", "hash", hash, "diaries", Collections.unmodifiableList(out)));
	}
}
