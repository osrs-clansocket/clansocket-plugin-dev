package com.clansocket.chat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.callback.ClientThread;

import com.clansocket.ClanSocketConstants;
import com.clansocket.util.Strings;
import com.clansocket.util.Urls;

@Singleton
@SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
public class GameChatEmitter
{
	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;
	@Inject
	private ScheduledExecutorService executor;

	private ScheduledFuture<?> pendingOfflineTask;
	private boolean offlineAnnounced;
	private final Map<String, Long> reminderLastShown = new HashMap<>();

	public void pluginEnabled()
	{
		announce("Plugin active. Connecting to clan dashboard...");
	}

	public void pluginDisabled()
	{
		synchronized (this)
		{
			cancelPendingOffline();
			offlineAnnounced = false;
		}
		announce("Plugin disabled. Streaming stopped — no data leaving your client.");
	}

	public synchronized void connected(final String url)
	{
		if (cancelPendingOffline())
		{
			return;
		}
		if (!offlineAnnounced)
		{
			return;
		}
		offlineAnnounced = false;
		announce("Connection " + ClanSocketConstants.LIVE_TAG + " — telemetry streaming to " + safeHost(url) + ".");
	}

	public synchronized void disconnected(final String reason)
	{
		if (pendingOfflineTask != null || offlineAnnounced)
		{
			return;
		}
		pendingOfflineTask = executor.schedule(() -> announceOffline(reason), ClanSocketConstants.DISCONNECT_GRACE_MS,
		        TimeUnit.MILLISECONDS);
	}

	public synchronized void clanReminder(final String reason, final String clanName)
	{
		final String fmt = ClanSocketConstants.CLAN_REMINDER_MSG_BY_REASON.get(reason);
		final String key = reason + ":" + clanName;
		final long now = System.currentTimeMillis();
		if (fmt == null || Strings.isEmpty(clanName)
		        || now - reminderLastShown.getOrDefault(key, 0L) < ClanSocketConstants.CLAN_REMINDER_INTERVAL_MS)
		{
			return;
		}
		reminderLastShown.put(key, now);
		announce(String.format(fmt, clanName));
	}

	public void sessionReminder(final String actualClanName, final String actualClanRank, final boolean connected)
	{
		if (Strings.isEmpty(actualClanName))
		{
			announce(
			        "Streaming requires clan membership — you must be in an in-game clan for the dashboard to receive data.");
			return;
		}
		final String tag = connected ? ClanSocketConstants.LIVE_TAG : ClanSocketConstants.OFFLINE_TAG;
		final String suffix = connected ? ". Disable the plugin to disconnect." : ". Attempting to reconnect...";
		final String rankPart = Strings.isEmpty(actualClanRank) ? "" : " as " + actualClanRank;
		announce("Streaming to " + actualClanName + rankPart + " — status: " + tag + suffix);
	}

	public void serverBroadcast(final String message)
	{
		if (Strings.isEmpty(message))
		{
			return;
		}
		clientThread.invokeLater(() ->
		{
			if (client.getGameState() != GameState.LOGGED_IN)
			{
				return;
			}
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
		});
	}

	private synchronized void announceOffline(final String reason)
	{
		pendingOfflineTask = null;
		offlineAnnounced = true;
		final String suffix = Strings.isNotEmpty(reason) ? " (" + reason + ")" : "";
		announce("Connection " + ClanSocketConstants.OFFLINE_TAG + suffix + ". Attempting to reconnect...");
	}

	private synchronized boolean cancelPendingOffline()
	{
		if (pendingOfflineTask == null)
		{
			return false;
		}
		pendingOfflineTask.cancel(false);
		pendingOfflineTask = null;
		return true;
	}

	private void announce(final String text)
	{
		serverBroadcast(ClanSocketConstants.BRAND_PREFIX + text);
	}

	private static String safeHost(final String url)
	{
		return url == null ? "?" : Urls.host(url);
	}
}
