package com.clansocket.tracking.progression;

import java.util.Set;

public final class ProgressionConstants
{
	public static final int LOGIN_WARMUP_TICKS = 16;
	public static final int QUEST_WARMUP_TICKS = 8;

	static final String CLUE_PREFIX = "You have completed ";
	static final String CLUE_SUFFIX_PLURAL = " Treasure Trails.";
	static final String CLUE_SUFFIX_SINGULAR = " Treasure Trail.";
	static final int DECIMAL_BASE = 10;

	static final String CLUE_ITEM_PREFIX_CLUE = "Clue scroll (";
	static final String CLUE_ITEM_PREFIX_CHALLENGE = "Challenge scroll (";
	static final String CLUE_ITEM_PREFIX_TREASURE = "Treasure scroll (";
	static final String CLUE_MENU_READ = "Read";

	static final Set<String> CLUE_TIERS = Set.of("beginner", "easy", "medium", "hard", "elite", "master");

	private ProgressionConstants() {
	}
}
