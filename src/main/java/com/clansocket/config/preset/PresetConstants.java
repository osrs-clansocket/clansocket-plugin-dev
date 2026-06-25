package com.clansocket.config.preset;

import java.io.File;

import net.runelite.client.RuneLite;

public final class PresetConstants
{
	public static final String CLANSOCKET_DIR_NAME = "clansocket";
	public static final String PRESETS_DIR_NAME = "presets";
	public static final File PRESETS_DIR = new File(new File(RuneLite.RUNELITE_DIR, CLANSOCKET_DIR_NAME),
	        PRESETS_DIR_NAME);

	public static final int MAX_SLOTS = 7;

	public static final String FILENAME_PATTERN = "preset-%d.json";

	public static final int SCHEMA_VERSION = 1;

	public static final String LABEL_PRESET_FORMAT = "Preset %d";
	public static final String LABEL_PRESET_EMPTY_FORMAT = "Preset %d (empty)";

	public static final String LOG_SLOTS_FULL = "ClanSocket preset: all slots full — delete one first";
	public static final String LOG_DELETE_EMPTY = "ClanSocket preset: slot already empty";
	public static final String LOG_BAD_JSON = "ClanSocket preset: invalid JSON";
	public static final String LOG_BAD_VERSION = "ClanSocket preset: unsupported schema version";
	public static final String LOG_SKIPPED_DENYLISTED = "ClanSocket preset: skipped denylisted key";
	public static final String LOG_HANDLER_FAILED = "ClanSocket preset: handler failed";
	public static final String LOG_SAVE_BLOCKED_CLAN_MODE = "ClanSocket preset: save blocked while in CLAN mode";

	public static final String EXCLUDED_PREFIX_RSPROFILE = "$";
	public static final String EXCLUDED_PREFIX_SNAP_HASH = "snap_hash.";
	public static final String EXCLUDED_PREFIX_WIKIAV = "wikiav.";

	private PresetConstants() {
	}
}
