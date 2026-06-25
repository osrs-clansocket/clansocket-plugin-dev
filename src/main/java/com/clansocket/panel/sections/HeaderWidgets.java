package com.clansocket.panel.sections;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.clansocket.ClanSocketConstants;
import com.clansocket.panel.PanelConstants;
import com.clansocket.panel.PanelStats;
import com.clansocket.panel.swingfactory.SwingFactory;
import com.clansocket.panel.widgets.ResetButton;
import com.clansocket.panel.widgets.StatusDot;
import com.clansocket.util.Strings;
import com.clansocket.util.Urls;

final class HeaderWidgets
{
	private HeaderWidgets() {
	}

	static void applyConnectionState(final StatusDot dot, final JLabel connStatus,
	        final PanelStats.ConnectionState state)
	{
		dot.setColor(SwingFactory.colorFor(state));
		if (state == PanelStats.ConnectionState.CONNECTED)
		{
			connStatus.setText(PanelConstants.CONN_STATUS_ONLINE);
			connStatus.setForeground(PanelConstants.COLOR_AUTH_VALID);
			return;
		}
		if (state == PanelStats.ConnectionState.CONNECTING || state == PanelStats.ConnectionState.RECONNECTING)
		{
			connStatus.setText(PanelConstants.CONN_STATUS_RECONNECT);
			connStatus.setForeground(PanelConstants.COLOR_STATE_WARN);
			return;
		}
		connStatus.setText(PanelConstants.CONN_STATUS_OFFLINE);
		connStatus.setForeground(PanelConstants.COLOR_AUTH_NOT_SET);
	}

	static void applyClanStatus(final JLabel label, final String clanName, final String reason)
	{
		if (clanName == null)
		{
			label.setText(PanelConstants.CLAN_STATUS_NONE);
			label.setForeground(PanelConstants.COLOR_AUTH_NOT_SET);
			return;
		}
		if (ClanSocketConstants.REASON_NOT_REGISTERED.equals(reason))
		{
			label.setText(String.format(PanelConstants.CLAN_STATUS_UNCLAIMED_FMT, clanName));
			label.setForeground(PanelConstants.COLOR_STATE_WARN);
			return;
		}
		if (ClanSocketConstants.REASON_NOT_MEMBER.equals(reason))
		{
			label.setText(String.format(PanelConstants.CLAN_STATUS_UNVERIFIED_FMT, clanName));
			label.setForeground(PanelConstants.COLOR_STATE_WARN);
			return;
		}
		label.setText(String.format(PanelConstants.CLAN_STATUS_FMT, clanName));
		label.setForeground(PanelConstants.COLOR_AUTH_VALID);
	}

	static JPanel buildEastBlock(final StatusDot dot, final JLabel connStatus, final Runnable resetAction)
	{
		SwingFactory.applySmall(connStatus, PanelConstants.COLOR_AUTH_NOT_SET);
		final JPanel top = new JPanel();
		top.setOpaque(false);
		top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
		top.add(dot);
		top.add(Box.createHorizontalStrut(PanelConstants.RESET_BUTTON_RIGHT_GAP));
		top.add(connStatus);
		final JPanel bottom = new JPanel();
		bottom.setOpaque(false);
		bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
		bottom.add(Box.createHorizontalGlue());
		bottom.add(new ResetButton(PanelConstants.RESET_LABEL, PanelConstants.RESET_TOOLTIP, resetAction));
		final JPanel block = new JPanel();
		block.setOpaque(false);
		block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
		block.add(top);
		block.add(Box.createVerticalGlue());
		block.add(bottom);
		return block;
	}

	static String formatSubtitle(final PanelStats stats)
	{
		final String url = stats.getEndpoint();
		if (Strings.isEmpty(url))
		{
			return PanelConstants.SUBTITLE_DISCONNECTED;
		}
		return Urls.host(url);
	}
}
