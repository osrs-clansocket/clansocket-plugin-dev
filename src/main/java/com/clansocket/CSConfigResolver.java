package com.clansocket;

import com.clansocket.util.Strings;

public final class CSConfigResolver
{
	private CSConfigResolver() {
	}

	public static String serverWsUrl(final ClanSocketConfig config)
	{
		final String raw = config.serverWsUrl();
		final String picked = Strings.isBlank(raw) ? ClanSocketConstants.SERVER_URL : raw.trim();
		final String stripped = picked.endsWith("/") ? picked.substring(0, picked.length() - 1) : picked;
		return stripped.contains(ClanSocketConstants.SCHEME_DELIM)
		        ? stripped
		        : ClanSocketConstants.SCHEME_WSS + stripped;
	}
}
