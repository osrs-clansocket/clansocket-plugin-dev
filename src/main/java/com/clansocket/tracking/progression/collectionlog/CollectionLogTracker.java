package com.clansocket.tracking.progression.collectionlog;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.ScriptID;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.gameval.VarClientID;
import net.runelite.client.eventbus.Subscribe;

import com.clansocket.ClanSocketConfig;
import com.clansocket.bus.GameChatTracker;
import com.clansocket.protocol.common.Payload;
import com.clansocket.tracking.movement.LocationContext;
import com.clansocket.tracking.social.ChatTextSanitizer;

@Singleton
public class CollectionLogTracker extends GameChatTracker
{
	@Inject
	private ClanSocketConfig config;
	@Inject
	private LocationContext locationContext;

	private boolean notificationStarted;

	@Override
	protected void handleGameChat(final String text)
	{
		if (!config.streamCollectionLog())
		{
			return;
		}
		final String stripped = ChatTextSanitizer.sanitize(text);
		if (!stripped.startsWith(CollectionLogConstants.CHAT_PREFIX))
		{
			return;
		}
		final String itemName = stripped.substring(CollectionLogConstants.CHAT_PREFIX.length()).trim();
		if (itemName.isEmpty())
		{
			return;
		}
		emit(itemName);
	}

	@Subscribe
	public void onScriptPreFired(final ScriptPreFired event)
	{
		switch (event.getScriptId())
		{
			case ScriptID.NOTIFICATION_START :
				notificationStarted = true;
				break;
			case ScriptID.NOTIFICATION_DELAY :
				if (notificationStarted)
				{
					handleNotification();
					notificationStarted = false;
				}
				break;
			default :
				break;
		}
	}

	private void handleNotification()
	{
		if (!config.streamCollectionLog())
		{
			return;
		}
		final String title = client.getVarcStrValue(VarClientID.NOTIFICATION_TITLE);
		if (title == null || !CollectionLogConstants.NOTIFICATION_TITLE.equalsIgnoreCase(title))
		{
			return;
		}
		final String body = client.getVarcStrValue(VarClientID.NOTIFICATION_MAIN);
		if (body == null)
		{
			return;
		}
		final String itemName = parseItemName(body);
		if (itemName.isEmpty())
		{
			return;
		}
		emit(itemName);
	}

	private void emit(final String itemName)
	{
		batcher.enqueue(new Payload("collection_log_entry", "itemName", itemName, "itemId", null, "where",
		        locationContext.capture(), "category", null, "sourceKind", null));
	}

	private static String parseItemName(final String body)
	{
		final String stripped = ChatTextSanitizer.sanitize(body);
		if (!stripped.startsWith(CollectionLogConstants.NOTIFICATION_PREFIX))
		{
			return "";
		}
		return stripped.substring(CollectionLogConstants.NOTIFICATION_PREFIX.length()).trim();
	}
}
