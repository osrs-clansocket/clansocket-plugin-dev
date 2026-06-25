package com.clansocket.protocol.progression;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class CatalogEntry
{
	public final int taskId;
	public final String name;
	public final String description;
	public final String tier;
	public final String taskType;
	public final int points;
	public final int bossId;
	public final String bossName;
}
