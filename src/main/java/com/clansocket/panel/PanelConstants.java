package com.clansocket.panel;

import java.awt.Color;

import net.runelite.client.ui.ColorScheme;

import com.clansocket.ClanSocketConstants;

public final class PanelConstants
{
	public static final long REFRESH_INTERVAL_MS = 1000L;
	public static final long COUNT_FLUSH_INTERVAL_MS = 30_000L;
	public static final long MS_PER_SECOND = 1000L;
	public static final long UNINITIALIZED_SECOND = -1L;
	public static final int HEADER_PADDING_Y = 8;
	public static final int HEADER_PADDING_X = 10;
	public static final int FOOTER_PADDING_Y = 6;
	public static final int FOOTER_PADDING_X = 10;
	public static final int ROW_PADDING_Y = 2;
	public static final int ROW_PADDING_X = 8;
	public static final int ROW_COUNT_GAP = 6;
	public static final int HEADER_DOT_DIAMETER = 10;
	public static final int ICON_GAP = 8;
	public static final int FOOTER_LINK_GAP = 2;
	public static final int SEPARATOR_HEIGHT = 1;
	public static final String ICON_RESOURCE = "icon.png";
	public static final String NAV_TOOLTIP = "ClanSocket";
	public static final String TITLE_TEXT = "ClanSocket";
	public static final String SUBTITLE_DISCONNECTED = "disconnected";
	public static final String FOOTER_WEB_LABEL = "clansocket.com";
	public static final String FOOTER_WEB_URL = "https://clansocket.com";
	public static final String FOOTER_GITHUB_LABEL = "github.com/osrs-clansocket/clansocket-plugin";
	public static final String FOOTER_GITHUB_URL = "https://github.com/osrs-clansocket/clansocket-plugin";
	public static final String VERSION_LABEL = "v" + ClanSocketConstants.PLUGIN_VERSION;
	public static final int NAV_PRIORITY = 5;
	public static final String RESET_LABEL = "reset";
	public static final String RESET_TOOLTIP = "reset stream counters for this account";
	public static final String RESET_CONFIRM_TITLE = "ClanSocket";
	public static final String RESET_CONFIRM_MESSAGE = "reset stream counters for this account?";
	public static final int RESET_BUTTON_RIGHT_GAP = 6;
	public static final String CLAN_STATUS_FMT = "clan: %s";
	public static final String CLAN_STATUS_UNCLAIMED_FMT = "clan: %s (unclaimed)";
	public static final String CLAN_STATUS_UNVERIFIED_FMT = "clan: %s (unverified)";
	public static final String CLAN_STATUS_NONE = "Not a clan member";
	public static final String CLAN_STATUS_NOT_LOGGED_IN = "Not logged in";
	public static final String CONN_STATUS_ONLINE = "Online";
	public static final String CONN_STATUS_OFFLINE = "Offline";
	public static final String CONN_STATUS_RECONNECT = "Reconnect";

	public static final String PRESETS_BTN_SAVE = "Save";
	public static final String PRESETS_BTN_MANUAL = "Manual";
	public static final String PRESETS_BTN_CLAN = "Clan";
	public static final String PRESETS_ACTIVE_SLOT_KEY = "presetActiveSlot";
	public static final int PRESETS_BUTTON_GAP = 4;

	public static final int CARD_PADDING_X = 6;
	public static final int CARD_PADDING_Y = 2;
	public static final int CARD_GRID_GAP = 3;
	public static final int CARD_GRID_PADDING = 4;
	public static final int CARD_ICON_SIZE = 16;
	public static final int CARD_LABEL_PAD = 2;

	public static final int SPARK_W = 60;
	public static final int SPARK_H = 16;
	public static final int TELEMETRY_CARD_HEIGHT = 26;
	public static final String SENT_FORMAT = "Sent: %d";
	public static final String AGO_NEVER = "never";
	public static final long NEVER_EVENT_AT_MS = 0L;
	public static final String LAST_FORMAT = "Last: %s";
	public static final long RECENT_THRESHOLD_MS = 300_000L;
	public static final long SECONDS_PER_MINUTE = 60L;
	public static final long MINUTES_PER_HOUR = 60L;
	public static final long HOURS_PER_DAY = 24L;
	public static final String STATUS_GLYPH_DISABLED = "✗";

	public static final int PRESET_NUM_BUTTON_GAP = 2;
	public static final int SLOT_BUTTON_HEIGHT = 20;

	public static final int SECTION_HEADER_PADDING_Y = 4;
	public static final int SECTION_HEADER_GAP = 6;
	public static final String SECTION_ICON_PATH_FMT = "/icons/section/%s.png";
	public static final String CHEVRON_EXPANDED = "▾";
	public static final String CHEVRON_COLLAPSED = "▸";

	public static final Color COLOR_AUTH_VALID = ColorScheme.PROGRESS_COMPLETE_COLOR;
	public static final Color COLOR_AUTH_NOT_SET = ColorScheme.PROGRESS_ERROR_COLOR;
	public static final Color COLOR_STATE_WARN = ColorScheme.BRAND_ORANGE;
	public static final Color COLOR_GOLD_TEXT = new Color(0xE0, 0xC9, 0x6E);
	public static final Color COLOR_GOLD_FILL = new Color(0xC9, 0xA8, 0x4C);
	public static final Color COLOR_GOLD_BORDER = new Color(0xC9, 0xA8, 0x4C, 0xBF);
	public static final Color COLOR_GOLD_TOGGLE = new Color(0xC9, 0xA8, 0x4C, 0x80);

	private PanelConstants() {
	}
}
