package com.clansocket.tracking.clan;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.clan.ClanID;
import net.runelite.api.clan.ClanMember;
import net.runelite.api.clan.ClanSettings;

import com.clansocket.bus.Hashes;
import com.clansocket.bus.primitive.AbstractSnapshotEmitter;
import com.clansocket.protocol.common.Payload;
import com.clansocket.protocol.identity.ClanRosterMember;
import com.clansocket.tracking.identity.ClanResolver;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
final class ClanRosterEmitter extends AbstractSnapshotEmitter
{
	@Inject
	ClanRosterEmitter() {
		super("members", "roster", log);
	}

	@Override
	protected Optional<Payload> buildSnapshot()
	{
		final ClanSettings clan = client.getClanSettings(ClanID.CLAN);
		if (clan == null || clan.getMembers() == null)
		{
			return Optional.empty();
		}
		final List<ClanRosterMember> members = buildMembers(clan);
		final String fingerprint = Hashes
		        .of(members.stream().map(m -> m.name + "|" + m.rank + "|" + m.joinedAt).toArray());
		return Optional.of(
		        new Payload("clan_roster", "clanName", clan.getName(), "fingerprint", fingerprint, "members", members));
	}

	private static List<ClanRosterMember> buildMembers(final ClanSettings clan)
	{
		final List<ClanMember> raw = clan.getMembers();
		final List<ClanRosterMember> out = new ArrayList<>(raw.size());
		for (final ClanMember member : raw)
		{
			out.add(new ClanRosterMember(member.getName(), ClanResolver.rankTitle(clan, member.getRank()),
			        joinIso(member)));
		}
		return Collections.unmodifiableList(out);
	}

	private static String joinIso(final ClanMember member)
	{
		final LocalDate join = member.getJoinDate();
		return join != null ? join.toString() : null;
	}
}
