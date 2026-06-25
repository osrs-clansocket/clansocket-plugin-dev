package com.clansocket.util;

import com.google.gson.JsonObject;

public final class Json
{
	public static String optString(final JsonObject obj, final String key)
	{
		return obj.has(key) ? obj.get(key).getAsString() : "";
	}

	private Json() {
	}
}
