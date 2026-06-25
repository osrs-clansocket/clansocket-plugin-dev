package com.clansocket.tracking.loot;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.client.game.ItemManager;

@Singleton
public final class PetNameResolver
{
	@Inject
	private ItemManager itemManager;

	@SuppressWarnings("checkstyle:IllegalCatch")
	public Integer resolve(final String name)
	{
		if (name == null || name.isEmpty())
		{
			return null;
		}
		for (final Integer id : PetCatalog.allIds())
		{
			try
			{
				if (name.equalsIgnoreCase(itemManager.getItemComposition(id).getName()))
				{
					return id;
				}
			} catch (final RuntimeException ignored)
			{
				continue;
			}
		}
		return null;
	}

	@SuppressWarnings("checkstyle:IllegalCatch")
	public String resolveName(final int itemId)
	{
		try
		{
			final String name = itemManager.getItemComposition(itemId).getName();
			return name == null ? "?" : name;
		} catch (final RuntimeException ex)
		{
			return "?";
		}
	}
}
