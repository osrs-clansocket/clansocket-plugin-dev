package com.clansocket.tracking.clan;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.events.ClanMemberJoined;
import net.runelite.api.events.ClanMemberLeft;
import net.runelite.client.eventbus.Subscribe;

import com.clansocket.bus.primitive.AbstractPolledSnapshotTracker;

@Singleton
public class ClanRosterTracker extends AbstractPolledSnapshotTracker
{
	@Inject
	public ClanRosterTracker(final ClanRosterEmitter emitter) {
		super(emitter, ClanConstants.CLAN_SETTINGS_TICK_WAIT, ClanConstants.ROSTER_REPOLL_TICKS);
	}

	@Subscribe
	public void onClanMemberJoined(final ClanMemberJoined event)
	{
		if (isLoggedIn())
		{
			emitSnapshot();
		}
	}

	@Subscribe
	public void onClanMemberLeft(final ClanMemberLeft event)
	{
		if (isLoggedIn())
		{
			emitSnapshot();
		}
	}
}
