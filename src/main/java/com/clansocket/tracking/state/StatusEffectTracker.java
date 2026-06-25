package com.clansocket.tracking.state;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;

import com.clansocket.ClanSocketConfig;
import com.clansocket.bus.AbstractStateTracker;
import com.clansocket.bus.Latched;
import com.clansocket.protocol.common.Payload;
import com.clansocket.tracking.movement.LocationContext;

@Singleton
public class StatusEffectTracker extends AbstractStateTracker
{
	@Inject
	private ClanSocketConfig config;
	@Inject
	private LocationContext locationContext;

	private final Latched<Boolean> poisonLatch = new Latched<>(false);
	private final Latched<Boolean> venomLatch = new Latched<>(false);
	private final Latched<Boolean> diseaseLatch = new Latched<>(false);
	private final Latched<Boolean> coldLatch = new Latched<>(false);

	@Override
	protected void onLoginScreen()
	{
		resetLatches();
	}

	@Override
	protected void emitFreshSnapshot()
	{
		resetLatches();
		update();
	}

	private void resetLatches()
	{
		poisonLatch.preset(false);
		venomLatch.preset(false);
		diseaseLatch.preset(false);
		coldLatch.preset(false);
	}

	@Subscribe
	public void onVarbitChanged(final VarbitChanged event)
	{
		if (!config.streamStatusEffects())
		{
			return;
		}
		if (event.getVarpId() == VarPlayerID.POISON || event.getVarpId() == VarPlayerID.DISEASE
		        || event.getVarbitId() == VarbitID.WINT_WARMTH)
		{
			update();
		}
	}

	private void update()
	{
		if (!isLoggedIn())
		{
			return;
		}
		final int poison = client.getVarpValue(VarPlayerID.POISON);
		final int disease = client.getVarpValue(VarPlayerID.DISEASE);
		final int warmth = client.getVarbitValue(VarbitID.WINT_WARMTH);
		final boolean venomed = poison >= StateConstants.VENOM_THRESHOLD;
		emitIfChanged(poisonLatch, "POISON", poison > 0 && !venomed);
		emitIfChanged(venomLatch, "VENOM", venomed);
		emitIfChanged(diseaseLatch, "DISEASE", disease > 0);
		emitIfChanged(coldLatch, "COLD", warmth == 0);
	}

	private void emitIfChanged(final Latched<Boolean> latch, final String effect, final boolean current)
	{
		if (latch.update(current))
		{
			batcher.enqueue(new Payload("status_effect", "effect", effect, "active", current, "where",
			        locationContext.capture()));
		}
	}
}
