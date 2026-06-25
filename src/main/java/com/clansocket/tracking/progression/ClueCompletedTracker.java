package com.clansocket.tracking.progression;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.clansocket.ClanSocketConfig;
import com.clansocket.bus.GameChatTracker;
import com.clansocket.tracking.movement.LocationContext;

@Singleton
public class ClueCompletedTracker extends GameChatTracker
{
	@Inject
	private ClanSocketConfig config;
	@Inject
	private LocationContext locationContext;

	@Override
	protected void handleGameChat(final String text)
	{
		if (!config.streamClues())
		{
			return;
		}
		ClueCompletionParser.parse(text, locationContext.capture()).ifPresent(batcher::enqueue);
	}
}
