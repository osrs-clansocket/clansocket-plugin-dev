package com.clansocket;

import javax.inject.Inject;

import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import com.clansocket.chat.GameChatEmitter;
import com.clansocket.config.ConfigEventBridge;
import com.clansocket.panel.PanelLifecycle;
import com.clansocket.protocol.common.Payload;
import com.clansocket.transport.LifecycleListeners;
import com.google.inject.Provides;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@PluginDescriptor(name = "ClanSocket", description = "Streams realtime clan + gameplay telemetry over WebSocket.", tags = {
        "clan", "telemetry", "tracker", "dashboard", "websocket", "chat"})
public class ClanSocketPlugin extends Plugin
{
	@Inject
	private ClanSocketConfig config;
	@Inject
	private ClanSocket socket;
	@Inject
	private CSTrackerRegistry trackers;
	@Inject
	private GameChatEmitter chatEmitter;
	@Inject
	private EventBus eventBus;
	@Inject
	private ClientThread clientThread;
	@Inject
	private PanelLifecycle panelLifecycle;
	@Inject
	private ConfigEventBridge configEventBridge;
	@Inject
	private LifecycleListeners listeners;

	@Override
	protected void startUp()
	{
		log.info("ClanSocket started");
		trackers.getAll().forEach(eventBus::register);
		eventBus.register(configEventBridge);
		listeners.setOnOpenListener(() ->
		{
			socket.send(new Payload("hello", "protocolVersion", ClanSocketConstants.PROTOCOL_VERSION, "token", null));
			clientThread.invokeLater(trackers::broadcastResetForReconnect);
		});
		listeners.setOnReidentifyListener(() -> clientThread.invokeLater(trackers::broadcastResetForReconnect));
		panelLifecycle.install();
		chatEmitter.pluginEnabled();
		final String url = CSConfigResolver.serverWsUrl(config);
		log.info("ClanSocket WS attempting connection to: '{}'", url);
		socket.tryConnect(url);
	}

	@Override
	protected void shutDown()
	{
		chatEmitter.pluginDisabled();
		trackers.getAll().forEach(eventBus::unregister);
		eventBus.unregister(configEventBridge);
		socket.disconnect();
		listeners.setOnOpenListener(null);
		panelLifecycle.shutdown();
		log.info("ClanSocket stopped");
	}

	@Subscribe
	public void onGameStateChanged(final GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			socket.kickReconnect();
		}
	}

	@Provides
	ClanSocketConfig provideConfig(final ConfigManager mgr)
	{
		return mgr.getConfig(ClanSocketConfig.class);
	}
}
