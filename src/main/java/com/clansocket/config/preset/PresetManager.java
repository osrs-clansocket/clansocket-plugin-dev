package com.clansocket.config.preset;

import java.io.IOException;
import java.util.OptionalInt;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.client.config.ConfigManager;

import com.clansocket.ClanSocketConfig;
import com.clansocket.ClanSocketConstants;
import com.clansocket.util.NumberInput;

import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public final class PresetManager
{
	private final ClanSocketConfig config;
	private final ConfigManager configManager;
	private final PresetStore store;
	private final PresetApplier applier;
	private final PresetCodec codec;

	@Inject
	@SuppressWarnings({"checkstyle:ParameterNumber", "PMD.ExcessiveParameterList"})
	public PresetManager(final ClanSocketConfig config, final ConfigManager configManager, final PresetStore store,
	        final PresetApplier applier, final PresetCodec codec) {
		this.config = config;
		this.configManager = configManager;
		this.store = store;
		this.applier = applier;
		this.codec = codec;
	}

	public boolean useSlot(final int slot)
	{
		if (slotExists(slot))
		{
			try
			{
				applySlot(slot);
			} catch (final IOException | IllegalArgumentException ex)
			{
				log.warn("ClanSocket preset: load slot {} failed: {}", slot, ex.getMessage());
				return false;
			}
		} else if (!saveToSlot(slot))
		{
			return false;
		}
		setActiveSlot(slot);
		return true;
	}

	public void applySlot(final int slot) throws IOException
	{
		store.read(slot).ifPresent(s -> applier.apply(s, PresetApplier.Source.LOCAL));
	}

	public OptionalInt saveCurrent()
	{
		if (config.mode() == ClanSocketConfig.ConfigMode.CLAN)
		{
			log.info(PresetConstants.LOG_SAVE_BLOCKED_CLAN_MODE);
			return OptionalInt.empty();
		}
		final int active = getActiveSlot();
		if (active >= 1 && active <= PresetConstants.MAX_SLOTS)
		{
			return saveToSlot(active) ? OptionalInt.of(active) : OptionalInt.empty();
		}
		return persistAsNewPreset(PresetCodec.snapshot(configManager));
	}

	public boolean saveToSlot(final int slot)
	{
		if (config.mode() == ClanSocketConfig.ConfigMode.CLAN)
		{
			log.info(PresetConstants.LOG_SAVE_BLOCKED_CLAN_MODE);
			return false;
		}
		try
		{
			store.write(slot, PresetCodec.snapshot(configManager));
			return true;
		} catch (final IOException ex)
		{
			log.warn(PresetConstants.LOG_HANDLER_FAILED, ex);
			return false;
		}
	}

	public boolean delete(final int slot)
	{
		if (!store.existsForSlot(slot))
		{
			log.info(PresetConstants.LOG_DELETE_EMPTY);
			return false;
		}
		return store.delete(slot);
	}

	public boolean slotExists(final int slot)
	{
		return store.existsForSlot(slot);
	}

	public int getActiveSlot()
	{
		return NumberInput.parseIntOr(configManager.getConfiguration(ClanSocketConstants.CONFIG_GROUP,
		        com.clansocket.panel.PanelConstants.PRESETS_ACTIVE_SLOT_KEY), -1);
	}

	public void setActiveSlot(final int slot)
	{
		configManager.setConfiguration(ClanSocketConstants.CONFIG_GROUP,
		        com.clansocket.panel.PanelConstants.PRESETS_ACTIVE_SLOT_KEY, Integer.toString(slot));
	}

	public String labelFor(final int slot)
	{
		return store.existsForSlot(slot)
		        ? String.format(PresetConstants.LABEL_PRESET_FORMAT, slot)
		        : String.format(PresetConstants.LABEL_PRESET_EMPTY_FORMAT, slot);
	}

	private OptionalInt persistAsNewPreset(final PresetSchema schema)
	{
		try
		{
			final OptionalInt match = store.findMatchingSlot(codec.canonicalHash(schema));
			if (match.isPresent())
			{
				return match;
			}
			final OptionalInt next = store.findNextEmptySlot();
			if (next.isEmpty())
			{
				log.info(PresetConstants.LOG_SLOTS_FULL);
				return OptionalInt.empty();
			}
			store.write(next.getAsInt(), schema);
			return next;
		} catch (final IOException ex)
		{
			log.warn(PresetConstants.LOG_HANDLER_FAILED, ex);
			return OptionalInt.empty();
		}
	}
}
