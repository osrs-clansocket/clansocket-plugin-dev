package com.clansocket.config.preset;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;

import com.clansocket.ClanSocketConstants;
import com.google.gson.JsonElement;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public final class PresetApplier
{
	public enum Source
	{
		LOCAL, CLAN
	}

	@Value
	public static class Applied
	{
		Set<String> changedKeys;

		public boolean changed(final String key)
		{
			return changedKeys.contains(key);
		}
	}

	private final ConfigManager configManager;
	private final EventBus eventBus;
	private final AtomicBoolean importing = new AtomicBoolean(false);
	private volatile Map<String, JsonElement> lastAppliedClanValues = Map.of();

	@Inject
	public PresetApplier(final ConfigManager configManager, final EventBus eventBus) {
		this.configManager = configManager;
		this.eventBus = eventBus;
	}

	public boolean isImporting()
	{
		return importing.get();
	}

	public Optional<String> lastClanValue(final String key)
	{
		final JsonElement el = lastAppliedClanValues.get(key);
		return el == null ? Optional.empty() : Optional.of(el.getAsString());
	}

	public void seedClanCanonicalFromCurrent()
	{
		this.lastAppliedClanValues = new HashMap<>(PresetCodec.snapshot(configManager).getValues());
	}

	public void apply(final PresetSchema schema, final Source source)
	{
		final Set<String> changed = new HashSet<>();
		importing.set(true);
		try
		{
			for (final Map.Entry<String, JsonElement> e : schema.getValues().entrySet())
			{
				applyOne(e.getKey(), e.getValue().getAsString(), changed);
			}
		} finally
		{
			importing.set(false);
		}
		if (source == Source.CLAN)
		{
			this.lastAppliedClanValues = new HashMap<>(schema.getValues());
		}
		eventBus.post(new Applied(changed));
	}

	private void applyOne(final String key, final String newValue, final Set<String> changed)
	{
		if (PresetCodec.isDenylisted(key))
		{
			log.debug("{} {}", PresetConstants.LOG_SKIPPED_DENYLISTED, key);
			return;
		}
		final String old = configManager.getConfiguration(ClanSocketConstants.CONFIG_GROUP, key);
		if (newValue.equals(old))
		{
			return;
		}
		configManager.setConfiguration(ClanSocketConstants.CONFIG_GROUP, key, newValue);
		changed.add(key);
	}
}
