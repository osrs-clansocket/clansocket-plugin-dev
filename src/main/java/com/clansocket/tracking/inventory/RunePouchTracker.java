package com.clansocket.tracking.inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.EnumComposition;
import net.runelite.api.EnumID;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.Subscribe;

import com.clansocket.ClanSocketConfig;
import com.clansocket.bus.Hashes;
import com.clansocket.bus.primitive.LatchedSnapshotTracker;
import com.clansocket.protocol.common.Payload;
import com.clansocket.protocol.inventory.RunePouchSlot;

@Singleton
public class RunePouchTracker extends LatchedSnapshotTracker<Long>
{
	@Inject
	private ItemNames itemNames;
	@Inject
	private ClanSocketConfig config;

	@Override
	protected void emitFreshSnapshot()
	{
		if (!config.streamRunePouch())
		{
			return;
		}
		latch.reset();
		readAndEmit();
	}

	@Subscribe
	public void onVarbitChanged(final VarbitChanged event)
	{
		if (!config.streamRunePouch())
		{
			return;
		}
		if (!InventoryConstants.RUNE_POUCH_WATCHED.contains(event.getVarbitId()) || !isLoggedIn())
		{
			return;
		}
		readAndEmit();
	}

	private void readAndEmit()
	{
		final EnumComposition runeEnum = client.getEnum(EnumID.RUNEPOUCH_RUNE);
		if (runeEnum != null)
		{
			emitIfChanged(runeEnum);
		}
	}

	private void emitIfChanged(final EnumComposition runeEnum)
	{
		final int slotCount = InventoryConstants.RUNE_POUCH_TYPE_VARBITS.length;
		final List<RunePouchSlot> slots = new ArrayList<>(slotCount);
		final long sig = collectSlots(runeEnum, slots, slotCount);
		if (!latch.update(sig))
		{
			return;
		}
		batcher.enqueue(
		        new Payload("rune_pouch", "hash", Long.toHexString(sig), "slots", Collections.unmodifiableList(slots)));
	}

	private long collectSlots(final EnumComposition runeEnum, final List<RunePouchSlot> out, final int slotCount)
	{
		long sig = 1L;
		for (int i = 0; i < slotCount; i++)
		{
			sig = sig * Hashes.HASH_PRIME + client.getVarbitValue(InventoryConstants.RUNE_POUCH_QTY_VARBITS[i]);
			sig = sig * Hashes.HASH_PRIME + client.getVarbitValue(InventoryConstants.RUNE_POUCH_TYPE_VARBITS[i]);
			final RunePouchSlot slot = resolveSlot(runeEnum, i);
			if (slot != null)
			{
				out.add(slot);
			}
		}
		return sig;
	}

	private RunePouchSlot resolveSlot(final EnumComposition runeEnum, final int index)
	{
		final int qty = client.getVarbitValue(InventoryConstants.RUNE_POUCH_QTY_VARBITS[index]);
		final int runeKey = client.getVarbitValue(InventoryConstants.RUNE_POUCH_TYPE_VARBITS[index]);
		if (qty <= 0 || runeKey <= 0)
		{
			return null;
		}
		final int itemId = runeEnum.getIntValue(runeKey);
		if (itemId <= 0)
		{
			return null;
		}
		return new RunePouchSlot(index, itemId, qty, itemNames.resolve(itemId), itemNames.resolvePrice(itemId));
	}
}
