package com.clansocket.config;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.RuneScapeProfileChanged;

import com.clansocket.CSConfigResolver;
import com.clansocket.ClanSocket;
import com.clansocket.ClanSocketConfig;
import com.clansocket.ClanSocketConstants;
import com.clansocket.config.preset.PresetApplier;
import com.clansocket.config.preset.PresetCodec;
import com.clansocket.panel.PanelStats;
import com.clansocket.panel.verify.ConsentRouter;

import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class ConfigEventBridge
{
	@Inject
	private ClanSocketConfig config;
	@Inject
	private ClanSocket socket;
	@Inject
	private ConfigManager configManager;
	@Inject
	private PanelStats panelStats;
	@Inject
	private ConsentRouter consentRouter;
	@Inject
	private PresetApplier presetApplier;
	@Inject
	private ScheduledExecutorService executor;

	@Subscribe
	public void onRuneScapeProfileChanged(final RuneScapeProfileChanged event)
	{
		executor.execute(panelStats::loadCounts);
	}

	@Subscribe
	public void onConfigChanged(final ConfigChanged event)
	{
		if (!ClanSocketConstants.CONFIG_GROUP.equals(event.getGroup()))
		{
			return;
		}
		if (presetApplier.isImporting())
		{
			return;
		}
		dispatchAfterGate(event.getKey(), event.getNewValue());
	}

	private void dispatchAfterGate(final String key, final String newValue)
	{
		if (ClanSocketConstants.CONFIG_KEY_MODE.equals(key))
		{
			handleModeChange(newValue);
			return;
		}
		if (ClanSocketConstants.CONFIG_KEY_SERVER_WS_URL.equals(key))
		{
			handleWsUrlChange();
			return;
		}
		if (revertedAsClanLocked(key, newValue))
		{
			return;
		}
		consentRouter.maybePrompt(key, newValue);
	}

	private boolean revertedAsClanLocked(final String key, final String newValue)
	{
		if (config.mode() != ClanSocketConfig.ConfigMode.CLAN || PresetCodec.isDenylisted(key))
		{
			return false;
		}
		final Optional<String> canonical = presetApplier.lastClanValue(key);
		if (canonical.isEmpty() || canonical.get().equals(newValue))
		{
			return false;
		}
		log.info("ClanSocket clan mode: reverting user edit on {}", key);
		configManager.setConfiguration(ClanSocketConstants.CONFIG_GROUP, key, canonical.get());
		return true;
	}

	private void handleModeChange(final String newValue)
	{
		if (ClanSocketConstants.CONFIG_MODE_CLAN.equals(newValue))
		{
			consentRouter.promptClanModeSwitch(this::requestClanConfig);
		}
	}

	private void requestClanConfig()
	{
		socket.send(Collections.singletonMap(ClanSocketConstants.WS_FIELD_TYPE,
		        ClanSocketConstants.WS_TYPE_CLAN_CONFIG_REQUEST));
	}

	@Subscribe
	public void onApplied(final PresetApplier.Applied event)
	{
		if (event.changed(ClanSocketConstants.CONFIG_KEY_SERVER_WS_URL))
		{
			handleWsUrlChange();
		}
	}

	private void handleWsUrlChange()
	{
		final String next = CSConfigResolver.serverWsUrl(config);
		log.info("ClanSocket WS endpoint changed → {}, reconnecting", next);
		socket.disconnect();
		socket.tryConnect(next);
	}
}
