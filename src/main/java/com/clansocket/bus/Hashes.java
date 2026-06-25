package com.clansocket.bus;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public final class Hashes
{
	@SuppressWarnings("checkstyle:StaticFinalOutsideConstants")
	public static final long HASH_PRIME = 31L;

	private Hashes() {
	}

	public static String of(final Object... parts)
	{
		return Integer.toHexString(Arrays.deepHashCode(parts));
	}

	public static <T> String ofMapped(final List<T> items, final Function<T, Object[]> projection)
	{
		return of(items.stream().map(projection).flatMap(Arrays::stream).toArray());
	}

	public static <T> long pairsLong(final List<T> items, final ToIntFunction<T> a, final ToIntFunction<T> b)
	{
		long h = 1L;
		for (final T item : items)
		{
			h = h * HASH_PRIME + a.applyAsInt(item);
			h = h * HASH_PRIME + b.applyAsInt(item);
		}
		return h;
	}

	public static long rollingArrays(final int seed, final int[]... arrays)
	{
		long h = seed;
		if (arrays.length == 0)
		{
			return h;
		}
		final int len = arrays[0].length;
		for (int i = 0; i < len; i++)
		{
			for (final int[] arr : arrays)
			{
				h = h * HASH_PRIME + arr[i];
			}
		}
		return h;
	}

	public static long rollingLong(final int... values)
	{
		long h = 1L;
		for (final int v : values)
		{
			h = h * HASH_PRIME + v;
		}
		return h;
	}
}
