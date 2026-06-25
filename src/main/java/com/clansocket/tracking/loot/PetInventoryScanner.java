package com.clansocket.tracking.loot;

import java.util.HashSet;
import java.util.Set;

import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.gameval.InventoryID;

final class PetInventoryScanner
{
	private PetInventoryScanner() {
	}

	static Set<Integer> snapshot(final Client client)
	{
		final Set<Integer> pets = new HashSet<>();
		final ItemContainer inv = client.getItemContainer(InventoryID.INV);
		if (inv == null)
		{
			return pets;
		}
		for (final Item item : inv.getItems())
		{
			if (item != null && PetCatalog.isPet(item.getId()))
			{
				pets.add(item.getId());
			}
		}
		return pets;
	}
}
