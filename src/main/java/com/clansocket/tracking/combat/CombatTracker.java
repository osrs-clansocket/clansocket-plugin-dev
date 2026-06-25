package com.clansocket.tracking.combat;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Actor;
import net.runelite.api.Hitsplat;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.InteractingChanged;
import net.runelite.client.eventbus.Subscribe;

import com.clansocket.ClanSocketConfig;
import com.clansocket.bus.AbstractTracker;
import com.clansocket.bus.Latched;
import com.clansocket.protocol.common.Payload;

@Singleton
public class CombatTracker extends AbstractTracker
{
	@Inject
	private ClanSocketConfig config;
	@Inject
	private InteractionState interactionState;
	@Inject
	private AttackStyleTracker attackStyleTracker;

	private final Latched<String> interactLatch = new Latched<>();

	@Subscribe
	public void onInteractingChanged(final InteractingChanged event)
	{
		if (!config.streamCombat() || !isLocalPlayer(event.getSource()))
		{
			return;
		}
		final Actor target = event.getTarget();
		if (target == null)
		{
			interactLatch.reset();
			return;
		}
		if (target instanceof NPC)
		{
			recordInteractNpc((NPC) target);
		} else if (target instanceof Player)
		{
			recordInteractPlayer();
		}
	}

	private void recordInteractNpc(final NPC npc)
	{
		interactionState.recordNpc(npc.getId(), npc.getName());
		emitInteracting(CombatConstants.INTERACT_KIND_NPC, npc.getId(), npc.getId(), npc.getName());
	}

	private void recordInteractPlayer()
	{
		interactionState.recordPlayer();
		emitInteracting(CombatConstants.INTERACT_KIND_PLAYER, CombatConstants.INTERACT_DEDUP_PLAYER, null, null);
	}

	private void emitInteracting(final String kind, final int dedupId, final Integer payloadId, final String name)
	{
		if (!interactLatch.update(kind + ":" + dedupId))
		{
			return;
		}
		batcher.enqueue(new Payload("interacting", "targetKind", kind, "targetId", payloadId, "targetName", name));
	}

	@Subscribe
	public void onHitsplatApplied(final HitsplatApplied event)
	{
		if (!config.streamCombat())
		{
			return;
		}
		final Hitsplat splat = event.getHitsplat();
		final Actor actor = event.getActor();
		if (splat == null || actor == null)
		{
			return;
		}
		if (isLocalPlayer(actor))
		{
			batcher.enqueue(new Payload("damage_taken", "amount", splat.getAmount(), "hitsplatType",
			        splat.getHitsplatType(), "hitsplatName", null, "sourceKind", interactionState.consumeKind(),
			        "sourceId", interactionState.consumeId(), "sourceName", interactionState.consumeName()));
			return;
		}
		if (splat.isMine())
		{
			emitDamageDealt(actor, splat);
		}
	}

	private void emitDamageDealt(final Actor actor, final Hitsplat splat)
	{
		final String style = attackStyleTracker.getCurrent();
		if (actor instanceof NPC)
		{
			final NPC npc = (NPC) actor;
			batcher.enqueue(new Payload("damage_dealt", "amount", splat.getAmount(), "hitsplatType",
			        splat.getHitsplatType(), "hitsplatName", null, "targetKind", CombatConstants.INTERACT_KIND_NPC,
			        "targetId", npc.getId(), "targetName", npc.getName(), "attackStyle", style));
		} else if (actor instanceof Player)
		{
			batcher.enqueue(new Payload("damage_dealt", "amount", splat.getAmount(), "hitsplatType",
			        splat.getHitsplatType(), "hitsplatName", null, "targetKind", CombatConstants.INTERACT_KIND_PLAYER,
			        "targetId", null, "targetName", null, "attackStyle", style));
		}
	}
}
