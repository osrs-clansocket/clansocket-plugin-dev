package com.clansocket.tracking.identity;

import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Client;
import net.runelite.api.clan.ClanChannel;
import net.runelite.api.clan.ClanID;
import net.runelite.api.clan.ClanMember;
import net.runelite.api.clan.ClanRank;
import net.runelite.api.clan.ClanSettings;
import net.runelite.api.clan.ClanTitle;

import com.clansocket.util.Strings;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public final class ClanResolver
{
	private final Client client;

	@Inject
	public ClanResolver(final Client client) {
		this.client = client;
	}

	public ClanInfo resolve(final String rsn)
	{
		return safe(() ->
		{
			final ClanSettings clan = client.getClanSettings(ClanID.CLAN);
			if (clan == null)
			{
				return ClanInfo.EMPTY;
			}
			final ClanMember me = clan.findMember(rsn);
			final String rank = resolveRank(clan, me);
			final String joinedAt = me != null && me.getJoinDate() != null ? me.getJoinDate().toString() : null;
			final Integer memberCount = clan.getMembers() != null ? clan.getMembers().size() : null;
			final ClanChannel channel = client.getClanChannel(ClanID.CLAN);
			final Integer onlineCount = channel != null && channel.getMembers() != null
			        ? channel.getMembers().size()
			        : null;
			return new ClanInfo(clan.getName(), rank, joinedAt, memberCount, onlineCount);
		}, ClanInfo.EMPTY, "clan resolve");
	}

	public String memberRank(final String rsn)
	{
		return safe(() ->
		{
			final ClanSettings clan = client.getClanSettings(ClanID.CLAN);
			return clan == null ? null : resolveRank(clan, clan.findMember(rsn));
		}, null, "clan rank lookup");
	}

	public String currentClanName()
	{
		return safe(() ->
		{
			final ClanChannel channel = client.getClanChannel();
			if (channel != null && channel.getName() != null)
			{
				return channel.getName();
			}
			final ClanSettings settings = client.getClanSettings(ClanID.CLAN);
			return settings == null ? null : settings.getName();
		}, null, "clan name lookup");
	}

	public static String rankTitle(final ClanSettings clan, final ClanRank rank)
	{
		if (rank == null)
		{
			return null;
		}
		final ClanTitle title = clan.titleForRank(rank);
		return title != null && Strings.isNotEmpty(title.getName()) ? title.getName() : "rank-" + rank.getRank();
	}

	private static String resolveRank(final ClanSettings clan, final ClanMember me)
	{
		return me == null ? null : rankTitle(clan, me.getRank());
	}

	@SuppressWarnings("checkstyle:IllegalCatch")
	private static <T> T safe(final Supplier<T> action, final T fallback, final String label)
	{
		try
		{
			return action.get();
		} catch (final RuntimeException t)
		{
			log.debug("ClanSocket {} failed", label, t);
			return fallback;
		}
	}
}
