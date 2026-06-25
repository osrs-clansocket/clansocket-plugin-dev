package com.clansocket.tracking.identity;

import java.time.Instant;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WorldChanged;
import net.runelite.client.eventbus.Subscribe;

import com.clansocket.bus.AbstractStateTracker;
import com.clansocket.bus.primitive.WarmupCounter;

@Singleton
public class IdentityTracker extends AbstractStateTracker
{
	private enum Phase
	{
		INITIAL, AWAITING_LOGIN, WARMUP, ACTIVE
	}

	@Inject
	private IdentityEmitter emitter;

	private final WarmupCounter warmup = new WarmupCounter(IdentityConstants.CLAN_SETTINGS_TICK_WAIT);
	private final WarmupCounter reassert = new WarmupCounter(IdentityConstants.IDENTITY_REASSERT_TICKS);

	private volatile Phase phase = Phase.INITIAL;
	private volatile boolean sessionReminderSent;
	private volatile String sessionStartIso;

	@Override
	protected void onLoginScreen()
	{
		phase = Phase.AWAITING_LOGIN;
		warmup.reset();
		reassert.reset();
		sessionReminderSent = false;
		sessionStartIso = null;
	}

	@Override
	protected void onLoggedIn()
	{
		if (phase != Phase.AWAITING_LOGIN)
		{
			return;
		}
		phase = Phase.WARMUP;
		warmup.reset();
		if (sessionStartIso == null)
		{
			sessionStartIso = Instant.now().toString();
		}
	}

	@Override
	protected void onOtherGameState(final GameState state)
	{
		if ((state == GameState.HOPPING || state == GameState.CONNECTION_LOST)
		        && (phase == Phase.ACTIVE || phase == Phase.WARMUP))
		{
			phase = Phase.WARMUP;
			warmup.reset();
		}
	}

	@Override
	public void resetForReconnect()
	{
		phase = Phase.WARMUP;
		warmup.skipToReady();
		reassert.reset();
	}

	@Subscribe
	public void onGameTick(final GameTick tick)
	{
		if (phase == Phase.WARMUP)
		{
			attemptInitialEmit();
		} else if (phase == Phase.ACTIVE && isLoggedIn())
		{
			maybeReassert();
		}
	}

	private void attemptInitialEmit()
	{
		final Player local = client.getLocalPlayer();
		if (local == null || local.getName() == null)
		{
			return;
		}
		if (!warmup.tickAndReady())
		{
			return;
		}
		sessionReminderSent = emitter.emit(local, sessionStartIso, sessionReminderSent);
		phase = Phase.ACTIVE;
		reassert.reset();
	}

	private void maybeReassert()
	{
		if (!reassert.tickAndReady())
		{
			return;
		}
		reassert.reset();
		final Player local = client.getLocalPlayer();
		if (local != null && local.getName() != null)
		{
			sessionReminderSent = emitter.emit(local, sessionStartIso, sessionReminderSent);
		}
	}

	@Subscribe
	public void onWorldChanged(final WorldChanged event)
	{
		phase = Phase.WARMUP;
		warmup.reset();
		reassert.reset();
	}
}
