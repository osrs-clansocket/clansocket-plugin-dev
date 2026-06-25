package com.clansocket.tracking.progression.combatachievements.progress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.clansocket.bus.Hashes;
import com.clansocket.protocol.common.Payload;
import com.clansocket.protocol.progression.CatalogEntry;
import com.clansocket.protocol.progression.CompletedTask;
import com.clansocket.tracking.progression.combatachievements.CombatAchievementConstants;

final class CombatAchievementSnapshotBuilder
{
	private CombatAchievementSnapshotBuilder() {
	}

	static Payload build(final Set<Integer> completed, final Map<Integer, CatalogEntry> catalogById)
	{
		final Map<String, Integer> tierCounts = countByTier(completed, catalogById);
		final List<Integer> sorted = new ArrayList<>(completed);
		Collections.sort(sorted);
		final List<CompletedTask> tasks = new ArrayList<>(sorted.size());
		for (final Integer taskId : sorted)
		{
			final CatalogEntry entry = catalogById.get(taskId);
			tasks.add(new CompletedTask(taskId, entry == null ? null : entry.name));
		}
		return new Payload("combat_achievements_snapshot", "hash", Hashes.of(tierCounts, sorted), "tierCounts",
		        Collections.unmodifiableMap(tierCounts), "totalCompleted", completed.size(), "completedTasks",
		        Collections.unmodifiableList(tasks));
	}

	private static Map<String, Integer> countByTier(final Set<Integer> completed,
	        final Map<Integer, CatalogEntry> catalogById)
	{
		final Map<String, Integer> tierCounts = new LinkedHashMap<>();
		for (final String tier : CombatAchievementConstants.TIER_NAMES)
		{
			tierCounts.put(tier, 0);
		}
		for (final Integer taskId : completed)
		{
			final CatalogEntry entry = catalogById.get(taskId);
			if (entry != null)
			{
				tierCounts.merge(entry.tier, 1, Integer::sum);
			}
		}
		return tierCounts;
	}
}
