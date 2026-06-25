package com.clansocket.transport.consent;

import javax.inject.Singleton;

import lombok.Value;

@Singleton
public final class ClaimConsentStore extends AbstractConsentRequestStore<ClaimConsentStore.Request>
{
	@Value
	public static class Request implements ConsentRequestView
	{
		long requestId;
		String requestingDisplayName;
		String requestedRsn;
		String requestedClanName;
		long expiresAtMs;
	}
}
