package com.clansocket.tracking.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;

import com.clansocket.ClanSocketConfig;
import com.clansocket.bus.Hashes;
import com.clansocket.bus.primitive.LatchedSnapshotTracker;
import com.clansocket.protocol.common.Payload;
import com.clansocket.protocol.state.BoostEntry;

@Singleton
public class BoostTracker extends LatchedSnapshotTracker<Long>
{
	@SuppressWarnings("checkstyle:StaticFinalOutsideConstants")
	private static final Skill[] SKILLS = Skill.values();

	@Inject
	private ClanSocketConfig config;

	@Subscribe
	public void onGameTick(final GameTick tick)
	{
		if (!config.streamBoosts() || !isLoggedIn())
		{
			return;
		}
		long sig = 1L;
		for (final Skill s : SKILLS)
		{
			sig = sig * Hashes.HASH_PRIME + (client.getBoostedSkillLevel(s) - client.getRealSkillLevel(s));
		}
		if (!latch.update(sig))
		{
			return;
		}
		final List<BoostEntry> entries = new ArrayList<>();
		for (final Skill s : SKILLS)
		{
			final int diff = client.getBoostedSkillLevel(s) - client.getRealSkillLevel(s);
			if (diff != 0)
			{
				entries.add(new BoostEntry(s.name(), diff));
			}
		}
		batcher.enqueue(new Payload("boosts", "hash", Hashes.of(sig), "boosts", Collections.unmodifiableList(entries)));
	}
}
