package com.clansocket.tracking.combat;

import java.util.Map;
import java.util.Set;

import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;

final class CombatConstants
{
	static final int INTERACT_DEDUP_PLAYER = -2;
	static final int SLAYER_TASK_ID_BOSSES = 98;
	static final int RESPAWN_STABLE_TICKS = 3;
	static final int RESPAWN_TIMEOUT_TICKS = 30;
	static final String INTERACT_KIND_NPC = "NPC";
	static final String INTERACT_KIND_PLAYER = "PLAYER";
	static final String INTERACT_KIND_UNKNOWN = "UNKNOWN";
	static final String CAUSE_CATEGORY_PVP = "pvp";
	static final String CAUSE_CATEGORY_PVM = "pvm";
	static final String CAUSE_CATEGORY_INSTANCE = "instance";
	static final String CAUSE_CATEGORY_ENVIRONMENT = "environment";

	static final Set<Integer> WATCHED_VARPS = Set.of(VarPlayerID.SLAYER_COUNT, VarPlayerID.SLAYER_TARGET,
	        VarPlayerID.SLAYER_AREA, VarPlayerID.SLAYER_COUNT_ORIGINAL);

	static final Set<Integer> WATCHED_VARBITS = Set.of(VarbitID.SLAYER_MASTER, VarbitID.SLAYER_POINTS,
	        VarbitID.SLAYER_TASKS_COMPLETED, VarbitID.SLAYER_TARGET_BOSSID, VarbitID.SLAYER_WILDERNESS_TASKS_COMPLETED);

	static final Map<Integer, String> SLAYER_MASTERS = Map.of(1, "Turael", 2, "Mazchna", 3, "Vannaka", 4, "Chaeldar", 5,
	        "Nieve", 6, "Duradel", 7, "Krystilia", 8, "Konar");

	static final String STYLE_MELEE = "MELEE";
	static final String STYLE_RANGED = "RANGED";
	static final String STYLE_MAGIC = "MAGIC";
	static final String STYLE_UNKNOWN = "UNKNOWN";
	static final String STYLE_NAME_CASTING = "Casting";
	static final String STYLE_NAME_DEFENSIVE_CASTING = "Defensive Casting";
	static final String STYLE_NAME_RANGING = "Ranging";
	static final String STYLE_NAME_LONGRANGE = "Longrange";
	static final int STYLE_CAST_SLOT = 4;
	static final int STYLE_NO_RESULT = -1;

	private CombatConstants() {
	}
}
