package com.clansocket.panel.verify;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.clansocket.ClanSocket;
import com.clansocket.ClanSocketConstants;
import com.clansocket.transport.VerifyRequestStore;

@Singleton
public class RsnVerifyBanner extends AbstractConsentBanner
{
	@Inject
	@SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
	public RsnVerifyBanner(final ClanSocket socket, final VerifyRequestStore store) {
		super(socket, VerifyConstants.LABEL_TITLE_RSN, store);
	}

	@Override
	protected String subtitleFormat()
	{
		return VerifyConstants.FMT_SUBTITLE_RSN;
	}

	@Override
	protected String rejectConfirmMessage()
	{
		return VerifyConstants.REJECT_CONFIRM_MESSAGE_RSN;
	}

	@Override
	protected String responseFrameType()
	{
		return ClanSocketConstants.WS_TYPE_RSN_VERIFY_RESPONSE;
	}
}
