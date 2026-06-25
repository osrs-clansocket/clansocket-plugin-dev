package com.clansocket.tracking.progression;

import java.util.HashMap;
import java.util.Map;

import net.runelite.api.gameval.VarbitID;

@SuppressWarnings({"checkstyle:StaticFinalOutsideConstants", "PMD.AvoidDuplicateLiterals"})
final class DiaryEntries
{
	static final DiaryEntries.Entry[] ALL = {new Entry("ARDOUGNE", "EASY", VarbitID.ARDOUGNE_DIARY_EASY_COMPLETE),
	        new Entry("ARDOUGNE", "MEDIUM", VarbitID.ARDOUGNE_DIARY_MEDIUM_COMPLETE),
	        new Entry("ARDOUGNE", "HARD", VarbitID.ARDOUGNE_DIARY_HARD_COMPLETE),
	        new Entry("ARDOUGNE", "ELITE", VarbitID.ARDOUGNE_DIARY_ELITE_COMPLETE),
	        new Entry("FALADOR", "EASY", VarbitID.FALADOR_DIARY_EASY_COMPLETE),
	        new Entry("FALADOR", "MEDIUM", VarbitID.FALADOR_DIARY_MEDIUM_COMPLETE),
	        new Entry("FALADOR", "HARD", VarbitID.FALADOR_DIARY_HARD_COMPLETE),
	        new Entry("FALADOR", "ELITE", VarbitID.FALADOR_DIARY_ELITE_COMPLETE),
	        new Entry("WILDERNESS", "EASY", VarbitID.WILDERNESS_DIARY_EASY_COMPLETE),
	        new Entry("WILDERNESS", "MEDIUM", VarbitID.WILDERNESS_DIARY_MEDIUM_COMPLETE),
	        new Entry("WILDERNESS", "HARD", VarbitID.WILDERNESS_DIARY_HARD_COMPLETE),
	        new Entry("WILDERNESS", "ELITE", VarbitID.WILDERNESS_DIARY_ELITE_COMPLETE),
	        new Entry("WESTERN", "EASY", VarbitID.WESTERN_DIARY_EASY_COMPLETE),
	        new Entry("WESTERN", "MEDIUM", VarbitID.WESTERN_DIARY_MEDIUM_COMPLETE),
	        new Entry("WESTERN", "HARD", VarbitID.WESTERN_DIARY_HARD_COMPLETE),
	        new Entry("WESTERN", "ELITE", VarbitID.WESTERN_DIARY_ELITE_COMPLETE),
	        new Entry("KANDARIN", "EASY", VarbitID.KANDARIN_DIARY_EASY_COMPLETE),
	        new Entry("KANDARIN", "MEDIUM", VarbitID.KANDARIN_DIARY_MEDIUM_COMPLETE),
	        new Entry("KANDARIN", "HARD", VarbitID.KANDARIN_DIARY_HARD_COMPLETE),
	        new Entry("KANDARIN", "ELITE", VarbitID.KANDARIN_DIARY_ELITE_COMPLETE),
	        new Entry("VARROCK", "EASY", VarbitID.VARROCK_DIARY_EASY_COMPLETE),
	        new Entry("VARROCK", "MEDIUM", VarbitID.VARROCK_DIARY_MEDIUM_COMPLETE),
	        new Entry("VARROCK", "HARD", VarbitID.VARROCK_DIARY_HARD_COMPLETE),
	        new Entry("VARROCK", "ELITE", VarbitID.VARROCK_DIARY_ELITE_COMPLETE),
	        new Entry("DESERT", "EASY", VarbitID.DESERT_DIARY_EASY_COMPLETE),
	        new Entry("DESERT", "MEDIUM", VarbitID.DESERT_DIARY_MEDIUM_COMPLETE),
	        new Entry("DESERT", "HARD", VarbitID.DESERT_DIARY_HARD_COMPLETE),
	        new Entry("DESERT", "ELITE", VarbitID.DESERT_DIARY_ELITE_COMPLETE),
	        new Entry("MORYTANIA", "EASY", VarbitID.MORYTANIA_DIARY_EASY_COMPLETE),
	        new Entry("MORYTANIA", "MEDIUM", VarbitID.MORYTANIA_DIARY_MEDIUM_COMPLETE),
	        new Entry("MORYTANIA", "HARD", VarbitID.MORYTANIA_DIARY_HARD_COMPLETE),
	        new Entry("MORYTANIA", "ELITE", VarbitID.MORYTANIA_DIARY_ELITE_COMPLETE),
	        new Entry("FREMENNIK", "EASY", VarbitID.FREMENNIK_DIARY_EASY_COMPLETE),
	        new Entry("FREMENNIK", "MEDIUM", VarbitID.FREMENNIK_DIARY_MEDIUM_COMPLETE),
	        new Entry("FREMENNIK", "HARD", VarbitID.FREMENNIK_DIARY_HARD_COMPLETE),
	        new Entry("FREMENNIK", "ELITE", VarbitID.FREMENNIK_DIARY_ELITE_COMPLETE),
	        new Entry("LUMBRIDGE", "EASY", VarbitID.LUMBRIDGE_DIARY_EASY_COMPLETE),
	        new Entry("LUMBRIDGE", "MEDIUM", VarbitID.LUMBRIDGE_DIARY_MEDIUM_COMPLETE),
	        new Entry("LUMBRIDGE", "HARD", VarbitID.LUMBRIDGE_DIARY_HARD_COMPLETE),
	        new Entry("LUMBRIDGE", "ELITE", VarbitID.LUMBRIDGE_DIARY_ELITE_COMPLETE),
	        new Entry("KARAMJA", "EASY", VarbitID.ATJUN_EASY_DONE),
	        new Entry("KARAMJA", "MEDIUM", VarbitID.ATJUN_MED_DONE),
	        new Entry("KARAMJA", "HARD", VarbitID.ATJUN_HARD_DONE),
	        new Entry("KARAMJA", "ELITE", VarbitID.KARAMJA_DIARY_ELITE_COMPLETE),
	        new Entry("KOUREND", "EASY", VarbitID.KOUREND_DIARY_EASY_COMPLETE),
	        new Entry("KOUREND", "MEDIUM", VarbitID.KOUREND_DIARY_MEDIUM_COMPLETE),
	        new Entry("KOUREND", "HARD", VarbitID.KOUREND_DIARY_HARD_COMPLETE),
	        new Entry("KOUREND", "ELITE", VarbitID.KOUREND_DIARY_ELITE_COMPLETE)};

	static final Map<Integer, Entry> BY_VARBIT;
	static
	{
		final Map<Integer, Entry> m = new HashMap<>(ALL.length * 2);
		for (final Entry e : ALL)
		{
			m.put(e.varbit, e);
		}
		BY_VARBIT = Map.copyOf(m);
	}

	private DiaryEntries() {
	}

	static final class Entry
	{
		final String region;
		final String tier;
		final int varbit;

		Entry(final String region, final String tier, final int varbit) {
			this.region = region;
			this.tier = tier;
			this.varbit = varbit;
		}
	}
}
