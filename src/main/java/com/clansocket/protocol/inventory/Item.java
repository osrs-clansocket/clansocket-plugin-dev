package com.clansocket.protocol.inventory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Item
{
	public final int id;
	public final int qty;
	public final String name;
	public final int price;
	public final int slot;
	public final Integer bankTab;
}
