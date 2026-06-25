package com.clansocket.transport.consent;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.clansocket.ClanSocketConstants;
import com.clansocket.transport.VerifyRequestStore;
import com.google.gson.JsonObject;

@Singleton
public final class ConsentDispatch
{
	private final VerifyRequestStore rsn;
	private final ClaimConsentStore claim;

	@Inject
	public ConsentDispatch(final VerifyRequestStore rsn, final ClaimConsentStore claim) {
		this.rsn = rsn;
		this.claim = claim;
	}

	public boolean tryHandle(final String type, final JsonObject obj)
	{
		return handleRsn(type, obj) || handleClaim(type, obj);
	}

	private boolean handleRsn(final String type, final JsonObject obj)
	{
		if (ClanSocketConstants.WS_TYPE_RSN_VERIFY_REQUEST.equals(type))
		{
			rsn.submit(new VerifyRequestStore.Request(getId(obj), getDisplayName(obj), getRsn(obj), getExpiresAt(obj)));
			return true;
		}
		if (ClanSocketConstants.WS_TYPE_RSN_VERIFY_CANCELLED.equals(type))
		{
			rsn.cancel(getId(obj));
			return true;
		}
		return false;
	}

	private boolean handleClaim(final String type, final JsonObject obj)
	{
		if (ClanSocketConstants.WS_TYPE_CLAIM_CONSENT_REQUEST.equals(type))
		{
			claim.submit(new ClaimConsentStore.Request(getId(obj), getDisplayName(obj), getRsn(obj), getClanName(obj),
			        getExpiresAt(obj)));
			return true;
		}
		if (ClanSocketConstants.WS_TYPE_CLAIM_CONSENT_CANCELLED.equals(type))
		{
			claim.cancel(getId(obj));
			return true;
		}
		return false;
	}

	private static long getId(final JsonObject obj)
	{
		return obj.get(ClanSocketConstants.WS_FIELD_REQUEST_ID).getAsLong();
	}

	private static long getExpiresAt(final JsonObject obj)
	{
		return obj.get(ClanSocketConstants.WS_FIELD_EXPIRES_AT).getAsLong();
	}

	private static String getDisplayName(final JsonObject obj)
	{
		return obj.get(ClanSocketConstants.WS_FIELD_REQUESTING_DISPLAY_NAME).getAsString();
	}

	private static String getRsn(final JsonObject obj)
	{
		return obj.get(ClanSocketConstants.WS_FIELD_REQUESTED_RSN).getAsString();
	}

	private static String getClanName(final JsonObject obj)
	{
		return obj.get(ClanSocketConstants.WS_FIELD_REQUESTED_CLAN_NAME).getAsString();
	}
}
