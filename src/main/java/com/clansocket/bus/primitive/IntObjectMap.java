package com.clansocket.bus.primitive;

import java.util.Arrays;

public final class IntObjectMap<V>
{
	@SuppressWarnings("checkstyle:StaticFinalOutsideConstants")
	private static final int UNSET = -1;

	private final IntIntMap keyToSlot;
	private Object[] values;
	private int size;

	public IntObjectMap() {
		this(IntIntMap.DEFAULT_CAPACITY);
	}

	public IntObjectMap(final int initialCapacity) {
		this.keyToSlot = new IntIntMap(initialCapacity, UNSET);
		this.values = new Object[Math.max(initialCapacity, 1)];
	}

	@SuppressWarnings("unchecked")
	public V get(final int key)
	{
		final int slot = keyToSlot.get(key);
		return slot == UNSET ? null : (V) values[slot];
	}

	public void put(final int key, final V value)
	{
		int slot = keyToSlot.get(key);
		if (slot == UNSET)
		{
			slot = size++;
			if (slot >= values.length)
			{
				values = Arrays.copyOf(values, values.length * 2);
			}
			keyToSlot.put(key, slot);
		}
		values[slot] = value;
	}

	public void clear()
	{
		keyToSlot.clear();
		Arrays.fill(values, null);
		size = 0;
	}
}
