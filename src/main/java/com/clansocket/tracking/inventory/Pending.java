package com.clansocket.tracking.inventory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class Pending
{
	final String label;
	final int[] ids;
	final int[] qtys;
	final int[] slots;
}
