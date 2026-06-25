package com.clansocket.panel.sections;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;

import com.clansocket.panel.PanelConstants;
import com.clansocket.panel.PanelStats;
import com.clansocket.panel.swingfactory.SwingFactory;
import com.clansocket.panel.widgets.StatusDot;
import com.clansocket.tracking.identity.ClanResolver;
import com.clansocket.util.Strings;

@Singleton
public class HeaderPanel extends JPanel
{
	private final StatusDot dot = new StatusDot(PanelConstants.HEADER_DOT_DIAMETER);
	private final JLabel subtitle = new JLabel(PanelConstants.SUBTITLE_DISCONNECTED);
	private final JLabel clanStatus = new JLabel(PanelConstants.CLAN_STATUS_NONE);
	private final JLabel connStatus = new JLabel(PanelConstants.CONN_STATUS_OFFLINE);
	private final PanelStats panelStats;
	private final ClanResolver clanResolver;
	private final Client client;
	private PanelStats.ConnectionState lastState;
	private String lastSubtitleText = PanelConstants.SUBTITLE_DISCONNECTED;
	private String lastClanKey;

	@Inject
	@SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
	public HeaderPanel(final PanelStats panelStats, final ClanResolver clanResolver, final Client client) {
		this.panelStats = panelStats;
		this.clanResolver = clanResolver;
		this.client = client;
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setLayout(new BorderLayout(PanelConstants.ICON_GAP, 0));
		setBorder(new CompoundBorder(
		        new MatteBorder(0, 0, PanelConstants.SEPARATOR_HEIGHT, 0, PanelConstants.COLOR_GOLD_BORDER),
		        new EmptyBorder(PanelConstants.HEADER_PADDING_Y, PanelConstants.HEADER_PADDING_X,
		                PanelConstants.HEADER_PADDING_Y, PanelConstants.HEADER_PADDING_X)));

		final BufferedImage icon = ImageUtil.loadImageResource(PanelConstants.class, PanelConstants.ICON_RESOURCE);
		final JLabel iconLabel = new JLabel(new ImageIcon(icon));
		iconLabel.setBorder(BorderFactory.createEmptyBorder());

		add(iconLabel, BorderLayout.WEST);
		add(buildTitleBlock(), BorderLayout.CENTER);
		add(HeaderWidgets.buildEastBlock(dot, connStatus, this::confirmReset), BorderLayout.EAST);
	}

	public void refresh(final PanelStats stats)
	{
		final PanelStats.ConnectionState state = stats.getConnectionState();
		if (state != lastState)
		{
			lastState = state;
			HeaderWidgets.applyConnectionState(dot, connStatus, state);
		}
		final String text = HeaderWidgets.formatSubtitle(stats);
		if (!text.equals(lastSubtitleText))
		{
			lastSubtitleText = text;
			subtitle.setText(text);
		}
		refreshClanStatus(stats);
	}

	private void refreshClanStatus(final PanelStats stats)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			if (!"loggedOut".equals(lastClanKey))
			{
				lastClanKey = "loggedOut";
				clanStatus.setText(PanelConstants.CLAN_STATUS_NOT_LOGGED_IN);
				clanStatus.setForeground(PanelConstants.COLOR_AUTH_NOT_SET);
			}
			return;
		}
		final String name = clanResolver.currentClanName();
		final String normalized = Strings.isEmpty(name) ? null : name;
		final String reason = stats.getClanReason();
		final String key = "in/" + normalized + "/" + reason;
		if (Objects.equals(key, lastClanKey))
		{
			return;
		}
		lastClanKey = key;
		HeaderWidgets.applyClanStatus(clanStatus, normalized, reason);
	}

	private JPanel buildTitleBlock()
	{
		final JPanel block = new JPanel();
		block.setOpaque(false);
		block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));

		final JLabel title = new JLabel(PanelConstants.TITLE_TEXT);
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setForeground(PanelConstants.COLOR_GOLD_TEXT);
		final JLabel version = SwingFactory.smallLabel(PanelConstants.VERSION_LABEL, ColorScheme.MEDIUM_GRAY_COLOR);

		SwingFactory.applySmall(subtitle, ColorScheme.MEDIUM_GRAY_COLOR);
		SwingFactory.applySmall(clanStatus, PanelConstants.COLOR_AUTH_NOT_SET);

		final JPanel titleRow = SwingFactory.transparentRow(PanelConstants.SECTION_HEADER_GAP, title, version,
		        BorderLayout.CENTER);
		titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
		subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
		clanStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
		block.add(titleRow);
		block.add(subtitle);
		block.add(clanStatus);
		return block;
	}

	@SuppressWarnings("PMD.UnusedPrivateMethod")
	private void confirmReset()
	{
		final int answer = JOptionPane.showConfirmDialog(this, PanelConstants.RESET_CONFIRM_MESSAGE,
		        PanelConstants.RESET_CONFIRM_TITLE, JOptionPane.YES_NO_OPTION);
		if (answer == JOptionPane.YES_OPTION)
		{
			panelStats.resetCounts();
		}
	}
}
