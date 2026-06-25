package com.clansocket.tracking.inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.runelite.api.ItemContainer;

import com.clansocket.bus.Hashes;
import com.clansocket.protocol.inventory.Item;

final class ItemSnapshots
{
	private ItemSnapshots() {
	}

	@SuppressWarnings("PMD.UseVarargs")
	static List<Item> fromContainer(final ItemContainer ic, final ItemNames itemNames, final int[] tabBoundaries)
	{
		final net.runelite.api.Item[] raw = ic.getItems();
		final List<Item> items = new ArrayList<>(raw.length);
		for (int slot = 0; slot < raw.length; slot++)
		{
			final net.runelite.api.Item it = raw[slot];
			final int id = it.getId();
			if (id <= 0 || it.getQuantity() <= 0 || itemNames.isPlaceholder(id))
			{
				continue;
			}
			final Integer tab = tabBoundaries == null ? null : Integer.valueOf(tabForSlot(slot, tabBoundaries));
			items.add(new Item(id, it.getQuantity(), itemNames.resolve(id), itemNames.resolvePrice(id), slot, tab));
		}
		return Collections.unmodifiableList(items);
	}

	@SuppressWarnings("PMD.UseVarargs")
	static int tabForSlot(final int slot, final int[] tabBoundaries)
	{
		for (int t = 0; t < tabBoundaries.length; t++)
		{
			if (slot < tabBoundaries[t])
			{
				return t + 1;
			}
		}
		return 0;
	}

	static Pending rawSnapshot(final String label, final ItemContainer ic)
	{
		final net.runelite.api.Item[] raw = ic.getItems();
		final int[] ids = new int[raw.length];
		final int[] qtys = new int[raw.length];
		final int[] slots = new int[raw.length];
		int idx = 0;
		for (int slot = 0; slot < raw.length; slot++)
		{
			final net.runelite.api.Item it = raw[slot];
			if (it.getId() <= 0 || it.getQuantity() <= 0)
			{
				continue;
			}
			ids[idx] = it.getId();
			qtys[idx] = it.getQuantity();
			slots[idx] = slot;
			idx++;
		}
		return new Pending(label, Arrays.copyOf(ids, idx), Arrays.copyOf(qtys, idx), Arrays.copyOf(slots, idx));
	}

	static List<Item> resolve(final Pending p, final ItemNames itemNames)
	{
		final List<Item> items = new ArrayList<>(p.ids.length);
		for (int i = 0; i < p.ids.length; i++)
		{
			final int id = p.ids[i];
			items.add(new Item(id, p.qtys[i], itemNames.resolve(id), itemNames.resolvePrice(id), p.slots[i], null));
		}
		return Collections.unmodifiableList(items);
	}

	static String hashByIdQty(final List<Item> items)
	{
		return Long.toHexString(Hashes.pairsLong(items, i -> i.id, i -> i.qty));
	}

	@SuppressWarnings("PMD.UseVarargs")
	static long rawHashLong(final String label, final int[] ids, final int[] qtys, final int[] slots)
	{
		return Hashes.rollingArrays(label.hashCode(), ids, qtys, slots);
	}
}
