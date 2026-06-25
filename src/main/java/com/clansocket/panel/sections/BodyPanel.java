package com.clansocket.panel.sections;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import com.clansocket.ClanSocketConfig;
import com.clansocket.ClanSocketConstants;
import com.clansocket.panel.PanelConstants;
import com.clansocket.panel.PanelStats;
import com.clansocket.panel.RateBuffer;
import com.clansocket.panel.widgets.StreamGate;
import com.clansocket.panel.widgets.TelemetryCard;

@Singleton
public class BodyPanel extends JPanel
{
	private final ConfigManager configManager;
	private final List<StreamGate> ordered = buildOrder();
	private final TelemetryCard[] cards = new TelemetryCard[StreamGate.ALL.size()];
	private final int[] rateScratch = new int[RateBuffer.WINDOW_SECONDS];

	@Inject
	@SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
	public BodyPanel(final ConfigManager configManager, final SpriteManager spriteManager) {
		this.configManager = configManager;
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setLayout(new BorderLayout());
		final JPanel grid = new JPanel(new GridLayout(0, 1, 0, PanelConstants.CARD_GRID_GAP));
		grid.setBackground(ColorScheme.DARK_GRAY_COLOR);
		grid.setBorder(new EmptyBorder(PanelConstants.CARD_GRID_PADDING, PanelConstants.CARD_GRID_PADDING,
		        PanelConstants.CARD_GRID_PADDING, PanelConstants.CARD_GRID_PADDING));
		for (final StreamGate gate : ordered)
		{
			final TelemetryCard card = makeCard(gate);
			cards[gate.ordinal()] = card;
			spriteManager.getSpriteAsync(gate.spriteId(), 0, card::setIconImage);
			grid.add(card);
		}
		add(grid, BorderLayout.NORTH);
		alignSparkColumns();
	}

	public void refresh(final PanelStats stats, final ClanSocketConfig config)
	{
		final boolean clanMode = config.mode() == ClanSocketConfig.ConfigMode.CLAN;
		for (final StreamGate gate : ordered)
		{
			final TelemetryCard card = cards[gate.ordinal()];
			card.setActive(gate.isEnabled(config));
			card.setLocked(clanMode);
			if (BodyPanelConstants.COMPACT.contains(gate))
			{
				card.updateInfrequent(stats.count(gate), stats.lastEventAt(gate));
			} else
			{
				stats.fillRateSnapshot(gate, rateScratch);
				card.updateFrequent(stats.count(gate), rateScratch);
			}
		}
	}

	@Override
	public Dimension getMaximumSize()
	{
		return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
	}

	private void alignSparkColumns()
	{
		final FontMetrics fm = getFontMetrics(FontManager.getRunescapeSmallFont());
		final List<TelemetryCard> spark = new ArrayList<>();
		int width = 0;
		for (final StreamGate gate : ordered)
		{
			if (BodyPanelConstants.COMPACT.contains(gate))
			{
				continue;
			}
			width = Math.max(width, fm.stringWidth(gate.displayName()));
			spark.add(cards[gate.ordinal()]);
		}
		final int target = width + PanelConstants.CARD_LABEL_PAD;
		for (final TelemetryCard card : spark)
		{
			card.setLabelWidth(target);
		}
	}

	private TelemetryCard makeCard(final StreamGate gate)
	{
		final TelemetryCard.Mode mode = BodyPanelConstants.COMPACT.contains(gate)
		        ? TelemetryCard.Mode.INFREQUENT
		        : TelemetryCard.Mode.FREQUENT;
		return new TelemetryCard(gate.displayName(), mode, enabled -> configManager
		        .setConfiguration(ClanSocketConstants.CONFIG_GROUP, gate.configKey(), Boolean.toString(enabled)));
	}

	private static List<StreamGate> buildOrder()
	{
		return StreamGate.ALL.stream().filter(g -> !BodyPanelConstants.HIDDEN.contains(g))
		        .sorted(Comparator.comparing(BodyPanelConstants.COMPACT::contains)).collect(Collectors.toList());
	}
}
