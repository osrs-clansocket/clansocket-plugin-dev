package com.clansocket.tracking.combat;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.DBTableID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;

import com.clansocket.ClanSocketConfig;
import com.clansocket.bus.AbstractStateTracker;
import com.clansocket.bus.Hashes;
import com.clansocket.bus.primitive.IntObjectMap;
import com.clansocket.protocol.common.Payload;

@Singleton
public class SlayerTracker extends AbstractStateTracker
{
	@Inject
	private ClanSocketConfig config;

	private long lastSignature = Long.MIN_VALUE;

	private final IntObjectMap<String> regularTaskNameCache = new IntObjectMap<>();
	private final IntObjectMap<String> bossTaskNameCache = new IntObjectMap<>();
	private final IntObjectMap<String> areaNameCache = new IntObjectMap<>();

	@Override
	protected void onLoginScreen()
	{
		lastSignature = Long.MIN_VALUE;
	}

	@Override
	protected void emitFreshSnapshot()
	{
		lastSignature = Long.MIN_VALUE;
		emitSnapshot();
	}

	@Subscribe
	public void onVarbitChanged(final VarbitChanged event)
	{
		if (!config.streamSlayer())
		{
			return;
		}
		final boolean matched = CombatConstants.WATCHED_VARPS.contains(event.getVarpId())
		        || CombatConstants.WATCHED_VARBITS.contains(event.getVarbitId());
		if (!matched || !isLoggedIn())
		{
			return;
		}
		emitSnapshot();
	}

	private void emitSnapshot()
	{
		final int count = client.getVarpValue(VarPlayerID.SLAYER_COUNT);
		final int target = client.getVarpValue(VarPlayerID.SLAYER_TARGET);
		final int area = client.getVarpValue(VarPlayerID.SLAYER_AREA);
		final int countOriginal = client.getVarpValue(VarPlayerID.SLAYER_COUNT_ORIGINAL);
		final int master = client.getVarbitValue(VarbitID.SLAYER_MASTER);
		final int points = client.getVarbitValue(VarbitID.SLAYER_POINTS);
		final int tasksCompleted = client.getVarbitValue(VarbitID.SLAYER_TASKS_COMPLETED);
		final int bossId = client.getVarbitValue(VarbitID.SLAYER_TARGET_BOSSID);
		final int wildyTasksCompleted = client.getVarbitValue(VarbitID.SLAYER_WILDERNESS_TASKS_COMPLETED);
		final long sig = Hashes.rollingLong(count, target, area, countOriginal, master, points, tasksCompleted, bossId,
		        wildyTasksCompleted);
		if (sig == lastSignature || count > countOriginal || count > 0 && master == 0)
		{
			return;
		}
		lastSignature = sig;
		batcher.enqueue(new Payload("slayer", "hash", Long.toHexString(sig), "count", count, "target", target, "area",
		        area, "countOriginal", countOriginal, "master", master, "points", points, "tasksCompleted",
		        tasksCompleted, "bossId", bossId, "bossName", null, "wildyTasksCompleted", wildyTasksCompleted,
		        "masterName", CombatConstants.SLAYER_MASTERS.get(master), "targetName",
		        resolveTargetName(target, bossId), "areaName", resolveAreaName(area)));
	}

	private String resolveTargetName(final int taskId, final int bossId)
	{
		if (taskId <= 0)
		{
			return null;
		}
		final boolean isBossTask = taskId == CombatConstants.SLAYER_TASK_ID_BOSSES;
		final IntObjectMap<String> cache = isBossTask ? bossTaskNameCache : regularTaskNameCache;
		final int cacheKey = isBossTask ? bossId : taskId;
		final String cached = cache.get(cacheKey);
		if (cached != null)
		{
			return cached;
		}
		final Integer taskDbRow = isBossTask ? lookupBossTaskDbRow(bossId) : lookupRegularTaskDbRow(taskId);
		if (taskDbRow == null)
		{
			return null;
		}
		final String name = (String) client.getDBTableField(taskDbRow, DBTableID.SlayerTask.COL_NAME_UPPERCASE, 0)[0];
		if (name != null)
		{
			cache.put(cacheKey, name);
		}
		return name;
	}

	private Integer lookupBossTaskDbRow(final int bossId)
	{
		final List<Integer> rows = client.getDBRowsByValue(DBTableID.SlayerTaskSublist.ID,
		        DBTableID.SlayerTaskSublist.COL_TASK_SUBTABLE_ID, 0, bossId);
		if (rows.isEmpty())
		{
			return null;
		}
		return (Integer) client.getDBTableField(rows.get(0), DBTableID.SlayerTaskSublist.COL_TASK, 0)[0];
	}

	private Integer lookupRegularTaskDbRow(final int taskId)
	{
		final List<Integer> rows = client.getDBRowsByValue(DBTableID.SlayerTask.ID, DBTableID.SlayerTask.COL_ID, 0,
		        taskId);
		return rows.isEmpty() ? null : rows.get(0);
	}

	private String resolveAreaName(final int areaId)
	{
		if (areaId <= 0)
		{
			return null;
		}
		final String cached = areaNameCache.get(areaId);
		if (cached != null)
		{
			return cached;
		}
		final List<Integer> areaRows = client.getDBRowsByValue(DBTableID.SlayerArea.ID,
		        DBTableID.SlayerArea.COL_AREA_ID, 0, areaId);
		if (areaRows.isEmpty())
		{
			return null;
		}
		final String name = (String) client.getDBTableField(areaRows.get(0), DBTableID.SlayerArea.COL_AREA_NAME_IN_HELPER,
		        0)[0];
		if (name != null)
		{
			areaNameCache.put(areaId, name);
		}
		return name;
	}
}
