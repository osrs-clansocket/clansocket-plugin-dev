package com.clansocket.protocol.inventory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ItemCause
{
	public final String action;
	public final String option;
	public final String target;
	public final int id;
}
