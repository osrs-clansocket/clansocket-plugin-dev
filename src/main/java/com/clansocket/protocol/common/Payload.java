package com.clansocket.protocol.common;

import java.util.LinkedHashMap;

public final class Payload extends LinkedHashMap<String, Object>
{
	public Payload(final String type, final Object... keyValuePairs) {
		super.put("type", type);
		for (int i = 0; i + 1 < keyValuePairs.length; i += 2)
		{
			super.put((String) keyValuePairs[i], keyValuePairs[i + 1]);
		}
	}

	public String type()
	{
		return (String) get("type");
	}
}
