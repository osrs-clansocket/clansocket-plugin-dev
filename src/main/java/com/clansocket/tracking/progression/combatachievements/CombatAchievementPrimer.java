package com.clansocket.tracking.progression.combatachievements;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import com.clansocket.protocol.progression.CatalogEntry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public final class CombatAchievementPrimer
{
	private final Map<Integer, CatalogEntry> catalogById = new HashMap<>();
	private boolean catalogLoaded;
	private boolean abandoned;
	private int warmupTicks;
	private int retries;

	public void reset()
	{
		catalogById.clear();
		catalogLoaded = false;
		abandoned = false;
		warmupTicks = 0;
		retries = 0;
	}

	public Map<Integer, CatalogEntry> catalog()
	{
		return catalogById;
	}

	public boolean isCatalogLoaded()
	{
		return catalogLoaded;
	}

	public boolean tickShouldPrime()
	{
		if (abandoned)
		{
			return false;
		}
		warmupTicks++;
		return warmupTicks >= CombatAchievementConstants.CA_WARMUP_TICKS;
	}

	public void markLoaded(final List<CatalogEntry> entries)
	{
		catalogById.clear();
		for (final CatalogEntry e : entries)
		{
			catalogById.put(e.taskId, e);
		}
		catalogLoaded = true;
	}

	public void recordLoadFailure()
	{
		retries++;
		if (retries >= CombatAchievementConstants.CA_MAX_PRIME_RETRIES)
		{
			log.warn("ClanSocket CA: catalog never loaded after {} retries, abandoning", retries);
			abandoned = true;
		} else
		{
			warmupTicks = CombatAchievementConstants.CA_WARMUP_TICKS - 1;
		}
	}
}
