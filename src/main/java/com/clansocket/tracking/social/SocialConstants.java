package com.clansocket.tracking.social;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.runelite.api.ChatMessageType;
import net.runelite.api.MenuAction;

final class SocialConstants
{
	static final String CLAN_CHANNEL = "CLAN";
	static final String CHAT_KIND_MESSAGE = "MESSAGE";
	static final String CHAT_KIND_BROADCAST = "BROADCAST";
	static final String CHAT_SYSTEM_SENDER = "CLAN_NOTIF";
	static final String BROADCAST_CATEGORY_DEFAULT = "Broadcast";

	static final List<String> CHAT_SKIP_FRAGMENTS = List.of("To talk in your clan's channel");

	static final Map<String, String> BROADCAST_CATEGORIES = Map.ofEntries(Map.entry("received a drop:", "Drop"),
	        Map.entry("received special loot", "Drop"), Map.entry("has a funny feeling", "Pet"),
	        Map.entry("has been promoted", "Promote"), Map.entry("has been demoted", "Demote"),
	        Map.entry("has been kicked out", "Kick"), Map.entry("has joined the clan", "Join"),
	        Map.entry("has left the clan", "Leave"), Map.entry("has reached the highest possible", "Level"),
	        Map.entry("skill total", "Level"), Map.entry("completed a quest", "Quest"),
	        Map.entry("completed a combat achievement", "Achievement"), Map.entry("combat task", "Achievement"),
	        Map.entry("received a new collection log item", "Collection"),
	        Map.entry("into the coffer", "CofferDeposit"), Map.entry("from the coffer", "CofferWithdraw"));

	static final Set<ChatMessageType> CLAN_CHAT_TYPES = EnumSet.of(ChatMessageType.CLAN_CHAT,
	        ChatMessageType.CLAN_MESSAGE, ChatMessageType.CLAN_GIM_FORM_GROUP, ChatMessageType.CLAN_GIM_GROUP_WITH,
	        ChatMessageType.BROADCAST);

	static final char NBSP = (char) 160;
	static final char ANGLE_OPEN = '<';
	static final char ANGLE_CLOSE = '>';
	static final char META_DELIMITER = '|';
	static final String IMG_PREFIX = "img";
	static final String COL_PREFIX = "col";
	static final String COL_CLOSE = "/col";
	static final String LT_ENTITY = "lt";
	static final String GT_ENTITY = "gt";
	static final String CA_ID_PREFIX = "CA_ID:";

	static final Set<MenuAction> TRACKED_MENU_ACTIONS = EnumSet.of(MenuAction.GAME_OBJECT_FIRST_OPTION,
	        MenuAction.GAME_OBJECT_SECOND_OPTION, MenuAction.GAME_OBJECT_THIRD_OPTION,
	        MenuAction.GAME_OBJECT_FOURTH_OPTION, MenuAction.GAME_OBJECT_FIFTH_OPTION, MenuAction.NPC_FIRST_OPTION,
	        MenuAction.NPC_SECOND_OPTION, MenuAction.NPC_THIRD_OPTION, MenuAction.NPC_FOURTH_OPTION,
	        MenuAction.NPC_FIFTH_OPTION);

	static final Map<String, String> MENU_KIND_BY_PREFIX = Map.of("NPC_", "NPC", "GAME_OBJECT_", "GAME_OBJECT");

	private SocialConstants() {
	}
}
