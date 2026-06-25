package com.clansocket.tracking.clan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.clan.ClanID;
import net.runelite.api.clan.ClanRank;
import net.runelite.api.clan.ClanSettings;
import net.runelite.api.clan.ClanTitle;

import com.clansocket.bus.Hashes;
import com.clansocket.bus.primitive.AbstractSnapshotEmitter;
import com.clansocket.protocol.common.Payload;
import com.clansocket.protocol.identity.ClanTitleEntry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
final class ClanTitlesEmitter extends AbstractSnapshotEmitter
{
	@Inject
	ClanTitlesEmitter() {
		super("titles", "titles", log);
	}

	@Override
	protected Optional<Payload> buildSnapshot()
	{
		final ClanSettings clan = client.getClanSettings(ClanID.CLAN);
		if (clan == null)
		{
			return Optional.empty();
		}
		final List<ClanTitleEntry> titles = collectTitles(clan);
		if (titles.isEmpty())
		{
			return Optional.empty();
		}
		final String fingerprint = Hashes
		        .of(titles.stream().map(t -> t.rank + "|" + t.titleId + "|" + t.title).toArray());
		return Optional.of(new Payload("clan_titles_snapshot", "clanName", clan.getName(), "fingerprint", fingerprint,
		        "titles", titles));
	}

	private static List<ClanTitleEntry> collectTitles(final ClanSettings clan)
	{
		final List<ClanTitleEntry> out = new ArrayList<>();
		final Set<Integer> seenIds = new HashSet<>();
		for (int r = ClanConstants.RANK_MIN; r <= ClanConstants.RANK_MAX; r++)
		{
			final ClanTitle title = clan.titleForRank(new ClanRank(r));
			if (title == null)
			{
				continue;
			}
			if (seenIds.add(title.getId()))
			{
				out.add(new ClanTitleEntry(r, title.getId(), title.getName()));
			}
		}
		return Collections.unmodifiableList(out);
	}
}
