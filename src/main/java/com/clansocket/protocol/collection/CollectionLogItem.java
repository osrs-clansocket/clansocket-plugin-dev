package com.clansocket.protocol.collection;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class CollectionLogItem
{
	public final int itemId;
	public final int quantity;
	public final String name;
	public final long price;
	public final String category;
}
