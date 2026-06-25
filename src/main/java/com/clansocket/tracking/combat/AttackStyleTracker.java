package com.clansocket.tracking.combat;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.EnumID;
import net.runelite.api.ParamID;
import net.runelite.api.StructComposition;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;

@Singleton
public class AttackStyleTracker
{
	@Inject
	private Client client;

	private String current = CombatConstants.STYLE_UNKNOWN;

	public String getCurrent()
	{
		return current;
	}

	@Subscribe
	public void onVarbitChanged(final VarbitChanged event)
	{
		final int varp = event.getVarpId();
		final int varb = event.getVarbitId();
		if (varp != VarPlayerID.COM_MODE && varb != VarbitID.COMBAT_WEAPON_CATEGORY && varb != VarbitID.AUTOCAST_DEFMODE
		        && varb != VarbitID.AUTOCAST_SPELL)
		{
			return;
		}
		current = resolve();
	}

	private String resolve()
	{
		if (client.getVarbitValue(VarbitID.AUTOCAST_SPELL) > 0)
		{
			return CombatConstants.STYLE_MAGIC;
		}
		final int structId = resolveStructId(effectiveSlot());
		if (structId == CombatConstants.STYLE_NO_RESULT)
		{
			return CombatConstants.STYLE_UNKNOWN;
		}
		final StructComposition s = client.getStructComposition(structId);
		if (s == null)
		{
			return CombatConstants.STYLE_UNKNOWN;
		}
		return mapStyleName(s.getStringValue(ParamID.ATTACK_STYLE_NAME));
	}

	private int effectiveSlot()
	{
		int slot = client.getVarpValue(VarPlayerID.COM_MODE);
		if (slot == CombatConstants.STYLE_CAST_SLOT)
		{
			slot += client.getVarbitValue(VarbitID.AUTOCAST_DEFMODE);
		}
		return slot;
	}

	private int resolveStructId(final int slot)
	{
		final int weaponCat = client.getVarbitValue(VarbitID.COMBAT_WEAPON_CATEGORY);
		final EnumComposition styles = client.getEnum(EnumID.WEAPON_STYLES);
		if (styles == null)
		{
			return CombatConstants.STYLE_NO_RESULT;
		}
		final int weaponStyleEnum = styles.getIntValue(weaponCat);
		if (weaponStyleEnum == CombatConstants.STYLE_NO_RESULT)
		{
			return CombatConstants.STYLE_NO_RESULT;
		}
		final EnumComposition styleStructs = client.getEnum(weaponStyleEnum);
		if (styleStructs == null)
		{
			return CombatConstants.STYLE_NO_RESULT;
		}
		final int[] structs = styleStructs.getIntVals();
		if (slot < 0 || slot >= structs.length)
		{
			return CombatConstants.STYLE_NO_RESULT;
		}
		return structs[slot];
	}

	private String mapStyleName(final String name)
	{
		if (name == null || name.isEmpty())
		{
			return CombatConstants.STYLE_UNKNOWN;
		}
		if (CombatConstants.STYLE_NAME_CASTING.equals(name)
		        || CombatConstants.STYLE_NAME_DEFENSIVE_CASTING.equals(name))
		{
			return CombatConstants.STYLE_MAGIC;
		}
		if (CombatConstants.STYLE_NAME_RANGING.equals(name) || CombatConstants.STYLE_NAME_LONGRANGE.equals(name))
		{
			return CombatConstants.STYLE_RANGED;
		}
		return CombatConstants.STYLE_MELEE;
	}
}
