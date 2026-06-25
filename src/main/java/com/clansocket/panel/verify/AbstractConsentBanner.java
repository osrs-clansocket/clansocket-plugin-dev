package com.clansocket.panel.verify;

import java.awt.Dimension;
import java.util.Optional;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.clansocket.ClanSocket;
import com.clansocket.ClanSocketConstants;
import com.clansocket.protocol.common.Payload;
import com.clansocket.protocol.system.ConsentClanProof;
import com.clansocket.transport.consent.AbstractConsentRequestStore;
import com.clansocket.transport.consent.ConsentRequestView;

abstract class AbstractConsentBanner extends JPanel
{
	private final ClanSocket socket;
	private final AbstractConsentRequestStore<?> source;
	private final ConsentBannerLayout layout;
	private float pulsePhase;

	@SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
	protected AbstractConsentBanner(final ClanSocket socket, final String titleText,
	        final AbstractConsentRequestStore<?> source) {
		this.socket = socket;
		this.source = source;
		this.layout = new ConsentBannerLayout(this, titleText,
		        e -> respond(ClanSocketConstants.CONSENT_ACTION_CONFIRM, false),
		        e -> respond(ClanSocketConstants.CONSENT_ACTION_REJECT, true));
		source.addListener(() -> SwingUtilities.invokeLater(this::refresh));
		new Timer(VerifyConstants.PULSE_INTERVAL_MS, e -> tick()).start();
		refresh();
	}

	protected abstract String subtitleFormat();
	protected abstract String rejectConfirmMessage();
	protected abstract String responseFrameType();

	protected Optional<Payload> rosterForConfirm()
	{
		return Optional.empty();
	}

	protected Optional<Payload> titlesForConfirm()
	{
		return Optional.empty();
	}

	private void refresh()
	{
		final ConsentRequestView view = source.current();
		setVisible(view != null);
		if (view != null)
		{
			layout.applySubtitle(String.format(subtitleFormat(), VerifyConstants.SUBTITLE_WIDTH,
			        view.getRequestingDisplayName(), view.getRequestedRsn(), view.getRequestedClanName()));
			layout.applyCountdown(view.getExpiresAtMs() - System.currentTimeMillis());
		}
		revalidate();
		repaint();
	}

	private void respond(final String action, final boolean needConfirm)
	{
		if (needConfirm && JOptionPane.showConfirmDialog(this, rejectConfirmMessage(),
		        VerifyConstants.REJECT_CONFIRM_TITLE, JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
		{
			return;
		}
		final ConsentRequestView view = source.current();
		if (view == null)
		{
			return;
		}
		final boolean confirmed = ClanSocketConstants.CONSENT_ACTION_CONFIRM.equals(action);
		final ConsentClanProof proof = confirmed
		        ? new ConsentClanProof(rosterForConfirm().orElse(null), titlesForConfirm().orElse(null))
		        : null;
		socket.send(new Payload(responseFrameType(), "requestId", view.getRequestId(), "action", action, "clanProof",
		        proof));
		source.clear();
	}

	private void tick()
	{
		if (!isVisible())
		{
			return;
		}
		pulsePhase = (pulsePhase + VerifyConstants.PULSE_STEP) % 1f;
		layout.applyPulse(VerifyConstants.pulseColor(pulsePhase));
		final ConsentRequestView view = source.current();
		if (view != null)
		{
			layout.applyCountdown(view.getExpiresAtMs() - System.currentTimeMillis());
		}
	}

	@Override
	public Dimension getMaximumSize()
	{
		return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
	}
}
