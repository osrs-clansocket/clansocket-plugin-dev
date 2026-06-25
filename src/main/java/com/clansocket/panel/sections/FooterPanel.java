package com.clansocket.panel.sections;

import java.awt.BorderLayout;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.LinkBrowser;

import com.clansocket.panel.PanelConstants;
import com.clansocket.panel.swingfactory.SwingFactory;

@Singleton
public class FooterPanel extends JPanel
{
	@Inject
	@SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
	public FooterPanel() {
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setLayout(new BorderLayout());
		setBorder(new CompoundBorder(
		        new MatteBorder(PanelConstants.SEPARATOR_HEIGHT, 0, 0, 0, PanelConstants.COLOR_GOLD_BORDER),
		        new EmptyBorder(PanelConstants.FOOTER_PADDING_Y, PanelConstants.FOOTER_PADDING_X,
		                PanelConstants.FOOTER_PADDING_Y, PanelConstants.FOOTER_PADDING_X)));

		add(buildLinks(), BorderLayout.WEST);
	}

	private static JPanel buildLinks()
	{
		final JPanel links = new JPanel();
		links.setOpaque(false);
		links.setLayout(new BoxLayout(links, BoxLayout.Y_AXIS));
		links.add(linkLabel(PanelConstants.FOOTER_WEB_LABEL, PanelConstants.FOOTER_WEB_URL));
		links.add(Box.createVerticalStrut(PanelConstants.FOOTER_LINK_GAP));
		links.add(linkLabel(PanelConstants.FOOTER_GITHUB_LABEL, PanelConstants.FOOTER_GITHUB_URL));
		return links;
	}

	private static JLabel linkLabel(final String text, final String url)
	{
		final JLabel label = SwingFactory.smallLabel(text, SwingConstants.LEFT, ColorScheme.LIGHT_GRAY_COLOR);
		SwingFactory.onHoverColorSwap(label, PanelConstants.COLOR_GOLD_TEXT, ColorScheme.LIGHT_GRAY_COLOR,
		        () -> LinkBrowser.browse(url));
		return label;
	}
}
