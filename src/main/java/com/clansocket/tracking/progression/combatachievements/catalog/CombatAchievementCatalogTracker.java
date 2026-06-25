package com.clansocket.tracking.progression.combatachievements.catalog;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.RuneScapeProfileChanged;

import com.clansocket.ClanSocketConfig;
import com.clansocket.bus.AbstractStateTracker;
import com.clansocket.bus.Hashes;
import com.clansocket.protocol.common.Payload;
import com.clansocket.protocol.progression.CatalogEntry;
import com.clansocket.tracking.progression.combatachievements.CombatAchievementPrimer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class CombatAchievementCatalogTracker extends AbstractStateTracker
{
	@Inject
	private CombatAchievementPrimer primer;
	@Inject
	private ClanSocketConfig config;

	@Override
	protected void onLoginScreen()
	{
		primer.reset();
	}

	@Subscribe
	public void onRuneScapeProfileChanged(final RuneScapeProfileChanged event)
	{
		primer.reset();
	}

	@Subscribe
	public void onGameTick(final GameTick tick)
	{
		if (!isLoggedIn() || !config.streamCombatAchievements() || primer.isCatalogLoaded())
		{
			return;
		}
		if (!primer.tickShouldPrime())
		{
			return;
		}
		final List<CatalogEntry> entries = CombatAchievementCatalogReader.readAll(client);
		if (entries.isEmpty())
		{
			log.debug("ClanSocket CA: catalog empty, retrying");
			primer.recordLoadFailure();
			return;
		}
		primer.markLoaded(entries);
		batcher.enqueue(new Payload("combat_achievements_catalog", "hash", catalogHash(entries), "tasks",
		        Collections.unmodifiableList(entries)));
	}

	private static String catalogHash(final List<CatalogEntry> entries)
	{
		return Hashes.ofMapped(entries, e -> new Object[]{e.taskId, e.tier, e.name, e.points, e.bossId});
	}
}
