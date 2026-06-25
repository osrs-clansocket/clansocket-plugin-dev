package com.clansocket.util;

import java.util.function.ToIntFunction;

public final class Money
{
	private Money() {
	}

	public static <T> long sumGp(final Iterable<T> items, final ToIntFunction<T> qty, final ToIntFunction<T> price)
	{
		long total = 0L;
		for (final T it : items)
		{
			total += (long) qty.applyAsInt(it) * price.applyAsInt(it);
		}
		return total;
	}
}
