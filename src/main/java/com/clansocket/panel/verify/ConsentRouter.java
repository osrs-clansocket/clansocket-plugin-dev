package com.clansocket.panel.verify;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import net.runelite.client.config.ConfigManager;

import com.clansocket.ClanSocketConfig;
import com.clansocket.ClanSocketConstants;
import com.clansocket.config.preset.PresetApplier;
import com.clansocket.panel.ClanSocketPanel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public final class ConsentRouter
{
	@Inject
	private ConfigManager configMgr;
	@Inject
	private Provider<ClanSocketPanel> panelProvider;
	@Inject
	private PresetApplier presetApplier;

	public void maybePrompt(final String key, final String newValue)
	{
		final String[] copy = Boolean.parseBoolean(newValue) ? ClanSocketConstants.CONSENT_COPY.get(key) : null;
		if (copy == null)
		{
			return;
		}
		ConsentDialog.confirm(panelProvider.get(), copy[0], copy[1], accepted ->
		{
			if (!accepted)
			{
				log.info("ClanSocket consent declined for {}, reverting to off", key);
				configMgr.setConfiguration(ClanSocketConstants.CONFIG_GROUP, key, false);
			}
		});
	}

	public void promptClanModeSwitch(final Runnable onAccept)
	{
		final String[] copy = ClanSocketConstants.CONSENT_COPY.get(ClanSocketConstants.CONFIG_KEY_MODE);
		ConsentDialog.confirm(panelProvider.get(), copy[0], copy[1], accepted ->
		{
			if (accepted)
			{
				presetApplier.seedClanCanonicalFromCurrent();
				onAccept.run();
				return;
			}
			log.info("ClanSocket consent declined for clan mode, reverting to manual");
			configMgr.setConfiguration(ClanSocketConstants.CONFIG_GROUP, ClanSocketConstants.CONFIG_KEY_MODE,
			        ClanSocketConfig.ConfigMode.MANUAL);
		});
	}
}
