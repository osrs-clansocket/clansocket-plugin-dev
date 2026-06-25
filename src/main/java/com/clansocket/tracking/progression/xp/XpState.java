package com.clansocket.tracking.progression.xp;

import javax.inject.Singleton;

import net.runelite.api.Skill;

import com.clansocket.bus.Hashes;
import com.clansocket.bus.primitive.ArmedState;

@Singleton
public class XpState extends ArmedState
{
	private final int[] lastXp = new int[Skill.values().length];
	private final int[] lastRealLevel = new int[Skill.values().length];

	public int getXp(final Skill skill)
	{
		return lastXp[skill.ordinal()];
	}

	public void setXp(final Skill skill, final int xp)
	{
		lastXp[skill.ordinal()] = xp;
	}

	public int getRealLevel(final Skill skill)
	{
		return lastRealLevel[skill.ordinal()];
	}

	public void setRealLevel(final Skill skill, final int level)
	{
		lastRealLevel[skill.ordinal()] = level;
	}

	public String snapshotHash()
	{
		return Hashes.of(lastXp, lastRealLevel);
	}

	@Override
	public void clear()
	{
		for (int i = 0; i < lastXp.length; i++)
		{
			lastXp[i] = 0;
			lastRealLevel[i] = 0;
		}
		disarm();
	}
}
