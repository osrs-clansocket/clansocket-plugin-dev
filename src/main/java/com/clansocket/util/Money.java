package com.clansocket.util;

import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

public final class Money
{
	private Money() {
	}

	public static <T> long sumGp(final Iterable<T> items, final ToIntFunction<T> qty, final ToLongFunction<T> price)
	{
		long total = 0L;
		for (final T it : items)
		{
			total += qty.applyAsInt(it) * price.applyAsLong(it);
		}
		return total;
	}
}
