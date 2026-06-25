package com.clansocket.util;

import com.clansocket.ClanSocketConstants;

public final class Urls
{
	private Urls() {
	}

	public static String host(final String url)
	{
		final String afterScheme = stripScheme(url);
		if (Strings.isEmpty(afterScheme))
		{
			return afterScheme;
		}
		final int slash = afterScheme.indexOf('/');
		return slash < 0 ? afterScheme : afterScheme.substring(0, slash);
	}

	private static String stripScheme(final String url)
	{
		if (Strings.isEmpty(url))
		{
			return url;
		}
		final int delim = url.indexOf(ClanSocketConstants.SCHEME_DELIM);
		return delim < 0 ? url : url.substring(delim + ClanSocketConstants.SCHEME_DELIM_LEN);
	}
}
