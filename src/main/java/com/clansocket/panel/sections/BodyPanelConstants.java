package com.clansocket.panel.sections;

import java.util.Set;

import com.clansocket.panel.widgets.StreamGate;

public final class BodyPanelConstants
{
	public static final Set<StreamGate> COMPACT = Set.of(StreamGate.SKILLS_SNAPSHOT, StreamGate.LEVEL_UPS,
	        StreamGate.DEATH, StreamGate.BANK, StreamGate.PET_DROPS, StreamGate.QUESTS, StreamGate.DIARIES,
	        StreamGate.CLUES, StreamGate.COLLECTION_LOG, StreamGate.COMBAT_ACHIEVEMENTS, StreamGate.FARMING);

	public static final Set<StreamGate> HIDDEN = Set.of(StreamGate.SKILLS_SNAPSHOT);

	private BodyPanelConstants() {
	}
}
