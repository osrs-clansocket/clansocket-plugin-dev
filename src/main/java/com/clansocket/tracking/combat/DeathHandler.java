package com.clansocket.tracking.combat;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;

import com.clansocket.ClanSocketConfig;
import com.clansocket.bus.AbstractTracker;
import com.clansocket.protocol.common.Payload;
import com.clansocket.tracking.movement.LocationContext;
import com.clansocket.world.AreaResolver;

@Singleton
public class DeathHandler extends AbstractTracker
{
	@Inject
	private ClanSocketConfig config;
	@Inject
	private InteractionState interactionState;
	@Inject
	private AreaResolver areaResolver;
	@Inject
	private LocationContext locationContext;

	private PendingDeath pendingDeath;
	private int respawnStableTicks;
	private int ticksSinceDeath;

	@Subscribe
	public void onGameTick(final GameTick tick)
	{
		final Player local = client.getLocalPlayer();
		if (local == null)
		{
			return;
		}
		interactionState.updateHp(client.getBoostedSkillLevel(Skill.HITPOINTS));
		advanceRespawnTracking(locationContext.resolvePoint(local));
	}

	private void advanceRespawnTracking(final WorldPoint here)
	{
		if (pendingDeath == null || here == null)
		{
			return;
		}
		ticksSinceDeath++;
		if (pendingDeath.matchesRespawn(here))
		{
			respawnStableTicks++;
		} else
		{
			pendingDeath.setRespawn(here);
			respawnStableTicks = 1;
		}
		if (respawnStableTicks >= CombatConstants.RESPAWN_STABLE_TICKS
		        || ticksSinceDeath >= CombatConstants.RESPAWN_TIMEOUT_TICKS)
		{
			flushPendingDeath();
		}
	}

	private void flushPendingDeath()
	{
		final String deathName = pendingDeath.deathRegionId != null
		        ? areaResolver.resolve(pendingDeath.deathRegionId)
		        : null;
		final String respawnName = pendingDeath.respawnRegionId != null
		        ? areaResolver.resolve(pendingDeath.respawnRegionId)
		        : null;
		batcher.enqueue(buildDeathPayload(deathName, respawnName));
		pendingDeath = null;
		respawnStableTicks = 0;
		ticksSinceDeath = 0;
	}

	private Payload buildDeathPayload(final String deathName, final String respawnName)
	{
		return new Payload("death", "x", pendingDeath.deathX, "y", pendingDeath.deathY, "plane",
		        pendingDeath.deathPlane, "regionId", pendingDeath.deathRegionId, "regionName", deathName, "area",
		        deathName, "causeKind", pendingDeath.causeKind, "causeId", pendingDeath.causeId, "causeName",
		        pendingDeath.causeName, "causeCategory", pendingDeath.causeCategory, "hpBefore", pendingDeath.hpBefore,
		        "respawnX", pendingDeath.respawnX, "respawnY", pendingDeath.respawnY, "respawnPlane",
		        pendingDeath.respawnPlane, "respawnRegionId", pendingDeath.respawnRegionId, "world", client.getWorld(),
		        "causeCombatLevel", resolveCauseCombatLevel(), "respawnRegionName", respawnName, "respawnArea",
		        respawnName);
	}

	private Integer resolveCauseCombatLevel()
	{
		if (!CombatConstants.INTERACT_KIND_NPC.equals(pendingDeath.causeKind) || pendingDeath.causeId == null)
		{
			return null;
		}
		final net.runelite.api.NPCComposition comp = client.getNpcDefinition(pendingDeath.causeId);
		return comp != null ? comp.getCombatLevel() : null;
	}

	@Subscribe
	public void onActorDeath(final ActorDeath event)
	{
		if (!config.streamDeath() || !isLocalPlayer(event.getActor()))
		{
			return;
		}
		final WorldPoint p = locationContext.resolvePoint(client.getLocalPlayer());
		if (p == null)
		{
			return;
		}
		final String kind = interactionState.consumeKind();
		pendingDeath = new PendingDeath(p, kind, interactionState.consumeId(), interactionState.consumeName(),
		        classifyCauseCategory(kind), interactionState.consumeHpBefore());
		respawnStableTicks = 0;
		ticksSinceDeath = 0;
		interactionState.reset();
	}

	private String classifyCauseCategory(final String kind)
	{
		if (CombatConstants.INTERACT_KIND_PLAYER.equals(kind))
		{
			return CombatConstants.CAUSE_CATEGORY_PVP;
		}
		if (CombatConstants.INTERACT_KIND_NPC.equals(kind))
		{
			return CombatConstants.CAUSE_CATEGORY_PVM;
		}
		final WorldView wv = client.getTopLevelWorldView();
		if (wv != null && wv.isInstance())
		{
			return CombatConstants.CAUSE_CATEGORY_INSTANCE;
		}
		return CombatConstants.CAUSE_CATEGORY_ENVIRONMENT;
	}
}
