package com.clansocket.tracking.inventory;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.client.game.ItemManager;

import com.clansocket.bus.primitive.IntObjectMap;

@Singleton
@SuppressWarnings("checkstyle:IllegalCatch")
public final class ItemNames
{
	private final ItemManager itemManager;
	private final IntObjectMap<String> nameCache = new IntObjectMap<>();
	private final IntObjectMap<Long> priceCache = new IntObjectMap<>(16);

	@Inject
	public ItemNames(final ItemManager itemManager) {
		this.itemManager = itemManager;
	}

	public String resolve(final int itemId)
	{
		String name = nameCache.get(itemId);
		if (name != null)
		{
			return name;
		}
		name = fetchName(itemId);
		if (name != null)
		{
			nameCache.put(itemId, name);
		}
		return name;
	}

	public long resolvePrice(final int itemId)
	{
		final Long cached = priceCache.get(itemId);
		if (cached != null)
		{
			return cached;
		}
		final long fetched = fetchPrice(itemId);
		priceCache.put(itemId, fetched);
		return fetched;
	}

	public boolean isPlaceholder(final int itemId)
	{
		try
		{
			return itemManager.getItemComposition(itemId).getPlaceholderTemplateId() != -1;
		} catch (final RuntimeException t)
		{
			return false;
		}
	}

	public void clear()
	{
		nameCache.clear();
		priceCache.clear();
	}

	private String fetchName(final int itemId)
	{
		try
		{
			return itemManager.getItemComposition(itemId).getName();
		} catch (final RuntimeException t)
		{
			return null;
		}
	}

	private long fetchPrice(final int itemId)
	{
		try
		{
			return itemManager.getItemPrice(itemId);
		} catch (final RuntimeException t)
		{
			return 0;
		}
	}
}
