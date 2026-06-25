package com.clansocket.tracking.progression.collectionlog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.clansocket.bus.Hashes;
import com.clansocket.bus.primitive.IntIntMap;
import com.clansocket.protocol.collection.CollectionLogItem;
import com.clansocket.protocol.common.Payload;
import com.clansocket.tracking.inventory.ItemNames;

final class CollectionLogSnapshotBuilder
{
	private CollectionLogSnapshotBuilder() {
	}

	static Payload build(final IntIntMap accumulator, final ItemNames itemNames)
	{
		final List<CollectionLogItem> items = sortedItems(accumulator, itemNames);
		return new Payload("collection_log_snapshot", "hash",
		        Long.toHexString(Hashes.pairsLong(items, i -> i.itemId, i -> i.quantity)), "itemCount", items.size(),
		        "items", Collections.unmodifiableList(items));
	}

	private static List<CollectionLogItem> sortedItems(final IntIntMap accumulator, final ItemNames itemNames)
	{
		final int[] ids = accumulator.keys();
		Arrays.sort(ids);
		final List<CollectionLogItem> items = new ArrayList<>(ids.length);
		for (final int id : ids)
		{
			items.add(new CollectionLogItem(id, accumulator.get(id), itemNames.resolve(id), itemNames.resolvePrice(id),
			        null));
		}
		return items;
	}
}
