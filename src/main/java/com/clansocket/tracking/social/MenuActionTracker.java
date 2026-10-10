package com.clansocket.tracking.social;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.MenuAction;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.WidgetUtil;
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
		if (SocialConstants.TRACKED_MENU_ACTIONS.contains(action))
		{
			enqueue(event, action, event.getId(), targetKind(action.name()));
		}
		else if (event.isItemOp())
		{
			enqueue(event, action, event.getItemId(), SocialConstants.KIND_ITEM);
		}
		else if (action == MenuAction.CC_OP
		        && WidgetUtil.componentToInterface(event.getParam1()) == InterfaceID.EMOTE)
		{
			enqueue(event, action, event.getParam0(), SocialConstants.KIND_EMOTE);
		}
	}

	private void enqueue(final MenuOptionClicked event, final MenuAction action, final int id, final String kind)
	{
		batcher.enqueue(new Payload("menu_action", "action", action.name(), "option", event.getMenuOption(), "target",
		        ChatTextSanitizer.sanitize(event.getMenuTarget()), "id", id, "targetKind", kind));
	}

	private static String targetKind(final String actionName)
	{
		return SocialConstants.MENU_KIND_BY_PREFIX.entrySet().stream().filter(e -> actionName.startsWith(e.getKey()))
		        .map(Map.Entry::getValue).findFirst().orElse(null);
	}
}
