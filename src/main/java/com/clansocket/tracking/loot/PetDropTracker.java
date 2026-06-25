package com.clansocket.tracking.loot;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.eventbus.Subscribe;

import com.clansocket.ClanSocketConfig;
import com.clansocket.bus.GameChatTracker;
import com.clansocket.protocol.common.Payload;
import com.clansocket.tracking.combat.InteractionState;
import com.clansocket.tracking.movement.LocationContext;
import com.clansocket.tracking.progression.collectionlog.CollectionLogConstants;
import com.clansocket.tracking.social.ChatTextSanitizer;

@Singleton
public class PetDropTracker extends GameChatTracker
{
	@Inject
	private ClanSocketConfig config;
	@Inject
	private LocationContext locationContext;
	@Inject
	private PetNameResolver petNameResolver;
	@Inject
	private InteractionState interactionState;

	private String pendingTrigger;
	private String pendingMessage;
	private Set<Integer> preTriggerPets;
	private int pendingTriggerTick;

	@Override
	protected void handleGameChat(final String text)
	{
		if (!config.streamPetDrops())
		{
			return;
		}
		if (pendingTrigger != null && (tryConsumeUntradeableDrop(text) || tryConsumeCollectionLogPet(text)))
		{
			return;
		}
		final String trigger = PetTriggerClassifier.classify(text);
		if (trigger == null)
		{
			return;
		}
		pendingTrigger = trigger;
		pendingMessage = text;
		pendingTriggerTick = client.getTickCount();
		preTriggerPets = PetInventoryScanner.snapshot(client);
	}

	private boolean tryConsumeUntradeableDrop(final String text)
	{
		final int idx = text.indexOf(LootConstants.UNTRADEABLE_DROP_PREFIX);
		if (idx < 0)
		{
			return false;
		}
		final String name = text.substring(idx + LootConstants.UNTRADEABLE_DROP_PREFIX.length()).trim();
		if (name.isEmpty())
		{
			return false;
		}
		flush(name, petNameResolver.resolve(name));
		return true;
	}

	private boolean tryConsumeCollectionLogPet(final String text)
	{
		final String stripped = ChatTextSanitizer.sanitize(text);
		if (!stripped.startsWith(CollectionLogConstants.CHAT_PREFIX))
		{
			return false;
		}
		final String name = stripped.substring(CollectionLogConstants.CHAT_PREFIX.length()).trim();
		final Integer id = petNameResolver.resolve(name);
		if (id == null)
		{
			return false;
		}
		flush(name, id);
		return true;
	}

	@Subscribe
	public void onItemContainerChanged(final ItemContainerChanged event)
	{
		if (pendingTrigger == null || event.getContainerId() != InventoryID.INV)
		{
			return;
		}
		final ItemContainer container = event.getItemContainer();
		if (container != null)
		{
			findNewPet(container);
		}
	}

	private void findNewPet(final ItemContainer container)
	{
		for (final Item item : container.getItems())
		{
			if (item == null)
			{
				continue;
			}
			final int id = item.getId();
			if (!PetCatalog.isPet(id) || preTriggerPets != null && preTriggerPets.contains(id))
			{
				continue;
			}
			flush(petNameResolver.resolveName(id), id);
			return;
		}
	}

	@Subscribe
	public void onGameTick(final GameTick event)
	{
		if (pendingTrigger != null
		        && client.getTickCount() - pendingTriggerTick > LootConstants.PET_PENDING_TIMEOUT_TICKS)
		{
			flush(null, null);
		}
	}

	private void flush(final String petName, final Integer petItemId)
	{
		batcher.enqueue(new Payload("pet_drop", "trigger", pendingTrigger, "message", pendingMessage, "petName",
		        petName, "petItemId", petItemId, "where", locationContext.capture(), "sourceKind",
		        interactionState.consumeKind(), "sourceId", interactionState.consumeId(), "sourceName",
		        interactionState.consumeName()));
		pendingTrigger = pendingMessage = null;
		preTriggerPets = null;
	}
}
