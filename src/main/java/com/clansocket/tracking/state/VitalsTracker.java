package com.clansocket.tracking.state;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.eventbus.Subscribe;

import com.clansocket.ClanSocketConfig;
import com.clansocket.bus.Hashes;
import com.clansocket.bus.primitive.LatchedSnapshotTracker;
import com.clansocket.protocol.common.Payload;

@Singleton
public class VitalsTracker extends LatchedSnapshotTracker<Long>
{
	@Inject
	private ClanSocketConfig config;

	@Subscribe
	public void onGameTick(final GameTick tick)
	{
		if (!config.streamVitals() || !isLoggedIn())
		{
			return;
		}
		final int energy = client.getEnergy() / StateConstants.ENERGY_DIVISOR;
		final int weight = client.getWeight();
		final int spec = client.getVarpValue(VarPlayerID.SA_ENERGY) / StateConstants.SPEC_DIVISOR;
		final int hitpoints = client.getBoostedSkillLevel(Skill.HITPOINTS);
		final int prayer = client.getBoostedSkillLevel(Skill.PRAYER);
		final int maxHitpoints = client.getRealSkillLevel(Skill.HITPOINTS);
		final int maxPrayer = client.getRealSkillLevel(Skill.PRAYER);
		if (!latch.update(Hashes.rollingLong(energy, weight, spec, hitpoints, prayer, maxHitpoints, maxPrayer)))
		{
			return;
		}
		batcher.enqueue(new Payload("vitals", "energy", energy, "weight", weight, "spec", spec, "hitpoints", hitpoints,
		        "prayer", prayer, "maxHitpoints", maxHitpoints, "maxPrayer", maxPrayer));
	}
}
