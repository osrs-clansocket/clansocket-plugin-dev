package com.clansocket.tracking.progression;

import java.util.Locale;
import java.util.Optional;

import com.clansocket.protocol.common.Payload;
import com.clansocket.protocol.common.Where;

final class ClueCompletionParser
{
	private ClueCompletionParser() {
	}

	static Optional<Payload> parse(final String text, final Where where)
	{
		if (text == null || !text.startsWith(ProgressionConstants.CLUE_PREFIX))
		{
			return Optional.empty();
		}
		final String suffix = matchSuffix(text);
		if (suffix == null)
		{
			return Optional.empty();
		}
		final int tierEnd = text.length() - suffix.length();
		final int[] parsedDigits = readDigits(text, ProgressionConstants.CLUE_PREFIX.length(), tierEnd);
		if (parsedDigits == null)
		{
			return Optional.empty();
		}
		final String tier = extractTier(text, parsedDigits[1], tierEnd);
		if (tier == null)
		{
			return Optional.empty();
		}
		return Optional.of(new Payload("clue_completed", "tier", tier.toUpperCase(Locale.ROOT), "total",
		        parsedDigits[0], "cluesCompletedBefore", Math.max(0, parsedDigits[0] - 1), "where", where));
	}

	private static String extractTier(final String text, final int afterDigits, final int tierEnd)
	{
		if (afterDigits >= tierEnd || text.charAt(afterDigits) != ' ')
		{
			return null;
		}
		final String tier = text.substring(afterDigits + 1, tierEnd);
		return ProgressionConstants.CLUE_TIERS.contains(tier) ? tier : null;
	}

	private static String matchSuffix(final String text)
	{
		if (text.endsWith(ProgressionConstants.CLUE_SUFFIX_PLURAL))
		{
			return ProgressionConstants.CLUE_SUFFIX_PLURAL;
		}
		if (text.endsWith(ProgressionConstants.CLUE_SUFFIX_SINGULAR))
		{
			return ProgressionConstants.CLUE_SUFFIX_SINGULAR;
		}
		return null;
	}

	@SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
	private static int[] readDigits(final String text, final int start, final int end)
	{
		int total = 0;
		int i = start;
		while (i < end)
		{
			final char c = text.charAt(i);
			if (c < '0' || c > '9')
			{
				break;
			}
			total = total * ProgressionConstants.DECIMAL_BASE + (c - '0');
			i++;
		}
		if (i == start)
		{
			return null;
		}
		return new int[]{total, i};
	}
}
