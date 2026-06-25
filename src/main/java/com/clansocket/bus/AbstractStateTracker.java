package com.clansocket.bus;

import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.eventbus.Subscribe;

public abstract class AbstractStateTracker extends AbstractTracker
{
	@Subscribe
	public final void onGameStateChanged(final GameStateChanged event)
	{
		final GameState state = event.getGameState();
		if (state == GameState.LOGIN_SCREEN)
		{
			onLoginScreen();
		} else if (state == GameState.LOGGED_IN)
		{
			onLoggedIn();
		} else
		{
			onOtherGameState(state);
		}
	}

	protected void onLoginScreen()
	{
	}

	protected void onLoggedIn()
	{
	}

	protected void onOtherGameState(final GameState state)
	{
	}

	public void resetForReconnect()
	{
		if (isLoggedIn())
		{
			emitFreshSnapshot();
		}
	}

	protected void emitFreshSnapshot()
	{
	}
}
