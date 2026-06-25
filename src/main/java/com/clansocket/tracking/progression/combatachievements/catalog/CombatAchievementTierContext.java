package com.clansocket.tracking.progression.combatachievements.catalog;

import net.runelite.api.Client;
import net.runelite.api.EnumComposition;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class CombatAchievementTierContext
{
	final Client client;
	final EnumComposition bossEnum;
	final int tierIdx;
}
