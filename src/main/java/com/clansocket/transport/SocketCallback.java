package com.clansocket.transport;

import com.clansocket.ClanSocket;
import com.clansocket.ClanSocketConfig;
import com.clansocket.ClanSocketConstants;
import com.clansocket.chat.GameChatEmitter;
import com.clansocket.config.preset.PresetApplier;
import com.clansocket.config.preset.PresetSchema;
import com.clansocket.panel.PanelStats;
import com.clansocket.transport.consent.ConsentDispatch;
import com.clansocket.util.Json;
import com.clansocket.util.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

@Slf4j
public final class SocketCallback extends WebSocketListener
{
	private final ClanSocket socket;
	private final GameChatEmitter chatEmitter;
	private final SessionStore sessions;
	private final PanelStats panelStats;
	private final ConsentDispatch consentDispatch;
	private final LifecycleListeners listeners;
	private final PresetApplier presetApplier;
	private final ClanSocketConfig config;
	private final Gson gson;

	@SuppressWarnings({"checkstyle:ParameterNumber", "PMD.ExcessiveParameterList"})
	public SocketCallback(final ClanSocket socket, final GameChatEmitter chatEmitter, final SessionStore sessions,
	        final PanelStats panelStats, final ConsentDispatch consentDispatch, final LifecycleListeners listeners,
	        final PresetApplier presetApplier, final ClanSocketConfig config, final Gson gson) {
		this.socket = socket;
		this.chatEmitter = chatEmitter;
		this.sessions = sessions;
		this.panelStats = panelStats;
		this.consentDispatch = consentDispatch;
		this.listeners = listeners;
		this.presetApplier = presetApplier;
		this.config = config;
		this.gson = gson;
	}

	@Override
	public void onOpen(final WebSocket webSocket, final Response response)
	{
		log.info("ClanSocket WS connected to {}", socket.getEndpoint());
		socket.resetBackoff();
		chatEmitter.connected(socket.getEndpoint());
		panelStats.markConnected();
		socket.drainAndArm(webSocket);
		listeners.fireOnOpen();
	}

	@Override
	@SuppressWarnings("checkstyle:IllegalCatch")
	public void onMessage(final WebSocket webSocket, final String text)
	{
		log.debug("ClanSocket WS message: {}", text);
		try
		{
			final JsonObject obj = gson.fromJson(text, JsonObject.class);
			if (!obj.has(ClanSocketConstants.WS_FIELD_TYPE))
			{
				return;
			}
			dispatch(obj);
		} catch (final RuntimeException e)
		{
			log.warn("ClanSocket WS message parse failed: {}", e.getMessage());
		}
	}

	private void dispatch(final JsonObject obj)
	{
		final String type = obj.get(ClanSocketConstants.WS_FIELD_TYPE).getAsString();
		if (consentDispatch.tryHandle(type, obj))
		{
			return;
		}
		if (ClanSocketConstants.WS_TYPE_WELCOME.equals(type) && obj.has(ClanSocketConstants.WS_FIELD_SESSION_ID))
		{
			handleWelcome(obj);
		} else if (ClanSocketConstants.WS_TYPE_CLAN_REMINDER.equals(type))
		{
			handleClanReminder(obj);
		} else if (ClanSocketConstants.WS_TYPE_BROADCAST.equals(type))
		{
			chatEmitter.serverBroadcast(Json.optString(obj, ClanSocketConstants.WS_FIELD_MESSAGE));
		} else if (ClanSocketConstants.WS_TYPE_CLAN_CONFIG_PUSH.equals(type))
		{
			if (config.mode() != ClanSocketConfig.ConfigMode.CLAN)
			{
				return;
			}
			presetApplier.apply(gson.fromJson(obj.get(ClanSocketConstants.WS_FIELD_PAYLOAD), PresetSchema.class),
			        PresetApplier.Source.CLAN);
		} else
		{
			handleControlEvent(type);
		}
	}

	private void handleControlEvent(final String type)
	{
		if (ClanSocketConstants.WS_TYPE_IDENTITY_OK.equals(type))
		{
			panelStats.setClanReason(null);
		} else if (ClanSocketConstants.WS_TYPE_REIDENTIFY.equals(type))
		{
			listeners.fireOnReidentify();
		}
	}

	private void handleWelcome(final JsonObject obj)
	{
		sessions.setSessionId(obj.get(ClanSocketConstants.WS_FIELD_SESSION_ID).getAsString());
		panelStats.setClanReason(null);
	}

	private void handleClanReminder(final JsonObject obj)
	{
		final String reason = Json.optString(obj, ClanSocketConstants.WS_FIELD_REASON);
		panelStats.setClanReason(reason);
		chatEmitter.clanReminder(reason, Json.optString(obj, ClanSocketConstants.WS_FIELD_CLAN_NAME));
	}

	@Override
	public void onClosed(final WebSocket webSocket, final int code, final String reason)
	{
		log.info("ClanSocket WS closed ({}) {}", code, reason);
		sessions.clear();
		socket.handleClose(Strings.isNotEmpty(reason) ? reason : "code " + code);
	}

	@Override
	public void onFailure(final WebSocket webSocket, final Throwable t, final Response response)
	{
		log.warn("ClanSocket WS failure: {}", t.getMessage());
		sessions.clear();
		socket.handleClose(t.getMessage());
	}
}
