package com.clansocket.tracking.progression.combatachievements.progress;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.RuneScapeProfileChanged;

import com.clansocket.ClanSocketConfig;
import com.clansocket.bus.AbstractStateTracker;
import com.clansocket.protocol.common.Payload;
import com.clansocket.protocol.progression.CatalogEntry;
import com.clansocket.tracking.progression.combatachievements.CombatAchievementConstants;
import com.clansocket.tracking.progression.combatachievements.CombatAchievementPrimer;

@Singleton
public class CombatAchievementProgressTracker extends AbstractStateTracker
{
	@Inject
	private ClientThread clientThread;
	@Inject
	private ClanSocketConfig config;
	@Inject
	private CombatAchievementPrimer primer;

	private Set<Integer> lastCompleted = Collections.emptySet();
	private boolean snapshotEmitted;

	@Override
	protected void onLoginScreen()
	{
		snapshotEmitted = false;
		lastCompleted = Collections.emptySet();
	}

	@Subscribe
	public void onRuneScapeProfileChanged(final RuneScapeProfileChanged event)
	{
		snapshotEmitted = false;
		lastCompleted = Collections.emptySet();
	}

	@Override
	protected void emitFreshSnapshot()
	{
		snapshotEmitted = false;
	}

	@Subscribe
	public void onGameTick(final GameTick tick)
	{
		if (!isLoggedIn() || snapshotEmitted || !config.streamCombatAchievements() || !primer.isCatalogLoaded())
		{
			return;
		}
		lastCompleted = CombatAchievementCompletionState.readCompletedTaskIds(client);
		batcher.enqueue(CombatAchievementSnapshotBuilder.build(lastCompleted, primer.catalog()));
		snapshotEmitted = true;
	}

	@Subscribe
	public void onChatMessage(final ChatMessage event)
	{
		if (config.streamCombatAchievements() && isCompletionMessage(event))
		{
			clientThread.invokeLater(this::detectAndEmitCompletions);
		}
	}

	private void detectAndEmitCompletions()
	{
		if (!primer.isCatalogLoaded())
		{
			return;
		}
		final Set<Integer> current = CombatAchievementCompletionState.readCompletedTaskIds(client);
		if (emitNewCompletions(current))
		{
			lastCompleted = current;
			batcher.enqueue(CombatAchievementSnapshotBuilder.build(current, primer.catalog()));
		}
	}

	private boolean emitNewCompletions(final Set<Integer> current)
	{
		final int pointsBefore = computePointsBefore();
		boolean any = false;
		for (final Integer taskId : current)
		{
			if (lastCompleted.contains(taskId))
			{
				continue;
			}
			final CatalogEntry entry = primer.catalog().get(taskId);
			if (entry == null)
			{
				continue;
			}
			batcher.enqueue(new Payload("combat_achievement_completed", "taskId", entry.taskId, "name", entry.name,
			        "tier", entry.tier, "taskType", entry.taskType, "points", entry.points, "bossId", entry.bossId,
			        "bossName", entry.bossName, "pointsBefore", pointsBefore));
			any = true;
		}
		return any;
	}

	private int computePointsBefore()
	{
		int sum = 0;
		for (final Integer taskId : lastCompleted)
		{
			final CatalogEntry priorEntry = primer.catalog().get(taskId);
			if (priorEntry != null)
			{
				sum += priorEntry.points;
			}
		}
		return sum;
	}

	private static boolean isCompletionMessage(final ChatMessage event)
	{
		final ChatMessageType t = event.getType();
		if (t != ChatMessageType.GAMEMESSAGE && t != ChatMessageType.SPAM)
		{
			return false;
		}
		final String msg = event.getMessage();
		return msg != null && msg.contains(CombatAchievementConstants.CHAT_COMPLETION_PREFIX)
		        && msg.contains(CombatAchievementConstants.CHAT_COMPLETION_TASK_TOKEN);
	}
}
