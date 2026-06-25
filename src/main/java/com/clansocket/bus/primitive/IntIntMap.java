package com.clansocket.bus.primitive;

import java.util.Arrays;

public final class IntIntMap
{
	@SuppressWarnings("checkstyle:StaticFinalOutsideConstants")
	public static final int FREE_KEY = Integer.MIN_VALUE;
	@SuppressWarnings("checkstyle:StaticFinalOutsideConstants")
	static final int DEFAULT_CAPACITY = 16;
	@SuppressWarnings("checkstyle:StaticFinalOutsideConstants")
	private static final int MIX_C1 = 0x85ebca6b;
	@SuppressWarnings("checkstyle:StaticFinalOutsideConstants")
	private static final int SHIFT_HIGH = 16;
	@SuppressWarnings("checkstyle:StaticFinalOutsideConstants")
	private static final int SHIFT_MID = 13;

	private int[] keys;
	private int[] values;
	private int size;
	private final int missValue;

	public IntIntMap() {
		this(DEFAULT_CAPACITY, 0);
	}

	public IntIntMap(final int initialCapacity, final int missValue) {
		int cap = 1;
		final int target = Math.max(initialCapacity, 1) * 2;
		while (cap < target)
		{
			cap <<= 1;
		}
		this.keys = new int[cap];
		this.values = new int[cap];
		Arrays.fill(this.keys, FREE_KEY);
		this.missValue = missValue;
	}

	public int get(final int key)
	{
		final int i = slot(key);
		return keys[i] == FREE_KEY ? missValue : values[i];
	}

	public void put(final int key, final int value)
	{
		final int i = slot(key);
		final boolean isNew = keys[i] == FREE_KEY;
		keys[i] = key;
		values[i] = value;
		if (isNew && ++size * 2 > keys.length)
		{
			resize();
		}
	}

	public int size()
	{
		return size;
	}

	public boolean isEmpty()
	{
		return size == 0;
	}

	public void clear()
	{
		Arrays.fill(keys, FREE_KEY);
		size = 0;
	}

	public int[] keys()
	{
		final int[] out = new int[size];
		int j = 0;
		for (final int k : keys)
		{
			if (k != FREE_KEY)
			{
				out[j++] = k;
			}
		}
		return out;
	}

	private int slot(final int key)
	{
		int h = key;
		h ^= h >>> SHIFT_HIGH;
		h *= MIX_C1;
		h ^= h >>> SHIFT_MID;
		int i = h & (keys.length - 1);
		while (keys[i] != FREE_KEY && keys[i] != key)
		{
			i = (i + 1) & (keys.length - 1);
		}
		return i;
	}

	private void resize()
	{
		final int[] oldKeys = keys;
		final int[] oldValues = values;
		keys = new int[oldKeys.length * 2];
		values = new int[oldValues.length * 2];
		Arrays.fill(keys, FREE_KEY);
		size = 0;
		for (int i = 0; i < oldKeys.length; i++)
		{
			if (oldKeys[i] != FREE_KEY)
			{
				put(oldKeys[i], oldValues[i]);
			}
		}
	}
}
