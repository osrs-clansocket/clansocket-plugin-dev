package com.clansocket.tracking.combat;

import javax.inject.Singleton;

@Singleton
public final class InteractionState
{
	private String lastKind;
	private Integer lastId;
	private String lastName;
	private int hpCache = -1;

	public void recordNpc(final int id, final String name)
	{
		lastKind = CombatConstants.INTERACT_KIND_NPC;
		lastId = id;
		lastName = name;
	}

	public void recordPlayer()
	{
		lastKind = CombatConstants.INTERACT_KIND_PLAYER;
		lastId = CombatConstants.INTERACT_DEDUP_PLAYER;
		lastName = null;
	}

	public void updateHp(final int hp)
	{
		if (hp > 0)
		{
			hpCache = hp;
		}
	}

	public String consumeKind()
	{
		return lastKind != null ? lastKind : CombatConstants.INTERACT_KIND_UNKNOWN;
	}

	public Integer consumeId()
	{
		return lastId;
	}

	public String consumeName()
	{
		return lastName;
	}

	public Integer consumeHpBefore()
	{
		return hpCache > 0 ? hpCache : null;
	}

	public void reset()
	{
		hpCache = -1;
		lastKind = null;
		lastId = null;
		lastName = null;
	}
}
