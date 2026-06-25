package com.clansocket.tracking.movement;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;

import com.clansocket.ClanSocketConfig;
import com.clansocket.bus.Hashes;
import com.clansocket.bus.primitive.LatchedSnapshotTracker;
import com.clansocket.protocol.common.Payload;
import com.clansocket.world.AreaResolver;

@Singleton
public class LocationTracker extends LatchedSnapshotTracker<Long>
{
	@Inject
	private AreaResolver areaResolver;
	@Inject
	private LocationContext locationContext;
	@Inject
	private ClanSocketConfig config;

	private int lastWorld = -1;

	@Subscribe
	public void onGameTick(final GameTick tick)
	{
		if (!config.streamLocation())
		{
			return;
		}
		final Player local = client.getLocalPlayer();
		if (local == null)
		{
			return;
		}
		detectWorldHop();
		final WorldPoint p = locationContext.resolvePoint(local);
		if (p == null)
		{
			return;
		}
		if (!latch.update(Hashes.rollingLong(p.getX(), p.getY(), p.getPlane())))
		{
			return;
		}
		final String name = areaResolver.resolve(p.getRegionID());
		batcher.enqueue(new Payload("location", "x", p.getX(), "y", p.getY(), "plane", p.getPlane(), "region",
		        p.getRegionID(), "regionName", name, "area", name));
	}

	private void detectWorldHop()
	{
		final int world = client.getWorld();
		if (world <= 0)
		{
			return;
		}
		if (lastWorld > 0 && world != lastWorld)
		{
			batcher.enqueue(new Payload("world_hop", "fromWorld", lastWorld, "toWorld", world));
		}
		lastWorld = world;
	}
}
