package com.clansocket;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public final class ClanSocketPluginTest
{
	private ClanSocketPluginTest() {
	}

	@SuppressWarnings({"unchecked", "varargs", "PMD.SignatureDeclareThrowsException"})
	public static void main(final String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ClanSocketPlugin.class);
		RuneLite.main(args);
	}
}
