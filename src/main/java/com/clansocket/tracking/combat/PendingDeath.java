package com.clansocket.tracking.combat;

import net.runelite.api.coords.WorldPoint;

final class PendingDeath
{
	final int deathX;
	final int deathY;
	final int deathPlane;
	final Integer deathRegionId;
	final String causeKind;
	final Integer causeId;
	final String causeName;
	final String causeCategory;
	final Integer hpBefore;
	Integer respawnX;
	Integer respawnY;
	Integer respawnPlane;
	Integer respawnRegionId;

	@SuppressWarnings({"checkstyle:ParameterNumber", "PMD.ExcessiveParameterList"})
	PendingDeath(final WorldPoint deathLoc, final String causeKind, final Integer causeId, final String causeName,
	        final String causeCategory, final Integer hpBefore) {
		this.deathX = deathLoc.getX();
		this.deathY = deathLoc.getY();
		this.deathPlane = deathLoc.getPlane();
		this.deathRegionId = deathLoc.getRegionID();
		this.causeKind = causeKind;
		this.causeId = causeId;
		this.causeName = causeName;
		this.causeCategory = causeCategory;
		this.hpBefore = hpBefore;
	}

	void setRespawn(final WorldPoint p)
	{
		respawnX = p.getX();
		respawnY = p.getY();
		respawnPlane = p.getPlane();
		respawnRegionId = p.getRegionID();
	}

	boolean matchesRespawn(final WorldPoint p)
	{
		return respawnX != null && respawnX == p.getX() && respawnY == p.getY() && respawnPlane == p.getPlane();
	}
}
