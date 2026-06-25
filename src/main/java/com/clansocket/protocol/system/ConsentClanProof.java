package com.clansocket.protocol.system;

import com.clansocket.protocol.common.Payload;

public final class ConsentClanProof
{
	public final Payload roster;
	public final Payload titles;

	public ConsentClanProof(final Payload roster, final Payload titles) {
		this.roster = roster;
		this.titles = titles;
	}
}
