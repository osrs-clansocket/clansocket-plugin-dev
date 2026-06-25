package com.clansocket.tracking.progression.xp;

import java.util.EnumMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.client.eventbus.Subscribe;

import com.clansocket.ClanSocketConfig;
import com.clansocket.bus.EventBatcher;
import com.clansocket.protocol.common.Payload;

@Singleton
public class XpDeltaTracker
{
	@Inject
	private Client client;
	@Inject
	private EventBatcher batcher;
	@Inject
	private XpState state;
	@Inject
	private ClanSocketConfig config;

	private final Map<Skill, Integer> pending = new EnumMap<>(Skill.class);

	@Subscribe
	public void onGameStateChanged(final GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			pending.clear();
		}
	}

	@Subscribe
	public void onStatChanged(final StatChanged event)
	{
		if (!config.streamXpGains() || !state.isArmed())
		{
			return;
		}
		final Skill skill = event.getSkill();
		final int xp = event.getXp();
		final int prev = state.getXp(skill);
		if (xp > prev)
		{
			pending.merge(skill, xp - prev, Integer::sum);
		}
		state.setXp(skill, xp);
	}

	@Subscribe
	public void onGameTick(final GameTick tick)
	{
		if (!config.streamXpGains() || pending.isEmpty())
		{
			return;
		}
		for (final Map.Entry<Skill, Integer> e : pending.entrySet())
		{
			final Skill skill = e.getKey();
			batcher.enqueue(new Payload("xp_gained", "skill", skill.name(), "xp", client.getSkillExperience(skill),
			        "delta", e.getValue()));
		}
		pending.clear();
	}
}
