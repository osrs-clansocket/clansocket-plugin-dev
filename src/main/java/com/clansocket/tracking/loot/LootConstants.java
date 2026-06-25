package com.clansocket.tracking.loot;

final class LootConstants
{
	static final String PET_FOLLOWING = "You have a funny feeling like you're being followed";
	static final String PET_SNEAKING = "You feel something weird sneaking into your backpack";
	static final String PET_BAGGED = "You have a funny feeling like you would have been followed";
	static final String UNTRADEABLE_DROP_PREFIX = "Untradeable drop: ";

	static final String TRIGGER_FOLLOWING = "FOLLOWING";
	static final String TRIGGER_SNEAKING = "SNEAKING";
	static final String TRIGGER_BAGGED = "BAGGED";

	static final String LOOT_TYPE_PLAYER = "PLAYER";
	static final String LOOT_TYPE_NPC = "NPC";

	static final String KILL_COUNT_GROUP = "killcount";
	static final String LOOT_TRACKER_GROUP = "loottracker";
	static final String LOOT_DROPS_KEY_PREFIX = "drops_";
	static final String LOOT_TRACKER_KILLS_FIELD = "kills";
	static final String OUR_KC_PREFIX = "ourkc.";

	static final int PET_PENDING_TIMEOUT_TICKS = 3;

	private LootConstants() {
	}
}
