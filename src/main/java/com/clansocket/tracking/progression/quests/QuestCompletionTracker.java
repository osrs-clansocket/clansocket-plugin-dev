package com.clansocket.tracking.progression.quests;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.Subscribe;

import com.clansocket.ClanSocketConfig;
import com.clansocket.bus.AbstractTracker;
import com.clansocket.protocol.common.Payload;
import com.clansocket.tracking.movement.LocationContext;

@Singleton
public class QuestCompletionTracker extends AbstractTracker
{
	@Inject
	private QuestStateCache cache;
	@Inject
	private ClanSocketConfig config;
	@Inject
	private LocationContext locationContext;

	private boolean dirty;

	@Subscribe
	public void onVarbitChanged(final VarbitChanged event)
	{
		dirty = true;
	}

	@Subscribe
	public void onGameTick(final GameTick tick)
	{
		if (!config.streamQuests() || !dirty || !cache.isArmed() || !isLoggedIn())
		{
			return;
		}
		dirty = false;
		final int completedBefore = countFinishedInCache();
		for (final Quest q : Quest.values())
		{
			processQuest(q, completedBefore);
		}
	}

	private int countFinishedInCache()
	{
		int count = 0;
		for (final Quest q : Quest.values())
		{
			if (cache.get(q) == QuestState.FINISHED)
			{
				count++;
			}
		}
		return count;
	}

	private void processQuest(final Quest q, final int completedBefore)
	{
		final QuestState current = cache.safeState(q);
		if (current == null)
		{
			return;
		}
		final QuestState prev = cache.put(q, current);
		if (current == QuestState.FINISHED && prev != null && prev != QuestState.FINISHED)
		{
			batcher.enqueue(new Payload("quest_completed", "id", q.getId(), "name", q.name(), "questsCompletedBefore",
			        completedBefore, "where", locationContext.capture()));
		}
	}
}
