package com.clansocket.tracking.social;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.MenuAction;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.eventbus.Subscribe;

import com.clansocket.ClanSocketConfig;
import com.clansocket.bus.AbstractTracker;
import com.clansocket.protocol.common.Payload;

@Singleton
public class MenuActionTracker extends AbstractTracker
{
	@Inject
	private ClanSocketConfig config;
	@Inject
	private MenuClickBuffer menuBuffer;

	@Subscribe
	public void onMenuOptionClicked(final MenuOptionClicked event)
	{
		menuBuffer.record(event);
		if (!config.streamMenuActions())
		{
			return;
		}
		final MenuAction action = event.getMenuAction();
		if (!SocialConstants.TRACKED_MENU_ACTIONS.contains(action))
		{
			return;
		}
		batcher.enqueue(new Payload("menu_action", "action", action.name(), "option", event.getMenuOption(), "target",
		        ChatTextSanitizer.sanitize(event.getMenuTarget()), "id", event.getId(), "targetKind",
		        targetKind(action.name())));
	}

	private static String targetKind(final String actionName)
	{
		return SocialConstants.MENU_KIND_BY_PREFIX.entrySet().stream().filter(e -> actionName.startsWith(e.getKey()))
		        .map(Map.Entry::getValue).findFirst().orElse(null);
	}
}
