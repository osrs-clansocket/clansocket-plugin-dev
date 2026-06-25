package com.clansocket.tracking.social;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;

import com.clansocket.ClanSocketConfig;
import com.clansocket.bus.EventBatcher;
import com.clansocket.protocol.common.Payload;
import com.clansocket.util.Strings;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ChatTracker
{
	@Inject
	private Client client;
	@Inject
	private ClanSocketConfig config;
	@Inject
	private EventBatcher batcher;

	@Subscribe
	public void onChatMessage(final ChatMessage event)
	{
		final ChatMessageType type = event.getType();
		if (!SocialConstants.CLAN_CHAT_TYPES.contains(type))
		{
			return;
		}
		if (!config.sendClanChat())
		{
			return;
		}
		final String text = ChatTextSanitizer.sanitize(event.getMessage());
		if (text.isEmpty())
		{
			log.debug("ClanSocket chat skip: empty after sanitize (raw='{}')", event.getMessage());
			return;
		}
		if (shouldSkipBroadcast(text))
		{
			return;
		}
		final String kind = resolveKind(type);
		final String sender = resolveSender(kind, event.getName(), text);
		batcher.enqueue(new Payload("chat", "channel", SocialConstants.CLAN_CHANNEL, "kind", kind, "world",
		        client.getWorld(), "senderRsn", sender, "text", text, "eventTs", event.getTimestamp()));
	}

	private static String resolveKind(final ChatMessageType type)
	{
		if (type == ChatMessageType.CLAN_CHAT || type == ChatMessageType.CLAN_GIM_FORM_GROUP
		        || type == ChatMessageType.CLAN_GIM_GROUP_WITH)
		{
			return SocialConstants.CHAT_KIND_MESSAGE;
		}
		return SocialConstants.CHAT_KIND_BROADCAST;
	}

	private static String resolveSender(final String kind, final String rawName, final String text)
	{
		if (SocialConstants.CHAT_KIND_BROADCAST.equals(kind))
		{
			return categorizeBroadcast(text);
		}
		final String cleaned = ChatTextSanitizer.sanitize(rawName);
		return cleaned.isEmpty() ? SocialConstants.CHAT_SYSTEM_SENDER : cleaned;
	}

	private static boolean shouldSkipBroadcast(final String text)
	{
		if (Strings.isEmpty(text))
		{
			return true;
		}
		for (final String fragment : SocialConstants.CHAT_SKIP_FRAGMENTS)
		{
			if (text.contains(fragment))
			{
				return true;
			}
		}
		return false;
	}

	private static String categorizeBroadcast(final String text)
	{
		if (Strings.isEmpty(text))
		{
			return SocialConstants.BROADCAST_CATEGORY_DEFAULT;
		}
		for (final Map.Entry<String, String> entry : SocialConstants.BROADCAST_CATEGORIES.entrySet())
		{
			if (text.contains(entry.getKey()))
			{
				return entry.getValue();
			}
		}
		return SocialConstants.BROADCAST_CATEGORY_DEFAULT;
	}
}
