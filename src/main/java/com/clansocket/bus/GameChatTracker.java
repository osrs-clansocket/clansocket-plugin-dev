package com.clansocket.bus;

import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

public abstract class GameChatTracker extends AbstractTracker
{
	@Subscribe
	public final void onChatMessage(final ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM)
		{
			return;
		}
		final String raw = event.getMessage();
		if (raw == null)
		{
			return;
		}
		handleGameChat(Text.removeTags(raw));
	}

	protected abstract void handleGameChat(String text);
}
