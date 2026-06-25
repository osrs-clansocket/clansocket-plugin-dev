package com.clansocket.tracking.progression.collectionlog;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.RuneScapeProfileChanged;

import com.clansocket.ClanSocketConfig;
import com.clansocket.bus.AbstractStateTracker;
import com.clansocket.bus.primitive.IntIntMap;
import com.clansocket.tracking.inventory.ItemNames;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class CollectionLogSnapshotTracker extends AbstractStateTracker
{
	@Inject
	private ClanSocketConfig config;
	@Inject
	private ItemNames itemNames;

	private final IntIntMap accumulator = new IntIntMap();
	private int lastItemTick = -1;
	private boolean autoScrapeFired;

	@Override
	protected void onLoginScreen()
	{
		reset();
	}

	@Override
	protected void onOtherGameState(final GameState state)
	{
		if (state != GameState.HOPPING)
		{
			reset();
		}
	}

	@Subscribe
	public void onRuneScapeProfileChanged(final RuneScapeProfileChanged event)
	{
		reset();
	}

	@Subscribe
	public void onVarbitChanged(final VarbitChanged event)
	{
		if (event.getVarbitId() == VarbitID.COLLECTION_POH_HOST_BOOK_OPEN && isAdventureLogOpen())
		{
			log.debug("ClanSocket clog: adventure log opened mid-scrape, discarding partial accumulator");
			reset();
		}
	}

	@Subscribe
	public void onScriptPreFired(final ScriptPreFired event)
	{
		if (event.getScriptId() != CollectionLogConstants.ITEM_SCRIPT_ID || isAdventureLogOpen()
		        || !config.streamCollectionLog())
		{
			return;
		}
		final int[] item = extractScriptArgs(event);
		if (item == null)
		{
			return;
		}
		accumulator.put(item[0], item[1]);
		lastItemTick = client.getTickCount();
	}

	@Subscribe
	public void onScriptPostFired(final ScriptPostFired event)
	{
		if (event.getScriptId() != CollectionLogConstants.SETUP_SCRIPT_ID)
		{
			return;
		}
		if (isAdventureLogOpen())
		{
			accumulator.clear();
			return;
		}
		if (autoScrapeFired || !config.streamCollectionLog())
		{
			return;
		}
		autoScrapeFired = true;
		client.menuAction(-1, CollectionLogConstants.AUTO_SCRAPE_WIDGET_ID, MenuAction.CC_OP, 1, -1, "Search", null);
		client.runScript(CollectionLogConstants.AUTO_SCRAPE_RUN_SCRIPT_ID);
	}

	@Subscribe
	public void onGameTick(final GameTick tick)
	{
		if (lastItemTick < 0 || accumulator.isEmpty()
		        || client.getTickCount() - lastItemTick < CollectionLogConstants.FLUSH_QUIESCENCE_TICKS)
		{
			return;
		}
		if (!config.streamCollectionLog())
		{
			reset();
			return;
		}
		batcher.enqueue(CollectionLogSnapshotBuilder.build(accumulator, itemNames));
		log.debug("ClanSocket clog snapshot flushed: {} items", accumulator.size());
		reset();
	}

	private void reset()
	{
		accumulator.clear();
		lastItemTick = -1;
		autoScrapeFired = false;
	}

	private boolean isAdventureLogOpen()
	{
		return client.getVarbitValue(VarbitID.COLLECTION_POH_HOST_BOOK_OPEN) == 1;
	}

	@SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
	private static int[] extractScriptArgs(final ScriptPreFired event)
	{
		final Object[] args = event.getScriptEvent() == null ? null : event.getScriptEvent().getArguments();
		if (args == null || args.length < CollectionLogConstants.MIN_SCRIPT_ARGS
		        || !(args[CollectionLogConstants.ARG_INDEX_ITEM_ID] instanceof Integer)
		        || !(args[CollectionLogConstants.ARG_INDEX_QUANTITY] instanceof Integer))
		{
			return null;
		}
		final int qty = (Integer) args[CollectionLogConstants.ARG_INDEX_QUANTITY];
		return qty <= 0 ? null : new int[]{(Integer) args[CollectionLogConstants.ARG_INDEX_ITEM_ID], qty};
	}
}
