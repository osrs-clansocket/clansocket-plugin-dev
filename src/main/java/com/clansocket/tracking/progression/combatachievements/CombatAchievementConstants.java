package com.clansocket.tracking.progression.combatachievements;

import java.util.Map;

import net.runelite.api.gameval.VarPlayerID;

public final class CombatAchievementConstants
{
	public static final int CA_WARMUP_TICKS = 10;
	public static final int CA_MAX_PRIME_RETRIES = 30;

	public static final int[] TIER_ENUM_IDS = {3981, 3982, 3983, 3984, 3985, 3986};
	public static final String[] TIER_NAMES = {"EASY", "MEDIUM", "HARD", "ELITE", "MASTER", "GRANDMASTER"};
	public static final int[] TIER_POINTS = {1, 2, 3, 4, 5, 6};

	public static final int BOSS_NAMES_ENUM_ID = 3971;

	public static final int STRUCT_PARAM_NAME = 1308;
	public static final int STRUCT_PARAM_DESC = 1309;
	public static final int STRUCT_PARAM_TASK_ID = 1306;
	public static final int STRUCT_PARAM_TYPE_ID = 1311;
	public static final int STRUCT_PARAM_BOSS_ID = 1312;

	public static final Map<Integer, String> TYPE_NAMES = Map.of(1, "STAMINA", 2, "PERFECTION", 3, "KILL_COUNT", 4,
	        "MECHANICAL", 5, "RESTRICTION", 6, "SPEED");

	public static final int[] COMPLETION_VARP_IDS = {VarPlayerID.CA_TASK_COMPLETED_0, VarPlayerID.CA_TASK_COMPLETED_1,
	        VarPlayerID.CA_TASK_COMPLETED_2, VarPlayerID.CA_TASK_COMPLETED_3, VarPlayerID.CA_TASK_COMPLETED_4,
	        VarPlayerID.CA_TASK_COMPLETED_5, VarPlayerID.CA_TASK_COMPLETED_6, VarPlayerID.CA_TASK_COMPLETED_7,
	        VarPlayerID.CA_TASK_COMPLETED_8, VarPlayerID.CA_TASK_COMPLETED_9, VarPlayerID.CA_TASK_COMPLETED_10,
	        VarPlayerID.CA_TASK_COMPLETED_11, VarPlayerID.CA_TASK_COMPLETED_12, VarPlayerID.CA_TASK_COMPLETED_13,
	        VarPlayerID.CA_TASK_COMPLETED_14, VarPlayerID.CA_TASK_COMPLETED_15, VarPlayerID.CA_TASK_COMPLETED_16,
	        VarPlayerID.CA_TASK_COMPLETED_17, VarPlayerID.CA_TASK_COMPLETED_18, VarPlayerID.CA_TASK_COMPLETED_19};

	public static final int BITS_PER_VARP = 32;

	public static final String CHAT_COMPLETION_PREFIX = "Congratulations, you've completed";
	public static final String CHAT_COMPLETION_TASK_TOKEN = "combat task";

	public static final String UNKNOWN_BOSS = "UNKNOWN";
	public static final String UNKNOWN_TYPE = "UNKNOWN";

	private CombatAchievementConstants() {
	}
}
