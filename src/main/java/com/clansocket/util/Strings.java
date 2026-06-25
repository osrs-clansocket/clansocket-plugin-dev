package com.clansocket.util;

public final class Strings
{
	private Strings() {
	}

	public static boolean isEmpty(final String s)
	{
		return s == null || s.isEmpty();
	}

	public static boolean isNotEmpty(final String s)
	{
		return s != null && !s.isEmpty();
	}

	public static boolean isBlank(final String s)
	{
		return s == null || s.isBlank();
	}
}
