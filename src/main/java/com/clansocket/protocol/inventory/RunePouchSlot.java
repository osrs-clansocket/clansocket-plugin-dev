package com.clansocket.protocol.inventory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class RunePouchSlot
{
	public final int slot;
	public final int itemId;
	public final int qty;
	public final String name;
	public final int price;
}
