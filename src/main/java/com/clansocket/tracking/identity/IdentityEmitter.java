package com.clansocket.tracking.identity;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.WorldType;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.game.WorldService;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldResult;

import com.clansocket.ClanSocket;
import com.clansocket.ClanSocketConstants;
import com.clansocket.bus.EventBatcher;
import com.clansocket.chat.GameChatEmitter;
import com.clansocket.protocol.common.Payload;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
final class IdentityEmitter
{
	@Inject
	private Client client;
	@Inject
	private EventBatcher batcher;
	@Inject
	private ClanSocket socket;
	@Inject
	private GameChatEmitter chatEmitter;
	@Inject
	private ClanResolver clanResolver;
	@Inject
	private WorldService worldService;

	boolean emit(final Player local, final String sessionStartIso, final boolean sessionReminderSent)
	{
		final long accountHashLong = client.getAccountHash();
		if (accountHashLong == -1L)
		{
			return sessionReminderSent;
		}
		final Payload payload = buildIdentity(local, sessionStartIso, accountHashLong);
		batcher.enqueue(payload);
		if (!sessionReminderSent)
		{
			chatEmitter.sessionReminder((String) payload.get("clanName"), (String) payload.get("clanRank"),
			        socket.isConnected());
			return true;
		}
		return sessionReminderSent;
	}

	private Payload buildIdentity(final Player local, final String sessionStartIso, final long accountHashLong)
	{
		final String rsn = local.getName();
		final int world = client.getWorld();
		final List<String> worldTypeNames = client.getWorldType().stream().map(WorldType::name)
		        .collect(Collectors.toUnmodifiableList());
		final ClanInfo clan = clanResolver.resolve(rsn);
		final Payload payload = new Payload("identity", "rsn", rsn, "accountHash",
		        Long.toUnsignedString(accountHashLong), "accountType", resolveAccountType(), "world", world,
		        "worldTypes", worldTypeNames, "activity", resolveActivity(world), "clanName", clan.name, "clanRank",
		        clan.rank, "clanJoinedAt", clan.joinedAt, "clanMemberCount", clan.memberCount, "clanOnlineCount",
		        clan.onlineCount, "sessionStart", sessionStartIso, "pluginVersion", ClanSocketConstants.PLUGIN_VERSION,
		        "schemaVersion", ClanSocketConstants.PROTOCOL_VERSION);
		log.debug("ClanSocket identity rsn={} acct={} world={} clan={}/{} joined={} members={}/{} types={}", rsn,
		        payload.get("accountHash"), world, clan.name, clan.rank, clan.joinedAt, clan.memberCount,
		        clan.onlineCount, worldTypeNames);
		return payload;
	}

	private String resolveAccountType()
	{
		final int idx = client.getVarbitValue(VarbitID.IRONMAN);
		if (idx >= 0 && idx < IdentityConstants.ACCOUNT_TYPE_NAMES.length)
		{
			return IdentityConstants.ACCOUNT_TYPE_NAMES[idx];
		}
		return "UNKNOWN_" + idx;
	}

	private String resolveActivity(final int currentWorld)
	{
		final WorldResult result = worldService.getWorlds();
		if (result == null)
		{
			return null;
		}
		final World w = result.findWorld(currentWorld);
		return w != null ? w.getActivity() : null;
	}
}
