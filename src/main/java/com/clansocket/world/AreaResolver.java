package com.clansocket.world;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public final class AreaResolver
{
	private final Gson gson;
	private Map<Long, String> regionToArea;

	@Inject
	AreaResolver(final Gson gson) {
		this.gson = gson;
	}

	public String resolve(final int regionId)
	{
		if (regionToArea == null)
		{
			regionToArea = load();
		}
		return regionToArea.get(regionKey(regionId >> WorldConstants.RX_SHIFT, regionId & WorldConstants.RY_MASK));
	}

	private static long regionKey(final int rx, final int ry)
	{
		return (((long) rx) << WorldConstants.PACK_SHIFT) | (ry & WorldConstants.PACK_MASK);
	}

	@SuppressWarnings("checkstyle:IllegalCatch")
	private Map<Long, String> load()
	{
		final Map<Long, String> out = new HashMap<>();
		try (InputStream is = AreaResolver.class.getResourceAsStream("/" + WorldConstants.LOCATIONS_RESOURCE))
		{
			if (is == null)
			{
				log.warn("ClanSocket area lookup: {} not found on classpath; area names disabled",
				        WorldConstants.LOCATIONS_RESOURCE);
				return out;
			}
			populate(out, is);
		} catch (final IOException | JsonParseException t)
		{
			log.warn("ClanSocket area lookup: load failed: {}", t.getMessage());
		}
		log.info("ClanSocket area lookup: loaded {} region→area mappings", out.size());
		return out;
	}

	private void populate(final Map<Long, String> out, final InputStream is) throws IOException
	{
		try (Reader reader = new InputStreamReader(is))
		{
			final Type type = new TypeToken<Map<String, List<List<Integer>>>>()
			{
			}.getType();
			final Map<String, List<List<Integer>>> data = gson.fromJson(reader, type);
			if (data == null)
			{
				return;
			}
			for (final Map.Entry<String, List<List<Integer>>> entry : data.entrySet())
			{
				addEntries(out, entry.getKey(), entry.getValue());
			}
		}
	}

	private static void addEntries(final Map<Long, String> out, final String name, final List<List<Integer>> chunks)
	{
		for (final List<Integer> chunk : chunks)
		{
			if (chunk == null || chunk.size() != WorldConstants.CHUNK_PAIR_SIZE)
			{
				continue;
			}
			out.put(regionKey(chunk.get(0), chunk.get(1)), name);
		}
	}
}
