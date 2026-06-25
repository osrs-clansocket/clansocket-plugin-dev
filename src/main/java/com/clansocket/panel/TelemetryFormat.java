package com.clansocket.panel;

import java.awt.Color;

import net.runelite.client.ui.ColorScheme;

public final class TelemetryFormat
{
	private TelemetryFormat() {
	}

	public static String formatAgo(final long lastEventAtMs)
	{
		if (lastEventAtMs <= PanelConstants.NEVER_EVENT_AT_MS)
		{
			return PanelConstants.AGO_NEVER;
		}
		final long agoMs = System.currentTimeMillis() - lastEventAtMs;
		final long seconds = agoMs / PanelConstants.MS_PER_SECOND;
		if (seconds < PanelConstants.SECONDS_PER_MINUTE)
		{
			return seconds + "s ago";
		}
		final long minutes = seconds / PanelConstants.SECONDS_PER_MINUTE;
		if (minutes < PanelConstants.MINUTES_PER_HOUR)
		{
			return minutes + "m ago";
		}
		final long hours = minutes / PanelConstants.MINUTES_PER_HOUR;
		if (hours < PanelConstants.HOURS_PER_DAY)
		{
			return hours + "h ago";
		}
		return (hours / PanelConstants.HOURS_PER_DAY) + "d ago";
	}

	public static Color dotColor(final long lastEventAtMs)
	{
		if (lastEventAtMs <= PanelConstants.NEVER_EVENT_AT_MS)
		{
			return ColorScheme.MEDIUM_GRAY_COLOR;
		}
		final long agoMs = System.currentTimeMillis() - lastEventAtMs;
		return agoMs <= PanelConstants.RECENT_THRESHOLD_MS
		        ? ColorScheme.PROGRESS_COMPLETE_COLOR
		        : ColorScheme.MEDIUM_GRAY_COLOR;
	}
}
