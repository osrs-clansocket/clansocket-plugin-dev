package com.clansocket.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import com.clansocket.ClanSocketConfig;
import com.clansocket.panel.sections.BodyPanel;
import com.clansocket.panel.sections.FooterPanel;
import com.clansocket.panel.sections.HeaderPanel;
import com.clansocket.panel.sections.PresetsSection;
import com.clansocket.panel.verify.ClaimConsentBanner;
import com.clansocket.panel.verify.RsnVerifyBanner;

@Singleton
public class ClanSocketPanel extends PluginPanel
{
	private final HeaderPanel header;
	private final BodyPanel body;
	private final PresetsSection presets;
	private final RsnVerifyBanner rsnVerifyBanner;
	private final ClaimConsentBanner claimConsentBanner;

	@Inject
	@SuppressWarnings({"PMD.ConstructorCallsOverridableMethod", "checkstyle:ParameterNumber",
	        "PMD.ExcessiveParameterList"})
	public ClanSocketPanel(final HeaderPanel header, final BodyPanel body, final FooterPanel footer,
	        final PresetsSection presets, final RsnVerifyBanner rsnVerifyBanner,
	        final ClaimConsentBanner claimConsentBanner) {
		super(false);
		this.header = header;
		this.body = body;
		this.presets = presets;
		this.rsnVerifyBanner = rsnVerifyBanner;
		this.claimConsentBanner = claimConsentBanner;
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		presets.setAlignmentX(Component.LEFT_ALIGNMENT);
		body.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(buildTop(), BorderLayout.NORTH);
		add(makeScroll(), BorderLayout.CENTER);
		add(footer, BorderLayout.SOUTH);
	}

	private JPanel buildTop()
	{
		final JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
		top.setBackground(ColorScheme.DARK_GRAY_COLOR);
		header.setAlignmentX(Component.LEFT_ALIGNMENT);
		rsnVerifyBanner.setAlignmentX(Component.LEFT_ALIGNMENT);
		claimConsentBanner.setAlignmentX(Component.LEFT_ALIGNMENT);
		top.add(header);
		top.add(rsnVerifyBanner);
		top.add(claimConsentBanner);
		return top;
	}

	public void refresh(final PanelStats stats, final ClanSocketConfig config)
	{
		header.refresh(stats);
		body.refresh(stats, config);
		presets.refresh();
	}

	private JScrollPane makeScroll()
	{
		final JPanel contents = new JPanel()
		{
			@Override
			public Dimension getPreferredSize()
			{
				return new Dimension(PluginPanel.PANEL_WIDTH, super.getPreferredSize().height);
			}
		};
		contents.setLayout(new BoxLayout(contents, BoxLayout.Y_AXIS));
		contents.setBackground(ColorScheme.DARK_GRAY_COLOR);
		contents.add(presets);
		contents.add(body);
		final JScrollPane scroll = new JScrollPane(contents);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.getViewport().setBackground(ColorScheme.DARK_GRAY_COLOR);
		return scroll;
	}
}
