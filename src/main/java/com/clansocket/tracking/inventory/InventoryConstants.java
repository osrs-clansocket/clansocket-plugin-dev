package com.clansocket.tracking.inventory;

import java.util.Map;
import java.util.Set;

import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarbitID;

final class InventoryConstants
{
	static final long MENU_CAUSE_MS = 1200L;

	static final int BANK_TAB_COUNT = 9;

	static final String LABEL_EQUIPMENT = "EQUIPMENT";
	static final String LABEL_INVENTORY = "INVENTORY";
	static final String LABEL_SEED_VAULT = "SEED_VAULT";

	static final Map<Integer, String> LABEL_BY_CONTAINER_ID = Map.of(InventoryID.WORN, LABEL_EQUIPMENT, InventoryID.INV,
	        LABEL_INVENTORY, InventoryID.SEED_VAULT, LABEL_SEED_VAULT);

	static final int[] INITIAL_SNAPSHOT_IDS = {InventoryID.WORN, InventoryID.INV, InventoryID.SEED_VAULT};

	static final int[] RUNE_POUCH_TYPE_VARBITS = {VarbitID.RUNE_POUCH_TYPE_1, VarbitID.RUNE_POUCH_TYPE_2,
	        VarbitID.RUNE_POUCH_TYPE_3, VarbitID.RUNE_POUCH_TYPE_4, VarbitID.RUNE_POUCH_TYPE_5,
	        VarbitID.RUNE_POUCH_TYPE_6};

	static final int[] RUNE_POUCH_QTY_VARBITS = {VarbitID.RUNE_POUCH_QUANTITY_1, VarbitID.RUNE_POUCH_QUANTITY_2,
	        VarbitID.RUNE_POUCH_QUANTITY_3, VarbitID.RUNE_POUCH_QUANTITY_4, VarbitID.RUNE_POUCH_QUANTITY_5,
	        VarbitID.RUNE_POUCH_QUANTITY_6};

	static final Set<Integer> RUNE_POUCH_WATCHED = Set.of(VarbitID.RUNE_POUCH_TYPE_1, VarbitID.RUNE_POUCH_TYPE_2,
	        VarbitID.RUNE_POUCH_TYPE_3, VarbitID.RUNE_POUCH_TYPE_4, VarbitID.RUNE_POUCH_TYPE_5,
	        VarbitID.RUNE_POUCH_TYPE_6, VarbitID.RUNE_POUCH_QUANTITY_1, VarbitID.RUNE_POUCH_QUANTITY_2,
	        VarbitID.RUNE_POUCH_QUANTITY_3, VarbitID.RUNE_POUCH_QUANTITY_4, VarbitID.RUNE_POUCH_QUANTITY_5,
	        VarbitID.RUNE_POUCH_QUANTITY_6);

	private InventoryConstants() {
	}
}
