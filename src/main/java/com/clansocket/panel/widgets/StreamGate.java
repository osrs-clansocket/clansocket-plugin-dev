package com.clansocket.panel.widgets;

import java.util.List;
import java.util.function.Function;

import net.runelite.api.gameval.SpriteID;

import com.clansocket.ClanSocketConfig;

@SuppressWarnings("checkstyle:StaticFinalOutsideConstants")
public final class StreamGate
{
	private static int NEXT_ORDINAL;

	public static final StreamGate SKILLS_SNAPSHOT = new StreamGate("Skills snapshot",
	        ClanSocketConfig::streamSkillsSnapshot, "streamSkillsSnapshot", SpriteID.SideIcons.STATS);
	public static final StreamGate XP_GAINS = new StreamGate("XP gains", ClanSocketConfig::streamXpGains,
	        "streamXpGains", SpriteID.OrbXp.ORB);
	public static final StreamGate LEVEL_UPS = new StreamGate("Level-ups", ClanSocketConfig::streamLevelUps,
	        "streamLevelUps", SpriteID.Staticons2.TOTAL);
	public static final StreamGate COMBAT = new StreamGate("Combat", ClanSocketConfig::streamCombat, "streamCombat",
	        SpriteID.SideIcons.COMBAT);
	public static final StreamGate DEATH = new StreamGate("Death", ClanSocketConfig::streamDeath, "streamDeath",
	        SpriteID.ICON_SKULL);
	public static final StreamGate SLAYER = new StreamGate("Slayer", ClanSocketConfig::streamSlayer, "streamSlayer",
	        SpriteID.Staticons2.SLAYER);
	public static final StreamGate VITALS = new StreamGate("Vitals", ClanSocketConfig::streamVitals, "streamVitals",
	        SpriteID.OrbIcon.HITPOINTS);
	public static final StreamGate PRAYER = new StreamGate("Prayer", ClanSocketConfig::streamPrayer, "streamPrayer",
	        SpriteID.OrbIcon.PRAYER);
	public static final StreamGate BOOSTS = new StreamGate("Stat boosts", ClanSocketConfig::streamBoosts,
	        "streamBoosts", SpriteID.Staticons2.UNKNOWN_EMPTY_VIAL);
	public static final StreamGate STATUS_EFFECTS = new StreamGate("Status effects",
	        ClanSocketConfig::streamStatusEffects, "streamStatusEffects", SpriteID.IconBuffbar32x32._0);
	public static final StreamGate LOCATION = new StreamGate("Location", ClanSocketConfig::streamLocation,
	        "streamLocation", SpriteID.WorldmapIcon.PLANET);
	public static final StreamGate INVENTORY = new StreamGate("Inventory", ClanSocketConfig::streamInventory,
	        "streamInventory", SpriteID.SideIcons.INVENTORY);
	public static final StreamGate BANK = new StreamGate("Bank", ClanSocketConfig::streamBank, "streamBank",
	        SpriteID.Mapfunction.BANK);
	public static final StreamGate RUNE_POUCH = new StreamGate("Rune pouch", ClanSocketConfig::streamRunePouch,
	        "streamRunePouch", SpriteID.IconRune32x32._0);
	public static final StreamGate LOOT = new StreamGate("Loot", ClanSocketConfig::streamLoot, "streamLoot",
	        SpriteID.IconTrackerLoot01_30x30._0);
	public static final StreamGate PET_DROPS = new StreamGate("Pet drops", ClanSocketConfig::streamPetDrops,
	        "streamPetDrops", SpriteID.OptionsIcons.FINGER_POINTING_AT_PET);
	public static final StreamGate QUESTS = new StreamGate("Quests", ClanSocketConfig::streamQuests, "streamQuests",
	        SpriteID.SideIcons.QUEST);
	public static final StreamGate DIARIES = new StreamGate("Diaries", ClanSocketConfig::streamDiaries, "streamDiaries",
	        SpriteID.SideIcons.ACHIEVEMENT_DIARIES);
	public static final StreamGate CLUES = new StreamGate("Clues", ClanSocketConfig::streamClues, "streamClues",
	        SpriteID.OptionsIcons.SCROLL);
	public static final StreamGate COLLECTION_LOG = new StreamGate("Collection log",
	        ClanSocketConfig::streamCollectionLog, "streamCollectionLog",
	        SpriteID.SideiconsInterface.CHARACTER_SUMMARY);
	public static final StreamGate COMBAT_ACHIEVEMENTS = new StreamGate("Combat achievements",
	        ClanSocketConfig::streamCombatAchievements, "streamCombatAchievements", SpriteID.CaTierSwordsSmall._0);
	public static final StreamGate CLAN_CHAT = new StreamGate("Clan chat", ClanSocketConfig::sendClanChat,
	        "sendClanChat", SpriteID.SideiconsInterface.CLAN);
	public static final StreamGate MENU_ACTIONS = new StreamGate("Menu actions", ClanSocketConfig::streamMenuActions,
	        "streamMenuActions", SpriteID.OptionsIcons.FINGER_POINTING_AT_MENU);
	public static final StreamGate FARMING = new StreamGate("Farming patches", ClanSocketConfig::streamFarming,
	        "streamFarming", SpriteID.Staticons2.FARMING);

	public static final List<StreamGate> ALL = List.of(SKILLS_SNAPSHOT, XP_GAINS, LEVEL_UPS, COMBAT, DEATH, SLAYER,
	        VITALS, PRAYER, BOOSTS, STATUS_EFFECTS, LOCATION, INVENTORY, BANK, RUNE_POUCH, LOOT, PET_DROPS, QUESTS,
	        DIARIES, CLUES, COLLECTION_LOG, COMBAT_ACHIEVEMENTS, CLAN_CHAT, MENU_ACTIONS, FARMING);

	private final String displayName;
	private final Function<ClanSocketConfig, Boolean> getter;
	private final String configKey;
	private final int spriteId;
	private final int ordinal;

	private StreamGate(final String displayName, final Function<ClanSocketConfig, Boolean> getter,
	        final String configKey, final int spriteId) {
		this.displayName = displayName;
		this.getter = getter;
		this.configKey = configKey;
		this.spriteId = spriteId;
		this.ordinal = NEXT_ORDINAL++;
	}

	public String displayName()
	{
		return displayName;
	}

	public String configKey()
	{
		return configKey;
	}

	public int spriteId()
	{
		return spriteId;
	}

	public int ordinal()
	{
		return ordinal;
	}

	public boolean isEnabled(final ClanSocketConfig config)
	{
		return Boolean.TRUE.equals(getter.apply(config));
	}
}
