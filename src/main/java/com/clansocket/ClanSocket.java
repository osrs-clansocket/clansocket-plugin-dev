package com.clansocket;

import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.clansocket.bus.PendingQueue;
import com.clansocket.chat.GameChatEmitter;
import com.clansocket.config.preset.PresetApplier;
import com.clansocket.panel.PanelStats;
import com.clansocket.transport.LifecycleListeners;
import com.clansocket.transport.ReconnectScheduler;
import com.clansocket.transport.SessionStore;
import com.clansocket.transport.SocketCallback;
import com.clansocket.transport.WsOpener;
import com.clansocket.transport.consent.ConsentDispatch;
import com.google.gson.Gson;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;

@Slf4j
@Singleton
@SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
public class ClanSocket
{
	@Inject
	private WsOpener opener;
	@Inject
	private GameChatEmitter chatEmitter;
	@Inject
	private SessionStore sessions;
	@Inject
	private PanelStats panelStats;
	@Inject
	private ReconnectScheduler reconnectScheduler;
	@Inject
	private ConsentDispatch consentDispatch;
	@Inject
	private LifecycleListeners listeners;
	@Inject
	private PresetApplier presetApplier;
	@Inject
	private PendingQueue pending;
	@Inject
	private ScheduledExecutorService executor;
	@Inject
	private ClanSocketConfig config;
	@Inject
	private Gson gson;
	@Getter
	private volatile String endpoint;
	private volatile boolean shouldBeConnected;
	private volatile boolean drained;
	private int retryAttempts;
	private WebSocket ws;

	public synchronized boolean isConnected()
	{
		return ws != null;
	}

	public synchronized void connect(final String target)
	{
		this.endpoint = target;
		this.shouldBeConnected = true;
		reconnectScheduler.reset();
		panelStats.markConnecting(target);
		openNow();
	}

	public void tryConnect(final String target)
	{
		try
		{
			connect(target);
		} catch (final IllegalArgumentException ex)
		{
			log.warn("ClanSocket WS connect rejected: {} ({})", target, ex.getMessage());
		}
	}

	public synchronized void disconnect()
	{
		shouldBeConnected = false;
		drained = false;
		reconnectScheduler.cancel();
		if (ws != null)
		{
			ws.close(ClanSocketConstants.NORMAL_CLOSURE, "disconnect");
			ws = null;
		}
		pending.clear();
		panelStats.markDisconnected();
	}

	public void send(final Object payload)
	{
		executor.execute(() -> doSend(payload));
	}

	private void doSend(final Object payload)
	{
		final String json = gson.toJson(payload);
		if (json.length() > ClanSocketConstants.PAYLOAD_WARN_BYTES)
		{
			log.warn("ClanSocket WS payload {} bytes approaching server cap; risk of server-side rejection",
			        json.length());
		}
		final WebSocket local;
		synchronized (this)
		{
			local = ws;
		}
		if (local == null || !drained)
		{
			pending.offer(json);
			return;
		}
		local.send(json);
	}

	public void drainAndArm(final WebSocket newWs)
	{
		executor.execute(() ->
		{
			String msg;
			while ((msg = pending.poll()) != null)
			{
				newWs.send(msg);
			}
			drained = true;
		});
	}

	public synchronized void resetBackoff()
	{
		reconnectScheduler.reset();
		retryAttempts = 0;
	}

	public synchronized void kickReconnect()
	{
		if (!shouldBeConnected || ws != null)
		{
			return;
		}
		reconnectScheduler.cancel();
		reconnectScheduler.reset();
		openNow();
	}

	public synchronized void handleClose(final String reason)
	{
		ws = null;
		drained = false;
		if (!shouldBeConnected)
		{
			return;
		}
		chatEmitter.disconnected(reason);
		panelStats.markReconnectAttempt(retryAttempts++);
		reconnectScheduler.schedule(this::openNow);
	}

	private synchronized void openNow()
	{
		if (!shouldBeConnected || endpoint == null)
		{
			return;
		}
		ws = opener.open(endpoint, new SocketCallback(this, chatEmitter, sessions, panelStats, consentDispatch,
		        listeners, presetApplier, config, gson));
	}
}
