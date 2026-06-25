package com.clansocket.tracking.progression.combatachievements.catalog;

import java.util.ArrayList;
import java.util.List;

import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.StructComposition;

import com.clansocket.protocol.progression.CatalogEntry;
import com.clansocket.tracking.progression.combatachievements.CombatAchievementConstants;
import com.clansocket.util.Strings;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class CombatAchievementCatalogReader
{
	private CombatAchievementCatalogReader() {
	}

	public static List<CatalogEntry> readAll(final Client client)
	{
		final EnumComposition bossEnum = client.getEnum(CombatAchievementConstants.BOSS_NAMES_ENUM_ID);
		final List<CatalogEntry> entries = new ArrayList<>();
		for (int tierIdx = 0; tierIdx < CombatAchievementConstants.TIER_ENUM_IDS.length; tierIdx++)
		{
			readTier(new CombatAchievementTierContext(client, bossEnum, tierIdx), entries);
		}
		return entries;
	}

	private static void readTier(final CombatAchievementTierContext ctx, final List<CatalogEntry> out)
	{
		final int enumId = CombatAchievementConstants.TIER_ENUM_IDS[ctx.tierIdx];
		final EnumComposition tierEnum = ctx.client.getEnum(enumId);
		if (tierEnum == null)
		{
			log.debug("ClanSocket CA: missing tier enum {}", enumId);
			return;
		}
		final String tierName = CombatAchievementConstants.TIER_NAMES[ctx.tierIdx];
		final int points = CombatAchievementConstants.TIER_POINTS[ctx.tierIdx];
		for (final int structId : tierEnum.getIntVals())
		{
			final CatalogEntry entry = buildEntry(ctx, structId, tierName, points);
			if (entry != null)
			{
				out.add(entry);
			}
		}
	}

	private static CatalogEntry buildEntry(final CombatAchievementTierContext ctx, final int structId,
	        final String tierName, final int points)
	{
		final StructComposition struct = ctx.client.getStructComposition(structId);
		if (struct == null)
		{
			log.debug("ClanSocket CA: missing struct {}", structId);
			return null;
		}
		final int taskId = struct.getIntValue(CombatAchievementConstants.STRUCT_PARAM_TASK_ID);
		final String name = struct.getStringValue(CombatAchievementConstants.STRUCT_PARAM_NAME);
		final String desc = struct.getStringValue(CombatAchievementConstants.STRUCT_PARAM_DESC);
		final int typeId = struct.getIntValue(CombatAchievementConstants.STRUCT_PARAM_TYPE_ID);
		final int bossId = struct.getIntValue(CombatAchievementConstants.STRUCT_PARAM_BOSS_ID);
		final String typeName = CombatAchievementConstants.TYPE_NAMES.getOrDefault(typeId,
		        CombatAchievementConstants.UNKNOWN_TYPE);
		final String bossName = resolveBossName(ctx.bossEnum, bossId);
		return new CatalogEntry(taskId, name, desc, tierName, typeName, points, bossId, bossName);
	}

	private static String resolveBossName(final EnumComposition bossEnum, final int bossId)
	{
		if (bossEnum == null)
		{
			return CombatAchievementConstants.UNKNOWN_BOSS;
		}
		final String name = bossEnum.getStringValue(bossId);
		return Strings.isEmpty(name) ? CombatAchievementConstants.UNKNOWN_BOSS : name;
	}
}
