package com.clansocket.panel.verify;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.clansocket.ClanSocket;
import com.clansocket.ClanSocketConstants;
import com.clansocket.protocol.common.Payload;
import com.clansocket.tracking.clan.ClanRosterTracker;
import com.clansocket.tracking.clan.ClanTitlesTracker;
import com.clansocket.transport.consent.ClaimConsentStore;

@Singleton
public class ClaimConsentBanner extends AbstractConsentBanner
{
	private final ClanRosterTracker rosterTracker;
	private final ClanTitlesTracker titlesTracker;

	@Inject
	@SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
	public ClaimConsentBanner(final ClanSocket socket, final ClaimConsentStore store,
	        final ClanRosterTracker rosterTracker, final ClanTitlesTracker titlesTracker) {
		super(socket, VerifyConstants.LABEL_TITLE_CLAIM, store);
		this.rosterTracker = rosterTracker;
		this.titlesTracker = titlesTracker;
	}

	@Override
	protected String subtitleFormat()
	{
		return VerifyConstants.FMT_SUBTITLE_CLAIM;
	}

	@Override
	protected String rejectConfirmMessage()
	{
		return VerifyConstants.REJECT_CONFIRM_MESSAGE_CLAIM;
	}

	@Override
	protected String responseFrameType()
	{
		return ClanSocketConstants.WS_TYPE_CLAIM_CONSENT_RESPONSE;
	}

	@Override
	protected Optional<Payload> rosterForConfirm()
	{
		return rosterTracker.currentSnapshot();
	}

	@Override
	protected Optional<Payload> titlesForConfirm()
	{
		return titlesTracker.currentSnapshot();
	}
}
