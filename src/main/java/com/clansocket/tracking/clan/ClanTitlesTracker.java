package com.clansocket.tracking.clan;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.clansocket.bus.primitive.AbstractPolledSnapshotTracker;

@Singleton
public class ClanTitlesTracker extends AbstractPolledSnapshotTracker
{
	@Inject
	public ClanTitlesTracker(final ClanTitlesEmitter emitter) {
		super(emitter, ClanConstants.CLAN_SETTINGS_TICK_WAIT, ClanConstants.TITLES_REPOLL_TICKS);
	}
}
