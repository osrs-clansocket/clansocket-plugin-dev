package com.clansocket.tracking.social;

import com.clansocket.util.Strings;

public final class ChatTextSanitizer
{
	private ChatTextSanitizer() {
	}

	public static String sanitize(final String input)
	{
		if (Strings.isEmpty(input))
		{
			return "";
		}
		final String stripped = stripMetaPrefix(input);
		final StringBuilder out = new StringBuilder(stripped.length());
		walk(stripped, out);
		return out.toString().trim();
	}

	private static String stripMetaPrefix(final String input)
	{
		if (!input.startsWith(SocialConstants.CA_ID_PREFIX))
		{
			return input;
		}
		final int pipe = input.indexOf(SocialConstants.META_DELIMITER);
		return pipe < 0 ? input : input.substring(pipe + 1);
	}

	private static void walk(final String input, final StringBuilder out)
	{
		final int len = input.length();
		int i = 0;
		while (i < len)
		{
			final char c = input.charAt(i);
			if (c == SocialConstants.NBSP)
			{
				out.append(' ');
				i++;
			} else if (c == SocialConstants.ANGLE_OPEN)
			{
				i = handleAngle(input, i, out);
			} else
			{
				out.append(c);
				i++;
			}
		}
	}

	private static int handleAngle(final String input, final int openIdx, final StringBuilder out)
	{
		final int close = input.indexOf(SocialConstants.ANGLE_CLOSE, openIdx + 1);
		if (close < 0)
		{
			out.append(SocialConstants.ANGLE_OPEN);
			return openIdx + 1;
		}
		final String inner = input.substring(openIdx + 1, close);
		if (SocialConstants.LT_ENTITY.equals(inner))
		{
			out.append(SocialConstants.ANGLE_OPEN);
			return close + 1;
		}
		if (SocialConstants.GT_ENTITY.equals(inner))
		{
			out.append(SocialConstants.ANGLE_CLOSE);
			return close + 1;
		}
		if (isStrippableTag(inner))
		{
			return close + 1;
		}
		out.append(SocialConstants.ANGLE_OPEN);
		return openIdx + 1;
	}

	private static boolean isStrippableTag(final String inner)
	{
		return inner.startsWith(SocialConstants.IMG_PREFIX) || inner.startsWith(SocialConstants.COL_PREFIX)
		        || SocialConstants.COL_CLOSE.equals(inner);
	}
}
