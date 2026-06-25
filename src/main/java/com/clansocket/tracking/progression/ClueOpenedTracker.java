package com.clansocket.tracking.progression;

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.eventbus.Subscribe;

import com.clansocket.ClanSocketConfig;
import com.clansocket.bus.AbstractTracker;
import com.clansocket.protocol.common.Payload;
import com.clansocket.tracking.inventory.ItemNames;
import com.clansocket.tracking.movement.LocationContext;

@Singleton
public class ClueOpenedTracker extends AbstractTracker
{
	@SuppressWarnings("checkstyle:StaticFinalOutsideConstants")
	private static final String[] CLUE_PREFIXES = {ProgressionConstants.CLUE_ITEM_PREFIX_CLUE,
	        ProgressionConstants.CLUE_ITEM_PREFIX_CHALLENGE, ProgressionConstants.CLUE_ITEM_PREFIX_TREASURE};

	@Inject
	private ItemNames itemNames;
	@Inject
	private ClanSocketConfig config;
	@Inject
	private LocationContext locationContext;

	@Subscribe
	public void onMenuOptionClicked(final MenuOptionClicked event)
	{
		if (!config.streamClues() || !ProgressionConstants.CLUE_MENU_READ.equals(event.getMenuOption()))
		{
			return;
		}
		final int itemId = event.getItemId();
		if (itemId <= 0)
		{
			return;
		}
		final String name = itemNames.resolve(itemId);
		if (name == null)
		{
			return;
		}
		final String tier = extractTier(name);
		if (tier == null)
		{
			return;
		}
		batcher.enqueue(new Payload("clue_opened", "tier", tier.toUpperCase(Locale.ROOT), "itemId", itemId, "itemName",
		        name, "where", locationContext.capture()));
	}

	private static String extractTier(final String name)
	{
		for (final String prefix : CLUE_PREFIXES)
		{
			if (!name.startsWith(prefix))
			{
				continue;
			}
			final int end = name.indexOf(')', prefix.length());
			if (end < 0)
			{
				continue;
			}
			final String tier = name.substring(prefix.length(), end);
			if (ProgressionConstants.CLUE_TIERS.contains(tier))
			{
				return tier;
			}
		}
		return null;
	}
}
