package com.clansocket.tracking.movement;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

import com.clansocket.protocol.common.Where;
import com.clansocket.world.AreaResolver;

@Singleton
public final class LocationContext
{
	@Inject
	private Client client;
	@Inject
	private AreaResolver areaResolver;

	public Where capture()
	{
		final Player local = client.getLocalPlayer();
		if (local == null)
		{
			return null;
		}
		final WorldPoint p = resolvePoint(local);
		if (p == null)
		{
			return null;
		}
		final int regionId = p.getRegionID();
		final String name = areaResolver.resolve(regionId);
		return new Where(client.getWorld(), p.getX(), p.getY(), p.getPlane(), regionId, name, name);
	}

	public WorldPoint resolvePoint(final Player local)
	{
		final WorldView wv = client.getTopLevelWorldView();
		if (wv != null && wv.isInstance())
		{
			final LocalPoint lp = local.getLocalLocation();
			if (lp != null)
			{
				return WorldPoint.fromLocalInstance(client, lp);
			}
		}
		return local.getWorldLocation();
	}
}
