package com.clansocket.bus;

import javax.inject.Inject;

import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;

@SuppressWarnings("PMD.CompareObjectsWithEquals")
public abstract class AbstractTracker
{
	@Inject
	protected Client client;
	@Inject
	protected EventBatcher batcher;

	protected final boolean isLoggedIn()
	{
		return client.getGameState() == GameState.LOGGED_IN;
	}

	protected final boolean isLocalPlayer(final Actor actor)
	{
		return actor != null && actor == client.getLocalPlayer();
	}
}
