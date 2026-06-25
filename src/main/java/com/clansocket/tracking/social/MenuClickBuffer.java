package com.clansocket.tracking.social;

import javax.inject.Singleton;

import net.runelite.api.events.MenuOptionClicked;

import com.clansocket.protocol.inventory.ItemCause;

@Singleton
public final class MenuClickBuffer
{
	private volatile MenuOptionClicked last;
	private volatile long lastAtMs;

	public void record(final MenuOptionClicked event)
	{
		this.last = event;
		this.lastAtMs = System.currentTimeMillis();
	}

	public ItemCause recentCause(final long maxAgeMs)
	{
		final MenuOptionClicked m = last;
		if (m == null || (System.currentTimeMillis() - lastAtMs) > maxAgeMs)
		{
			return null;
		}
		return new ItemCause(m.getMenuAction().name(), m.getMenuOption(), m.getMenuTarget(), m.getId());
	}
}
