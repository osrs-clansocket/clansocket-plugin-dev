package com.clansocket.tracking.progression.xp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Skill;

import com.clansocket.ClanSocketConfig;
import com.clansocket.bus.primitive.AbstractWarmupSnapshotTracker;
import com.clansocket.bus.primitive.ArmedState;
import com.clansocket.protocol.common.Payload;
import com.clansocket.protocol.skills.SkillEntry;
import com.clansocket.tracking.progression.ProgressionConstants;

@Singleton
public class StatsSnapshotTracker extends AbstractWarmupSnapshotTracker
{
	@SuppressWarnings("checkstyle:StaticFinalOutsideConstants")
	private static final Skill[] SKILLS = Skill.values();

	@Inject
	private XpState state;
	@Inject
	private ClanSocketConfig config;

	public StatsSnapshotTracker() {
		super(ProgressionConstants.LOGIN_WARMUP_TICKS);
	}

	@Override
	protected ArmedState state()
	{
		return state;
	}

	@Override
	protected boolean configAllows()
	{
		return config.streamSkillsSnapshot();
	}

	@Override
	protected void buildAndEmit()
	{
		for (final Skill skill : SKILLS)
		{
			state.setXp(skill, client.getSkillExperience(skill));
			state.setRealLevel(skill, client.getRealSkillLevel(skill));
		}
		final List<SkillEntry> entries = new ArrayList<>(SKILLS.length);
		for (final Skill skill : SKILLS)
		{
			entries.add(new SkillEntry(skill.name(), client.getRealSkillLevel(skill),
			        client.getBoostedSkillLevel(skill), client.getSkillExperience(skill)));
		}
		batcher.enqueue(
		        new Payload("stats", "hash", state.snapshotHash(), "skills", Collections.unmodifiableList(entries)));
	}
}
