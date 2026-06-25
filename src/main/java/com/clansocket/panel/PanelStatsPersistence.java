package com.clansocket.panel;

import java.util.concurrent.atomic.AtomicLong;

import net.runelite.client.config.ConfigManager;

import com.clansocket.ClanSocketConstants;
import com.clansocket.panel.widgets.StreamGate;

public final class PanelStatsPersistence
{
	private PanelStatsPersistence() {
	}

	public static void load(final ConfigManager mgr, final AtomicLong[] counts, final RateBuffer[] rates)
	{
		for (int i = 0; i < StreamGate.ALL.size(); i++)
		{
			final StreamGate gate = StreamGate.ALL.get(i);
			final Long c = mgr.getRSProfileConfiguration(ClanSocketConstants.CONFIG_GROUP, countConfigKey(gate),
			        long.class);
			counts[i].set(c == null ? 0L : c);
			final Long t = mgr.getRSProfileConfiguration(ClanSocketConstants.CONFIG_GROUP, lastEventAtConfigKey(gate),
			        long.class);
			rates[i].setLastEventAt(t == null ? 0L : t);
		}
	}

	public static void flush(final ConfigManager mgr, final AtomicLong[] counts, final RateBuffer[] rates)
	{
		if (mgr.getRSProfileKey() == null)
		{
			return;
		}
		for (int i = 0; i < StreamGate.ALL.size(); i++)
		{
			final StreamGate gate = StreamGate.ALL.get(i);
			mgr.setRSProfileConfiguration(ClanSocketConstants.CONFIG_GROUP, countConfigKey(gate), counts[i].get());
			mgr.setRSProfileConfiguration(ClanSocketConstants.CONFIG_GROUP, lastEventAtConfigKey(gate),
			        rates[i].lastEventAt());
		}
	}

	public static void reset(final ConfigManager mgr, final AtomicLong[] counts, final RateBuffer[] rates)
	{
		for (int i = 0; i < counts.length; i++)
		{
			counts[i].set(0L);
			rates[i].setLastEventAt(0L);
		}
		if (mgr.getRSProfileKey() == null)
		{
			return;
		}
		for (final StreamGate gate : StreamGate.ALL)
		{
			mgr.unsetRSProfileConfiguration(ClanSocketConstants.CONFIG_GROUP, countConfigKey(gate));
			mgr.unsetRSProfileConfiguration(ClanSocketConstants.CONFIG_GROUP, lastEventAtConfigKey(gate));
		}
	}

	private static String countConfigKey(final StreamGate gate)
	{
		return "count." + normalizeKey(gate.displayName());
	}

	private static String lastEventAtConfigKey(final StreamGate gate)
	{
		return "lastat." + normalizeKey(gate.displayName());
	}

	private static String normalizeKey(final String displayName)
	{
		return displayName.toLowerCase().replace(' ', '_').replace('-', '_');
	}
}
