package com.clansocket.tracking.farming;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.Subscribe;

import com.clansocket.ClanSocketConfig;
import com.clansocket.bus.AbstractStateTracker;
import com.clansocket.protocol.common.Payload;
import com.clansocket.protocol.common.Where;
import com.clansocket.tracking.movement.LocationContext;

@Singleton
public class FarmingTracker extends AbstractStateTracker
{
	@Inject
	private ClanSocketConfig config;
	@Inject
	private LocationContext locationContext;

	@Subscribe
	public void onVarbitChanged(final VarbitChanged event)
	{
		if (!config.streamFarming())
		{
			return;
		}
		final int varbitId = event.getVarbitId();
		if (!FarmingConstants.WATCHED_VARBITS.contains(varbitId) || !isLoggedIn())
		{
			return;
		}
		final Where where = locationContext.capture();
		if (where == null)
		{
			return;
		}
		batcher.enqueue(new Payload("farming_patch", "varbitId", varbitId, "value", client.getVarbitValue(varbitId),
		        "where", where));
	}
}
