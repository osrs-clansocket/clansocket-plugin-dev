package com.clansocket;

import java.util.Map;

public final class ClanSocketConstants
{
	public static final String PLUGIN_VERSION = "1.0.0";
	public static final int PROTOCOL_VERSION = 1;

	public static final String CONFIG_GROUP = "clansocket";
	public static final String CONFIG_KEY_SERVER_WS_URL = "serverWsUrl";
	public static final String CONFIG_KEY_STREAM_LOCATION = "streamLocation";
	public static final String CONFIG_KEY_MODE = "mode";
	public static final String CONFIG_MODE_CLAN = "CLAN";

	public static final String CONSENT_TITLE_LOCATION = "ClanSocket: enable Location sharing?";
	public static final String CONSENT_BODY_LOCATION = "Enabling Location streams your real-time in-game position (x, y, plane, region, area)\n"
	        + "to your clan dashboard while you are logged in.\n\n"
	        + "Visible to clan members only — not public outside the clan.\n"
	        + "You can turn this off any time in the plugin config.\n\n" + "Enable Location sharing?";

	public static final String CONSENT_TITLE_CLAN_MODE = "ClanSocket: hand config to clan managers?";
	public static final String CONSENT_BODY_CLAN_MODE = "Switching to Clan mode lets your clan managers push ClanSocket settings to your plugin "
	        + "from the clansocket.com dashboard.\n\n"
	        + "Your slot presets stay saved but become read-only while Clan mode is active. "
	        + "Switch back to Manual any time to take control again.\n\n" + "Hand config to clan managers?";

	public static final String SERVER_URL = "wss://ws.clansocket.com/data";

	public static final String WS_FIELD_TYPE = "type";
	public static final String WS_FIELD_PAYLOAD = "payload";
	public static final String WS_TYPE_WELCOME = "welcome";
	public static final String WS_TYPE_CLAN_REMINDER = "clan_reminder";
	public static final String WS_TYPE_REIDENTIFY = "reidentify";
	public static final String WS_TYPE_IDENTITY_OK = "identity_ok";
	public static final String WS_TYPE_RSN_VERIFY_REQUEST = "rsn_verify_request";
	public static final String WS_TYPE_RSN_VERIFY_CANCELLED = "rsn_verify_cancelled";
	public static final String WS_TYPE_RSN_VERIFY_RESPONSE = "rsn_verify_response";
	public static final String WS_TYPE_CLAIM_CONSENT_REQUEST = "claim_consent_request";
	public static final String WS_TYPE_CLAIM_CONSENT_CANCELLED = "claim_consent_cancelled";
	public static final String WS_TYPE_CLAIM_CONSENT_RESPONSE = "claim_consent_response";
	public static final String WS_TYPE_BROADCAST = "broadcast";
	public static final String WS_TYPE_CLAN_CONFIG_PUSH = "clan_config_push";
	public static final String WS_TYPE_CLAN_CONFIG_REQUEST = "clan_config_request";
	public static final String WS_FIELD_SESSION_ID = "sessionId";
	public static final String WS_FIELD_CLAN_NAME = "clanName";
	public static final String WS_FIELD_REASON = "reason";
	public static final String WS_FIELD_REQUEST_ID = "requestId";
	public static final String WS_FIELD_REQUESTING_DISPLAY_NAME = "requestingDisplayName";
	public static final String WS_FIELD_REQUESTED_RSN = "requestedRsn";
	public static final String WS_FIELD_REQUESTED_CLAN_NAME = "requestedClanName";
	public static final String WS_FIELD_EXPIRES_AT = "expiresAt";
	public static final String WS_FIELD_MESSAGE = "message";

	public static final String CONSENT_ACTION_CONFIRM = "confirm";
	public static final String CONSENT_ACTION_REJECT = "reject";

	public static final String REASON_NOT_REGISTERED = "not_registered";
	public static final String REASON_NOT_MEMBER = "not_member";

	public static final long CLAN_REMINDER_INTERVAL_MS = 24L * 60L * 60L * 1000L;
	public static final Map<String, String> CLAN_REMINDER_MSG_BY_REASON = Map.of(REASON_NOT_REGISTERED,
	        "Clan '%s' is not registered on ClanSocket. Owner or deputy must register at clansocket.com to enable telemetry.",
	        REASON_NOT_MEMBER,
	        "ClanSocket cant verify your membership in '%s'. Ask a clan owner to refresh the roster on the dashboard.");

	public static final long RECONNECT_INITIAL_MS = 1000L;
	public static final long RECONNECT_MAX_MS = 15_000L;
	public static final long WS_PING_INTERVAL_SECONDS = 15L;
	public static final int QUEUE_CAP = 2000;
	public static final int QUEUE_DROP_LOG_INTERVAL = 50;
	public static final int PAYLOAD_WARN_BYTES = 800_000;
	public static final int NORMAL_CLOSURE = 1000;

	public static final String BRAND_PREFIX = "<col=ffcc33>[ClanSocket]</col> ";
	public static final String LIVE_TAG = "<col=00ff00>LIVE</col>";
	public static final String OFFLINE_TAG = "<col=ff6600>OFFLINE</col>";

	public static final int SCHEME_DELIM_LEN = 3;
	public static final String SCHEME_DELIM = "://";
	public static final String SCHEME_WSS = "wss://";

	public static final long DISCONNECT_GRACE_MS = 10_000L;

	public static final Map<String, String[]> CONSENT_COPY = Map.of(CONFIG_KEY_STREAM_LOCATION,
	        new String[]{CONSENT_TITLE_LOCATION, CONSENT_BODY_LOCATION}, CONFIG_KEY_MODE,
	        new String[]{CONSENT_TITLE_CLAN_MODE, CONSENT_BODY_CLAN_MODE});

	private ClanSocketConstants() {
	}
}
