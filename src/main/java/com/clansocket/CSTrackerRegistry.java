package com.clansocket;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.clansocket.bus.AbstractStateTracker;
import com.clansocket.bus.EventBatcher;
import com.clansocket.tracking.clan.ClanRosterTracker;
import com.clansocket.tracking.clan.ClanTitlesTracker;
import com.clansocket.tracking.combat.AttackStyleTracker;
import com.clansocket.tracking.combat.CombatTracker;
import com.clansocket.tracking.combat.DeathHandler;
import com.clansocket.tracking.combat.SlayerTracker;
import com.clansocket.tracking.farming.FarmingTracker;
import com.clansocket.tracking.identity.IdentityTracker;
import com.clansocket.tracking.identity.LoginStateTracker;
import com.clansocket.tracking.inventory.BankSessionTracker;
import com.clansocket.tracking.inventory.ContainerTracker;
import com.clansocket.tracking.inventory.RunePouchTracker;
import com.clansocket.tracking.loot.LootTracker;
import com.clansocket.tracking.loot.PetDropTracker;
import com.clansocket.tracking.movement.LocationTracker;
import com.clansocket.tracking.progression.ClueCompletedTracker;
import com.clansocket.tracking.progression.ClueOpenedTracker;
import com.clansocket.tracking.progression.DiaryTracker;
import com.clansocket.tracking.progression.collectionlog.CollectionLogSnapshotTracker;
import com.clansocket.tracking.progression.collectionlog.CollectionLogTracker;
import com.clansocket.tracking.progression.combatachievements.catalog.CombatAchievementCatalogTracker;
import com.clansocket.tracking.progression.combatachievements.progress.CombatAchievementProgressTracker;
import com.clansocket.tracking.progression.quests.QuestCompletionTracker;
import com.clansocket.tracking.progression.quests.QuestSnapshotTracker;
import com.clansocket.tracking.progression.xp.LevelUpTracker;
import com.clansocket.tracking.progression.xp.StatsSnapshotTracker;
import com.clansocket.tracking.progression.xp.XpDeltaTracker;
import com.clansocket.tracking.social.ChatTracker;
import com.clansocket.tracking.social.MenuActionTracker;
import com.clansocket.tracking.state.BoostTracker;
import com.clansocket.tracking.state.PrayerTracker;
import com.clansocket.tracking.state.StatusEffectTracker;
import com.clansocket.tracking.state.VitalsTracker;

@Singleton
public class CSTrackerRegistry
{
	private final List<Object> trackers;

	@Inject
	@SuppressWarnings({"checkstyle:ParameterNumber", "PMD.ExcessiveParameterList"})
	public CSTrackerRegistry(final IdentityTracker identityTracker, final StatsSnapshotTracker statsSnapshotTracker,
	        final XpDeltaTracker xpDeltaTracker, final LevelUpTracker levelUpTracker, final CombatTracker combatTracker,
	        final DeathHandler deathHandler, final LocationTracker locationTracker, final VitalsTracker vitalsTracker,
	        final PrayerTracker prayerTracker, final StatusEffectTracker statusEffectTracker,
	        final ContainerTracker containerTracker, final BankSessionTracker bankSessionTracker,
	        final MenuActionTracker menuActionTracker, final BoostTracker boostTracker, final ChatTracker chatTracker,
	        final LootTracker lootTracker, final PetDropTracker petDropTracker, final SlayerTracker slayerTracker,
	        final LoginStateTracker loginStateTracker, final RunePouchTracker runePouchTracker,
	        final QuestSnapshotTracker questSnapshotTracker, final QuestCompletionTracker questCompletionTracker,
	        final DiaryTracker diaryTracker, final ClueCompletedTracker clueCompletedTracker,
	        final ClueOpenedTracker clueOpenedTracker, final CollectionLogTracker collectionLogTracker,
	        final CollectionLogSnapshotTracker collectionLogSnapshotTracker,
	        final CombatAchievementCatalogTracker combatAchievementCatalogTracker,
	        final CombatAchievementProgressTracker combatAchievementProgressTracker,
	        final FarmingTracker farmingTracker, final ClanRosterTracker clanRosterTracker,
	        final ClanTitlesTracker clanTitlesTracker, final AttackStyleTracker attackStyleTracker,
	        final EventBatcher eventBatcher) {
		this.trackers = Arrays.asList(identityTracker, statsSnapshotTracker, xpDeltaTracker, levelUpTracker,
		        combatTracker, deathHandler, locationTracker, vitalsTracker, prayerTracker, statusEffectTracker,
		        containerTracker, bankSessionTracker, menuActionTracker, boostTracker, chatTracker, lootTracker,
		        petDropTracker, slayerTracker, loginStateTracker, runePouchTracker, questSnapshotTracker,
		        questCompletionTracker, diaryTracker, clueCompletedTracker, clueOpenedTracker, collectionLogTracker,
		        collectionLogSnapshotTracker, combatAchievementCatalogTracker, combatAchievementProgressTracker,
		        farmingTracker, clanRosterTracker, clanTitlesTracker, attackStyleTracker, eventBatcher);
	}

	public List<Object> getAll()
	{
		return trackers;
	}

	public void broadcastResetForReconnect()
	{
		for (final Object t : trackers)
		{
			if (t instanceof AbstractStateTracker)
			{
				((AbstractStateTracker) t).resetForReconnect();
			}
		}
	}
}
