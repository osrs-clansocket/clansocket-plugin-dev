package com.clansocket.tracking.loot;

import java.util.Set;

import net.runelite.api.gameval.ItemID;

public final class PetCatalog
{
	@SuppressWarnings("checkstyle:StaticFinalOutsideConstants")
	private static final Set<Integer> PET_IDS = Set.of(ItemID.ABYSSALSIRE_PET, ItemID.HYDRAPET, ItemID.CALLISTO_PET,
	        ItemID.HELL_PET, ItemID.CHAOSELEPET, ItemID.SARADOMINPET, ItemID.COREPET, ItemID.PRIMEPET,
	        ItemID.SUPREMEPET, ItemID.REXPET, ItemID.JAD_PET, ItemID.BANDOSPET, ItemID.MOLEPET, ItemID.DAWNPET,
	        ItemID.INFERNOPET, ItemID.KQPET_WALKING, ItemID.KBDPET, ItemID.KRAKENPET, ItemID.ARMADYLPET,
	        ItemID.ZAMORAKPET, ItemID.SCORPIA_PET, ItemID.SKOTIZOPET, ItemID.SMOKEPET, ItemID.VENENATIS_PET,
	        ItemID.VETION_PET, ItemID.VORKATHPET, ItemID.PHOENIXPET, ItemID.SNAKEPET, ItemID.OLMPET, ItemID.VERZIKPET,
	        ItemID.BLOODHOUND_PET, ItemID.PENANCEPET, ItemID.SKILLPETFISH, ItemID.SKILLPETMINING, ItemID.SKILLPETWC,
	        ItemID.SKILLPETHUNTER_GREY, ItemID.SKILLPETAGILITY, ItemID.SKILLPETFARMING, ItemID.SKILLPETTHIEVING,
	        ItemID.SKILLPETRUNECRAFTING_FIRE, ItemID.HERBIBOARPET, ItemID.CHOMPYBIRD_PET, ItemID.SARACHNISPET,
	        ItemID.ZALCANOPET, ItemID.GAUNTLETPET, ItemID.NIGHTMAREPET, ItemID.SOULWARSPET_BLUE, ItemID.TEMPOROSSPET,
	        ItemID.NEXPET, ItemID.ABYSSALPET);

	private PetCatalog() {
	}

	public static boolean isPet(final int itemId)
	{
		return PET_IDS.contains(itemId);
	}

	public static Set<Integer> allIds()
	{
		return PET_IDS;
	}
}
