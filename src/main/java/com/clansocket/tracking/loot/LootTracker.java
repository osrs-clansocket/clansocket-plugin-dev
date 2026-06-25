package com.clansocket.tracking.loot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.NPCComposition;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ServerNpcLoot;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.client.util.Text;

import com.clansocket.ClanSocketConfig;
import com.clansocket.bus.AbstractTracker;
import com.clansocket.protocol.common.Payload;
import com.clansocket.protocol.inventory.Item;
import com.clansocket.tracking.inventory.ItemNames;
import com.clansocket.tracking.movement.LocationContext;

import lombok.RequiredArgsConstructor;

@Singleton
public class LootTracker extends AbstractTracker
{
	@Inject
	private ItemNames itemNames;
	@Inject
	private ClanSocketConfig config;
	@Inject
	private KillCountLookup killCounts;
	@Inject
	private LocationContext locationContext;

	@Subscribe(priority = -1f)
	public void onServerNpcLoot(final ServerNpcLoot event)
	{
		if (!config.streamLoot() || event == null || event.getComposition() == null)
		{
			return;
		}
		final NPCComposition npc = event.getComposition();
		enqueue(new Source(LootConstants.LOOT_TYPE_NPC, npc.getId(), npc.getName(), npc.getCombatLevel()),
		        event.getItems());
	}

	@Subscribe(priority = -1f)
	public void onLootReceived(final LootReceived event)
	{
		if (!config.streamLoot() || event == null || event.getType() == null)
		{
			return;
		}
		final String sourceType = event.getType().name();
		if (LootConstants.LOOT_TYPE_NPC.equals(sourceType))
		{
			return;
		}
		enqueue(new Source(sourceType, null, sanitizedSource(event), event.getCombatLevel()), event.getItems());
	}

	private void enqueue(final Source src, final Collection<ItemStack> items)
	{
		final String source = src.name == null ? null : Text.removeTags(src.name);
		final Integer kc = killCounts.bumpAndLookup(source, src.type);
		batcher.enqueue(new Payload("loot", "sourceType", src.type, "sourceId", src.id, "source", source, "sourceLevel",
		        src.level, "kc", kc, "items", snapshotItems(items), "where", locationContext.capture()));
	}

	private static String sanitizedSource(final LootReceived event)
	{
		if (LootConstants.LOOT_TYPE_PLAYER.equals(event.getType().name()))
		{
			return null;
		}
		return event.getName();
	}

	private List<Item> snapshotItems(final Collection<ItemStack> raw)
	{
		if (raw == null)
		{
			return Collections.emptyList();
		}
		final List<Item> out = new ArrayList<>(raw.size());
		for (final ItemStack it : raw)
		{
			if (it.getId() <= 0 || it.getQuantity() <= 0)
			{
				continue;
			}
			final int id = it.getId();
			out.add(new Item(id, it.getQuantity(), itemNames.resolve(id), itemNames.resolvePrice(id), -1, null));
		}
		return Collections.unmodifiableList(out);
	}

	@RequiredArgsConstructor
	private static final class Source
	{
		private final String type;
		private final Integer id;
		private final String name;
		private final int level;
	}
}
