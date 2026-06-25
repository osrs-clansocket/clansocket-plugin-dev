package com.clansocket.tracking.progression.combatachievements.progress;

import java.util.HashSet;
import java.util.Set;

import net.runelite.api.Client;

import com.clansocket.tracking.progression.combatachievements.CombatAchievementConstants;

final class CombatAchievementCompletionState
{
	private CombatAchievementCompletionState() {
	}

	static Set<Integer> readCompletedTaskIds(final Client client)
	{
		final Set<Integer> completed = new HashSet<>();
		for (int varpIdx = 0; varpIdx < CombatAchievementConstants.COMPLETION_VARP_IDS.length; varpIdx++)
		{
			final int varp = client.getVarpValue(CombatAchievementConstants.COMPLETION_VARP_IDS[varpIdx]);
			if (varp == 0)
			{
				continue;
			}
			final int base = varpIdx * CombatAchievementConstants.BITS_PER_VARP;
			for (int bit = 0; bit < CombatAchievementConstants.BITS_PER_VARP; bit++)
			{
				if ((varp & (1 << bit)) != 0)
				{
					completed.add(base + bit);
				}
			}
		}
		return completed;
	}
}
