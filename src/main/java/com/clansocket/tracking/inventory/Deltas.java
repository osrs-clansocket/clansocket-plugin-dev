package com.clansocket.tracking.inventory;

import java.util.ArrayList;
import java.util.List;

import com.clansocket.bus.primitive.IntIntMap;
import com.clansocket.protocol.inventory.ItemChange;

final class Deltas
{
	private Deltas() {
	}

	@SuppressWarnings("PMD.UseVarargs")
	static IntIntMap rawIdQtyMap(final int[] ids, final int[] qtys)
	{
		final IntIntMap out = new IntIntMap();
		for (int i = 0; i < ids.length; i++)
		{
			out.put(ids[i], out.get(ids[i]) + qtys[i]);
		}
		return out;
	}

	static List<ItemChange> compute(final IntIntMap prev, final IntIntMap current, final ItemNames itemNames)
	{
		final List<ItemChange> changes = new ArrayList<>();
		for (final int id : current.keys())
		{
			final int diff = current.get(id) - prev.get(id);
			if (diff != 0)
			{
				changes.add(new ItemChange(id, diff, itemNames.resolve(id), itemNames.resolvePrice(id)));
			}
		}
		for (final int id : prev.keys())
		{
			if (current.get(id) == 0)
			{
				changes.add(new ItemChange(id, -prev.get(id), itemNames.resolve(id), itemNames.resolvePrice(id)));
			}
		}
		return changes;
	}
}
