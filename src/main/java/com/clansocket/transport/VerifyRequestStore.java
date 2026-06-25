package com.clansocket.transport;

import javax.inject.Singleton;

import com.clansocket.transport.consent.AbstractConsentRequestStore;
import com.clansocket.transport.consent.ConsentRequestView;

import lombok.Value;

@Singleton
public final class VerifyRequestStore extends AbstractConsentRequestStore<VerifyRequestStore.Request>
{
	@Value
	public static class Request implements ConsentRequestView
	{
		long requestId;
		String requestingDisplayName;
		String requestedRsn;
		long expiresAtMs;
	}
}
