package com.clansocket.tracking.movement;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.WorldEntity;
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
		final LocalPoint lp = wv == null ? null : vesselPoint(wv, local);
		if (lp != null)
		{
			return wv.isInstance() ? WorldPoint.fromLocalInstance(client, lp)
			        : WorldPoint.fromLocal(wv, lp.getX(), lp.getY(), wv.getPlane());
		}
		if (wv != null && wv.isInstance())
		{
			final LocalPoint own = local.getLocalLocation();
			if (own != null)
			{
				return WorldPoint.fromLocalInstance(client, own);
			}
		}
		return local.getWorldLocation();
	}

	private static LocalPoint vesselPoint(final WorldView top, final Player local)
	{
		final WorldView own = local.getWorldView();
		final LocalPoint lp = local.getLocalLocation();
		if (own == null || own.isTopLevel() || lp == null)
		{
			return null;
		}
		final WorldEntity vessel = top.worldEntities().byIndex(own.getId());
		return vessel == null ? null : vessel.transformToMainWorld(lp);
	}
}
