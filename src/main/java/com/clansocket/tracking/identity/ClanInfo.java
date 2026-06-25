package com.clansocket.tracking.identity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@SuppressWarnings("checkstyle:StaticFinalOutsideConstants")
final class ClanInfo
{
	static final ClanInfo EMPTY = new ClanInfo(null, null, null, null, null);

	final String name;
	final String rank;
	final String joinedAt;
	final Integer memberCount;
	final Integer onlineCount;
}
