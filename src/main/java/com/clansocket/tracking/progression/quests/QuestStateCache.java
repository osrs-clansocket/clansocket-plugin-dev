package com.clansocket.tracking.progression.quests;

import java.util.EnumMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;

import com.clansocket.bus.primitive.ArmedState;

@Singleton
public class QuestStateCache extends ArmedState
{
	@Inject
	private Client client;

	private final Map<Quest, QuestState> last = new EnumMap<>(Quest.class);

	@Override
	public void clear()
	{
		last.clear();
		disarm();
	}

	public QuestState put(final Quest quest, final QuestState state)
	{
		return last.put(quest, state);
	}

	public QuestState get(final Quest quest)
	{
		return last.get(quest);
	}

	@SuppressWarnings("checkstyle:IllegalCatch")
	public QuestState safeState(final Quest quest)
	{
		try
		{
			return quest.getState(client);
		} catch (final RuntimeException ignored)
		{
			return null;
		}
	}
}
