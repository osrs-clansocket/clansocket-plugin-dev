package com.clansocket.util;

import java.util.function.Function;

public final class NumberInput
{
	private NumberInput() {
	}

	public static int parseIntOr(final String s, final int fallback)
	{
		return parseOr(s, Integer::parseInt, fallback);
	}

	private static <T> T parseOr(final String s, final Function<String, T> parser, final T fallback)
	{
		if (Strings.isBlank(s))
		{
			return fallback;
		}
		try
		{
			return parser.apply(s.trim());
		} catch (final NumberFormatException ignored)
		{
			return fallback;
		}
	}
}
