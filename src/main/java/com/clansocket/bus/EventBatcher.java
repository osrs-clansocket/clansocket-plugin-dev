package com.clansocket.bus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;

import com.clansocket.ClanSocket;
import com.clansocket.panel.PanelStats;
import com.clansocket.panel.widgets.StreamGate;
import com.clansocket.protocol.common.Payload;

@Singleton
public class EventBatcher
{
	@SuppressWarnings("checkstyle:StaticFinalOutsideConstants")
	private static final Map<String, StreamGate> GATE_BY_TYPE = Map.<String, StreamGate>ofEntries(
	        Map.entry("stats", StreamGate.SKILLS_SNAPSHOT), Map.entry("xp_gained", StreamGate.XP_GAINS),
	        Map.entry("level_up", StreamGate.LEVEL_UPS), Map.entry("damage_dealt", StreamGate.COMBAT),
	        Map.entry("damage_taken", StreamGate.COMBAT), Map.entry("death", StreamGate.DEATH),
	        Map.entry("interacting", StreamGate.COMBAT), Map.entry("slayer", StreamGate.SLAYER),
	        Map.entry("vitals", StreamGate.VITALS), Map.entry("prayers", StreamGate.PRAYER),
	        Map.entry("boosts", StreamGate.BOOSTS), Map.entry("status_effect", StreamGate.STATUS_EFFECTS),
	        Map.entry("location", StreamGate.LOCATION), Map.entry("world_hop", StreamGate.LOCATION),
	        Map.entry("container", StreamGate.INVENTORY), Map.entry("container_delta", StreamGate.INVENTORY),
	        Map.entry("bank_open", StreamGate.BANK), Map.entry("bank_close", StreamGate.BANK),
	        Map.entry("rune_pouch", StreamGate.RUNE_POUCH), Map.entry("loot", StreamGate.LOOT),
	        Map.entry("pet_drop", StreamGate.PET_DROPS), Map.entry("quests", StreamGate.QUESTS),
	        Map.entry("quest_completed", StreamGate.QUESTS), Map.entry("diaries", StreamGate.DIARIES),
	        Map.entry("diary_completed", StreamGate.DIARIES), Map.entry("clue_completed", StreamGate.CLUES),
	        Map.entry("clue_opened", StreamGate.CLUES), Map.entry("collection_log_entry", StreamGate.COLLECTION_LOG),
	        Map.entry("collection_log_snapshot", StreamGate.COLLECTION_LOG),
	        Map.entry("combat_achievements_catalog", StreamGate.COMBAT_ACHIEVEMENTS),
	        Map.entry("combat_achievements_snapshot", StreamGate.COMBAT_ACHIEVEMENTS),
	        Map.entry("combat_achievement_completed", StreamGate.COMBAT_ACHIEVEMENTS),
	        Map.entry("menu_action", StreamGate.MENU_ACTIONS), Map.entry("farming_patch", StreamGate.FARMING),
	        Map.entry("chat", StreamGate.CLAN_CHAT));

	@Inject
	private ClanSocket socket;
	@Inject
	private Client client;
	@Inject
	private PanelStats panelStats;

	private final List<Payload> pending = new ArrayList<>();
	private long nextSeq = 1L;

	public void enqueue(final Payload event)
	{
		final StreamGate gate = GATE_BY_TYPE.get(event.type());
		if (gate != null)
		{
			panelStats.bump(gate);
		}
		synchronized (pending)
		{
			pending.add(event);
		}
	}

	@Subscribe(priority = -1f)
	public void onGameTick(final GameTick tick)
	{
		flush();
	}

	@Subscribe
	public void onGameStateChanged(final GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			synchronized (pending)
			{
				pending.clear();
			}
			nextSeq = 1L;
		}
	}

	private void flush()
	{
		final List<Payload> toSend;
		synchronized (pending)
		{
			if (pending.isEmpty())
			{
				return;
			}
			toSend = new ArrayList<>(pending);
			pending.clear();
		}
		socket.send(new Payload("batch", "seq", nextSeq++, "tick", client.getTickCount(), "events", toSend));
	}
}
