package com.clansocket.tracking.progression.quests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Quest;
import net.runelite.api.QuestState;

import com.clansocket.ClanSocketConfig;
import com.clansocket.bus.Hashes;
import com.clansocket.bus.primitive.AbstractWarmupSnapshotTracker;
import com.clansocket.bus.primitive.ArmedState;
import com.clansocket.protocol.common.Payload;
import com.clansocket.protocol.quests.QuestEntry;
import com.clansocket.tracking.progression.ProgressionConstants;

@Singleton
public class QuestSnapshotTracker extends AbstractWarmupSnapshotTracker
{
	@Inject
	private QuestStateCache cache;
	@Inject
	private ClanSocketConfig config;

	public QuestSnapshotTracker() {
		super(ProgressionConstants.QUEST_WARMUP_TICKS);
	}

	@Override
	protected ArmedState state()
	{
		return cache;
	}

	@Override
	protected boolean configAllows()
	{
		return config.streamQuests();
	}

	@Override
	protected void buildAndEmit()
	{
		final List<QuestEntry> entries = new ArrayList<>();
		for (final Quest q : Quest.values())
		{
			final QuestState state = cache.safeState(q);
			if (state == null)
			{
				continue;
			}
			cache.put(q, state);
			entries.add(new QuestEntry(q.getId(), q.name(), state.name()));
		}
		final String hash = Hashes.ofMapped(entries, e -> new Object[]{e.id, e.state});
		batcher.enqueue(new Payload("quests", "hash", hash, "quests", Collections.unmodifiableList(entries)));
	}
}
