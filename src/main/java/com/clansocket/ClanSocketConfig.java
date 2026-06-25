package com.clansocket;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("clansocket")
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessivePublicCount"})
public interface ClanSocketConfig extends Config
{
	enum ConfigMode
	{
		MANUAL, CLAN
	}

	@ConfigSection(name = "Network customization", description = "Override endpoint.", position = 0, closedByDefault = true)
	String CLAN_CUSTOM = "clanCustom";

	@ConfigItem(keyName = "serverWsUrl", name = "WebSocket URL", description = "Blank = clansocket.com default.", section = CLAN_CUSTOM, position = 1)
	default String serverWsUrl()
	{
		return "";
	}

	@ConfigItem(keyName = "mode", name = "Config source", description = "MANUAL = local slots, CLAN = pushed from clansocket.com.", hidden = true)
	default ConfigMode mode()
	{
		return ConfigMode.MANUAL;
	}

	@ConfigItem(keyName = "streamSkillsSnapshot", name = "Skills snapshot", description = "Full table on login + change.", hidden = true)
	default boolean streamSkillsSnapshot()
	{
		return true;
	}

	@ConfigItem(keyName = "streamXpGains", name = "XP gains", description = "Per-skill XP deltas.", hidden = true)
	default boolean streamXpGains()
	{
		return true;
	}

	@ConfigItem(keyName = "streamLevelUps", name = "Level-ups", description = "Per-skill level-up events.", hidden = true)
	default boolean streamLevelUps()
	{
		return true;
	}

	@ConfigItem(keyName = "streamCombat", name = "Combat", description = "Hitsplats, target.", hidden = true)
	default boolean streamCombat()
	{
		return true;
	}

	@ConfigItem(keyName = "streamDeath", name = "Death", description = "Player death events.", hidden = true)
	default boolean streamDeath()
	{
		return true;
	}

	@ConfigItem(keyName = "streamSlayer", name = "Slayer", description = "Task name, count, master.", hidden = true)
	default boolean streamSlayer()
	{
		return true;
	}

	@ConfigItem(keyName = "streamVitals", name = "Vitals", description = "Run energy, weight, special.", hidden = true)
	default boolean streamVitals()
	{
		return true;
	}

	@ConfigItem(keyName = "streamPrayer", name = "Prayer", description = "Active prayers.", hidden = true)
	default boolean streamPrayer()
	{
		return true;
	}

	@ConfigItem(keyName = "streamBoosts", name = "Stat boosts", description = "Boost differences.", hidden = true)
	default boolean streamBoosts()
	{
		return true;
	}

	@ConfigItem(keyName = "streamStatusEffects", name = "Status effects", description = "Poison, venom, disease, cold.", hidden = true)
	default boolean streamStatusEffects()
	{
		return true;
	}

	@ConfigItem(keyName = "streamLocation", name = "Location", description = "Real-time coords visible to clan.", hidden = true)
	default boolean streamLocation()
	{
		return true;
	}

	@ConfigItem(keyName = "streamInventory", name = "Inventory", description = "Inventory, equipment, seed vault.", hidden = true)
	default boolean streamInventory()
	{
		return true;
	}

	@ConfigItem(keyName = "streamBank", name = "Bank", description = "Snapshots while open. Reveals wealth.", hidden = true)
	default boolean streamBank()
	{
		return true;
	}

	@ConfigItem(keyName = "streamRunePouch", name = "Rune pouch", description = "Slot contents.", hidden = true)
	default boolean streamRunePouch()
	{
		return true;
	}

	@ConfigItem(keyName = "streamLoot", name = "Loot", description = "NPC drops + pickups.", hidden = true)
	default boolean streamLoot()
	{
		return true;
	}

	@ConfigItem(keyName = "streamPetDrops", name = "Pet drops", description = "Pet drop events.", hidden = true)
	default boolean streamPetDrops()
	{
		return true;
	}

	@ConfigItem(keyName = "streamQuests", name = "Quests", description = "Snapshots + completions.", hidden = true)
	default boolean streamQuests()
	{
		return true;
	}

	@ConfigItem(keyName = "streamDiaries", name = "Diaries", description = "Diary tasks + completions.", hidden = true)
	default boolean streamDiaries()
	{
		return true;
	}

	@ConfigItem(keyName = "streamClues", name = "Clues", description = "Opens + per-tier completions.", hidden = true)
	default boolean streamClues()
	{
		return true;
	}

	@ConfigItem(keyName = "streamCollectionLog", name = "Collection log", description = "Per-item + full snapshot on log open.", hidden = true)
	default boolean streamCollectionLog()
	{
		return true;
	}

	@ConfigItem(keyName = "streamCombatAchievements", name = "Combat achievements", description = "Catalog, snapshot, completions.", hidden = true)
	default boolean streamCombatAchievements()
	{
		return true;
	}

	@ConfigItem(keyName = "sendClanChat", name = "Clan chat", description = "Configured clan only. Server dedups.", hidden = true)
	default boolean sendClanChat()
	{
		return true;
	}

	@ConfigItem(keyName = "streamMenuActions", name = "Menu actions", description = "Right-click options on objects/NPCs.", hidden = true)
	default boolean streamMenuActions()
	{
		return true;
	}

	@ConfigItem(keyName = "streamFarming", name = "Farming patches", description = "Patch state changes.", hidden = true)
	default boolean streamFarming()
	{
		return true;
	}
}
