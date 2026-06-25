package com.clansocket.transport;

import javax.inject.Singleton;

import lombok.Getter;
import lombok.Setter;

@Singleton
public final class SessionStore
{
	@Getter
	@Setter
	private volatile String sessionId;

	public void clear()
	{
		sessionId = null;
	}
}
