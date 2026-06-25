package com.clansocket.tracking.loot;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.client.config.ConfigManager;

import com.clansocket.ClanSocketConstants;
import com.clansocket.util.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@Singleton
@SuppressWarnings("checkstyle:IllegalCatch")
public final class KillCountLookup
{
	@Inject
	private ConfigManager configManager;

	@Inject
	private Gson gson;

	private final ConcurrentMap<String, Integer> ourCounter = new ConcurrentHashMap<>();

	public Integer bumpAndLookup(final String npcName, final String lootType)
	{
		if (Strings.isEmpty(npcName))
		{
			return null;
		}
		final Integer broadcast = configManager.getRSProfileConfiguration(LootConstants.KILL_COUNT_GROUP,
		        npcName.toLowerCase(), int.class);
		if (broadcast != null)
		{
			return broadcast;
		}
		return bumpOurCounter(npcName, lootType);
	}

	private Integer bumpOurCounter(final String npcName, final String lootType)
	{
		final String key = npcName.toLowerCase();
		final int next = ourCounter.compute(key, (k, v) -> v != null ? v + 1 : seed(npcName, lootType) + 1);
		configManager.setRSProfileConfiguration(ClanSocketConstants.CONFIG_GROUP, LootConstants.OUR_KC_PREFIX + key,
		        next);
		return next;
	}

	private int seed(final String npcName, final String lootType)
	{
		final Integer persisted = configManager.getRSProfileConfiguration(ClanSocketConstants.CONFIG_GROUP,
		        LootConstants.OUR_KC_PREFIX + npcName.toLowerCase(), int.class);
		if (persisted != null)
		{
			return persisted;
		}
		final Integer historical = readFromLootTracker(npcName, lootType);
		return historical == null ? 0 : historical;
	}

	private Integer readFromLootTracker(final String npcName, final String lootType)
	{
		if (Strings.isEmpty(lootType))
		{
			return null;
		}
		final String profile = configManager.getRSProfileKey();
		if (profile == null)
		{
			return null;
		}
		final String key = LootConstants.LOOT_DROPS_KEY_PREFIX + lootType + "_" + npcName;
		final String json = configManager.getConfiguration(LootConstants.LOOT_TRACKER_GROUP, profile, key);
		if (json == null)
		{
			return null;
		}
		return parseKills(json);
	}

	private Integer parseKills(final String json)
	{
		try
		{
			final JsonObject obj = gson.fromJson(json, JsonObject.class);
			if (obj == null || !obj.has(LootConstants.LOOT_TRACKER_KILLS_FIELD))
			{
				return null;
			}
			return obj.get(LootConstants.LOOT_TRACKER_KILLS_FIELD).getAsInt();
		} catch (final RuntimeException ignored)
		{
			return null;
		}
	}
}
