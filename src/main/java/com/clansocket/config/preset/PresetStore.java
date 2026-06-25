package com.clansocket.config.preset;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalInt;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public final class PresetStore
{
	private final PresetCodec codec;

	@Inject
	public PresetStore(final PresetCodec codec) {
		this.codec = codec;
		ensureDir();
	}

	public boolean existsForSlot(final int slot)
	{
		return slot >= 1 && slot <= PresetConstants.MAX_SLOTS && pathFor(slot).toFile().exists();
	}

	public Optional<PresetSchema> read(final int slot) throws IOException
	{
		if (!existsForSlot(slot))
		{
			return Optional.empty();
		}
		final String json = Files.readString(pathFor(slot), StandardCharsets.UTF_8);
		return Optional.of(codec.decode(json));
	}

	public void write(final int slot, final PresetSchema schema) throws IOException
	{
		ensureDir();
		Files.writeString(pathFor(slot), codec.encode(schema), StandardCharsets.UTF_8);
	}

	public boolean delete(final int slot)
	{
		return pathFor(slot).toFile().delete();
	}

	public OptionalInt findMatchingSlot(final String targetHash)
	{
		for (int i = 1; i <= PresetConstants.MAX_SLOTS; i++)
		{
			if (!existsForSlot(i))
			{
				continue;
			}
			try
			{
				final Optional<PresetSchema> opt = read(i);
				if (opt.isPresent() && codec.canonicalHash(opt.get()).equals(targetHash))
				{
					return OptionalInt.of(i);
				}
			} catch (final IOException | IllegalArgumentException ex)
			{
				log.debug("ClanSocket preset: skipping corrupted slot {}: {}", i, ex.getMessage());
			}
		}
		return OptionalInt.empty();
	}

	public OptionalInt findNextEmptySlot()
	{
		for (int i = 1; i <= PresetConstants.MAX_SLOTS; i++)
		{
			if (!existsForSlot(i))
			{
				return OptionalInt.of(i);
			}
		}
		return OptionalInt.empty();
	}

	private static Path pathFor(final int slot)
	{
		return PresetConstants.PRESETS_DIR.toPath().resolve(String.format(PresetConstants.FILENAME_PATTERN, slot));
	}

	private static void ensureDir()
	{
		PresetConstants.PRESETS_DIR.mkdirs();
	}
}
