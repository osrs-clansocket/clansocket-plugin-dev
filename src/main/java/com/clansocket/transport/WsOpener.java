package com.clansocket.transport;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.clansocket.ClanSocketConstants;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

@Singleton
public final class WsOpener
{
	@Inject
	private OkHttpClient http;
	private volatile OkHttpClient wsHttp;

	public WebSocket open(final String endpoint, final WebSocketListener callback)
	{
		OkHttpClient client = wsHttp;
		if (client == null)
		{
			client = http.newBuilder().pingInterval(ClanSocketConstants.WS_PING_INTERVAL_SECONDS, TimeUnit.SECONDS)
			        .build();
			wsHttp = client;
		}
		return client.newWebSocket(new Request.Builder().url(endpoint).build(), callback);
	}
}
