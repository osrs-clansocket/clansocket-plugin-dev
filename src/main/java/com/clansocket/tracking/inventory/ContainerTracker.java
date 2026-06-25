package com.clansocket.tracking.inventory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.ItemContainer;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.eventbus.Subscribe;

import com.clansocket.ClanSocketConfig;
import com.clansocket.bus.AbstractStateTracker;
import com.clansocket.bus.primitive.IntIntMap;
import com.clansocket.protocol.common.Payload;
import com.clansocket.protocol.inventory.Item;
import com.clansocket.protocol.inventory.ItemCause;
import com.clansocket.protocol.inventory.ItemChange;
import com.clansocket.tracking.social.MenuClickBuffer;

@Singleton
public class ContainerTracker extends AbstractStateTracker
{
	@Inject
	private ClanSocketConfig config;
	@Inject
	private ItemNames itemNames;
	@Inject
	private MenuClickBuffer menuBuffer;

	private final Map<Integer, Pending> pending = new HashMap<>();
	private final Map<Integer, IntIntMap> lastEmitted = new HashMap<>();
	private final Map<Integer, Long> lastEmittedHash = new HashMap<>();
	private boolean didInitialSnapshot;

	@Override
	protected void onLoginScreen()
	{
		lastEmitted.clear();
		lastEmittedHash.clear();
		pending.clear();
		itemNames.clear();
		didInitialSnapshot = false;
	}

	@Override
	protected void emitFreshSnapshot()
	{
		lastEmitted.clear();
		lastEmittedHash.clear();
		didInitialSnapshot = false;
	}

	@Subscribe
	public void onItemContainerChanged(final ItemContainerChanged event)
	{
		final String label = enabledLabel(event.getContainerId());
		final ItemContainer ic = label == null ? null : event.getItemContainer();
		if (ic != null)
		{
			pending.put(event.getContainerId(), ItemSnapshots.rawSnapshot(label, ic));
		}
	}

	@Subscribe
	public void onGameTick(final GameTick tick)
	{
		if (!didInitialSnapshot && isLoggedIn())
		{
			for (final int id : InventoryConstants.INITIAL_SNAPSHOT_IDS)
			{
				queueSnapshotIfPresent(id);
			}
			didInitialSnapshot = true;
		}
		for (final Map.Entry<Integer, Pending> entry : pending.entrySet())
		{
			flush(entry.getKey(), entry.getValue());
		}
		pending.clear();
	}

	private void queueSnapshotIfPresent(final int rawId)
	{
		final String label = enabledLabel(rawId);
		final ItemContainer ic = label == null ? null : client.getItemContainer(rawId);
		if (ic != null)
		{
			pending.put(rawId, ItemSnapshots.rawSnapshot(label, ic));
		}
	}

	private void flush(final int rawId, final Pending p)
	{
		final long hash = ItemSnapshots.rawHashLong(p.label, p.ids, p.qtys, p.slots);
		final Long previousHash = lastEmittedHash.get(rawId);
		if (previousHash != null && previousHash.longValue() == hash)
		{
			return;
		}
		final IntIntMap current = Deltas.rawIdQtyMap(p.ids, p.qtys);
		final IntIntMap prev = lastEmitted.get(rawId);
		final List<Item> items = ItemSnapshots.resolve(p, itemNames);
		batcher.enqueue(
		        new Payload("container", "hash", Long.toHexString(hash), "containerLabel", p.label, "items", items));
		if (prev != null)
		{
			emitDelta(p.label, prev, current);
		}
		lastEmitted.put(rawId, current);
		lastEmittedHash.put(rawId, hash);
	}

	private void emitDelta(final String label, final IntIntMap prev, final IntIntMap current)
	{
		final List<ItemChange> delta = Deltas.compute(prev, current, itemNames);
		if (delta.isEmpty())
		{
			return;
		}
		final ItemCause cause = menuBuffer.recentCause(InventoryConstants.MENU_CAUSE_MS);
		batcher.enqueue(new Payload("container_delta", "containerLabel", label, "changes",
		        Collections.unmodifiableList(delta), "cause", cause));
	}

	private String enabledLabel(final int containerId)
	{
		final String label = InventoryConstants.LABEL_BY_CONTAINER_ID.get(containerId);
		if (label == null)
		{
			return null;
		}
		if (InventoryConstants.LABEL_INVENTORY.equals(label) && !config.streamInventory())
		{
			return null;
		}
		return label;
	}
}
