package com.clansocket.panel.widgets;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import net.runelite.client.ui.ColorScheme;

import com.clansocket.panel.PanelConstants;
import com.clansocket.panel.TelemetryFormat;
import com.clansocket.panel.swingfactory.SwingFactory;

public class TelemetryCard extends ToggleCard
{
	public enum Mode
	{
		FREQUENT, INFREQUENT
	}

	private final JLabel iconLabel = new JLabel();
	private final JLabel nameLabel;
	private final JLabel rateLabel = new JLabel("", SwingConstants.RIGHT);
	private final JLabel lastLabel = new JLabel(String.format(PanelConstants.LAST_FORMAT, PanelConstants.AGO_NEVER));
	private final Sparkline sparkline = new Sparkline();

	@SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
	public TelemetryCard(final String name, final Mode mode, final Consumer<Boolean> onToggle) {
		super(onToggle);
		setLayout(new BorderLayout(PanelConstants.ROW_COUNT_GAP, 0));
		nameLabel = SwingFactory.smallLabel(name, ColorScheme.LIGHT_GRAY_COLOR);
		add(SwingFactory.transparentRow(PanelConstants.ICON_GAP, iconLabel, nameLabel, BorderLayout.CENTER),
		        BorderLayout.WEST);
		add(makeMiddle(mode), BorderLayout.CENTER);
		applyVisual();
		setSentTooltip(0);
	}

	private JPanel makeMiddle(final Mode mode)
	{
		final JPanel mid = new JPanel(new BorderLayout(PanelConstants.ROW_COUNT_GAP, 0));
		mid.setOpaque(false);
		if (mode == Mode.FREQUENT)
		{
			SwingFactory.applySmall(rateLabel, null);
			mid.add(sparkline, BorderLayout.CENTER);
			mid.add(rateLabel, BorderLayout.EAST);
		} else
		{
			SwingFactory.applySmall(lastLabel, ColorScheme.MEDIUM_GRAY_COLOR);
			mid.add(lastLabel, BorderLayout.EAST);
		}
		return mid;
	}

	public void setIconImage(final BufferedImage img)
	{
		if (img == null)
		{
			return;
		}
		final Image scaled = img.getScaledInstance(PanelConstants.CARD_ICON_SIZE, PanelConstants.CARD_ICON_SIZE,
		        Image.SCALE_SMOOTH);
		SwingUtilities.invokeLater(() -> iconLabel.setIcon(new ImageIcon(scaled)));
	}

	@SuppressWarnings("PMD.UseVarargs")
	public void updateFrequent(final long sentTotal, final int[] samples)
	{
		if (!active())
		{
			return;
		}
		setSentTooltip(sentTotal);
		sparkline.setSamples(samples);
	}

	public void updateInfrequent(final long sentTotal, final long lastEventAtMs)
	{
		if (!active())
		{
			return;
		}
		setSentTooltip(sentTotal);
		lastLabel.setText(String.format(PanelConstants.LAST_FORMAT, TelemetryFormat.formatAgo(lastEventAtMs)));
		lastLabel.setForeground(TelemetryFormat.dotColor(lastEventAtMs));
	}

	@Override
	protected void renderState(final boolean enabled)
	{
		nameLabel.setForeground(enabled ? PanelConstants.COLOR_GOLD_TEXT : ColorScheme.MEDIUM_GRAY_COLOR);
		if (enabled)
		{
			renderEnabled();
			return;
		}
		rateLabel.setText(PanelConstants.STATUS_GLYPH_DISABLED);
		rateLabel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
		lastLabel.setText(PanelConstants.STATUS_GLYPH_DISABLED);
		lastLabel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
		sparkline.setSamples(new int[0]);
	}

	private void renderEnabled()
	{
		rateLabel.setText("");
		lastLabel.setText(String.format(PanelConstants.LAST_FORMAT, PanelConstants.AGO_NEVER));
		lastLabel.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
	}

	private void setSentTooltip(final long sentTotal)
	{
		setToolTipText(String.format(PanelConstants.SENT_FORMAT, sentTotal));
	}

	public void setLabelWidth(final int width)
	{
		final Dimension pref = nameLabel.getPreferredSize();
		nameLabel.setPreferredSize(new Dimension(width, pref.height));
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(super.getPreferredSize().width, PanelConstants.TELEMETRY_CARD_HEIGHT);
	}

	@Override
	public Dimension getMaximumSize()
	{
		return new Dimension(Integer.MAX_VALUE, PanelConstants.TELEMETRY_CARD_HEIGHT);
	}
}
