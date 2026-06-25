package com.clansocket.transport.consent;

public interface ConsentRequestView
{
	long getRequestId();

	long getExpiresAtMs();

	String getRequestingDisplayName();

	String getRequestedRsn();

	default String getRequestedClanName()
	{
		return "";
	}
}
