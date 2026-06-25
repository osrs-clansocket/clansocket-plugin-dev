package com.clansocket.tracking.loot;

public final class PetTriggerClassifier
{
	private PetTriggerClassifier() {
	}

	public static String classify(final String text)
	{
		if (text.contains(LootConstants.PET_BAGGED))
		{
			return LootConstants.TRIGGER_BAGGED;
		}
		if (text.contains(LootConstants.PET_FOLLOWING))
		{
			return LootConstants.TRIGGER_FOLLOWING;
		}
		if (text.contains(LootConstants.PET_SNEAKING))
		{
			return LootConstants.TRIGGER_SNEAKING;
		}
		return null;
	}
}
