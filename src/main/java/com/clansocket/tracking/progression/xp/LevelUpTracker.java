package com.clansocket.tracking.progression.xp;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;
import net.runelite.client.eventbus.Subscribe;

import com.clansocket.ClanSocketConfig;
import com.clansocket.bus.EventBatcher;
import com.clansocket.protocol.common.Payload;
import com.clansocket.tracking.movement.LocationContext;

@Singleton
public class LevelUpTracker
{
	@Inject
	private EventBatcher batcher;
	@Inject
	private XpState state;
	@Inject
	private ClanSocketConfig config;
	@Inject
	private LocationContext locationContext;

	@Subscribe
	public void onStatChanged(final StatChanged event)
	{
		if (!config.streamLevelUps() || !state.isArmed())
		{
			return;
		}
		final Skill skill = event.getSkill();
		final int realLevel = event.getLevel();
		final int prevLevel = state.getRealLevel(skill);
		final int currentXp = event.getXp();
		final int prevXp = state.getXp(skill);
		if (realLevel > prevLevel && prevLevel > 0)
		{
			batcher.enqueue(new Payload("level_up", "skill", skill.name(), "level", realLevel, "levelBefore", prevLevel,
			        "xpBefore", (long) prevXp, "xpGained", currentXp - prevXp, "where", locationContext.capture()));
		}
		state.setRealLevel(skill, realLevel);
	}
}
