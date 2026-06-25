package com.clansocket.tracking.identity;

import javax.inject.Singleton;

import net.runelite.api.GameState;

import com.clansocket.bus.primitive.LatchedSnapshotTracker;
import com.clansocket.protocol.common.Payload;

@Singleton
public class LoginStateTracker extends LatchedSnapshotTracker<GameState>
{
	@Override
	protected void onLoginScreen()
	{
		emitIfChanged(GameState.LOGIN_SCREEN);
	}

	@Override
	protected void onLoggedIn()
	{
		emitIfChanged(GameState.LOGGED_IN);
	}

	@Override
	protected void emitFreshSnapshot()
	{
		final GameState state = client.getGameState();
		if (state == null)
		{
			return;
		}
		batcher.enqueue(new Payload("login_state", "state", state.name()));
		latch.preset(state);
	}

	private void emitIfChanged(final GameState state)
	{
		if (latch.update(state))
		{
			batcher.enqueue(new Payload("login_state", "state", state.name()));
		}
	}
}
