package com.clansocket.panel.verify;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import net.runelite.client.ui.ColorScheme;

import com.clansocket.panel.PanelConstants;
import com.clansocket.panel.swingfactory.SwingFactory;

final class ConsentBannerLayout
{
	private final JPanel host;
	private final JLabel countdown = SwingFactory.smallLabel("", SwingConstants.RIGHT, ColorScheme.MEDIUM_GRAY_COLOR);
	private final JLabel subtitle = SwingFactory.smallLabel("", ColorScheme.LIGHT_GRAY_COLOR);
	private final JButton confirmBtn = new JButton(VerifyConstants.LABEL_CONFIRM);
	private final JButton rejectBtn = new JButton(VerifyConstants.LABEL_REJECT);

	ConsentBannerLayout(final JPanel host, final String titleText, final ActionListener onConfirm,
	        final ActionListener onReject) {
		this.host = host;
		host.setLayout(new BorderLayout(0, VerifyConstants.GAP));
		host.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		host.setBorder(buildBorder(VerifyConstants.PULSE_GRAY));
		host.setAlignmentX(Component.LEFT_ALIGNMENT);
		final JLabel title = SwingFactory.smallLabel(titleText, PanelConstants.COLOR_GOLD_TEXT);
		confirmBtn.addActionListener(onConfirm);
		rejectBtn.addActionListener(onReject);
		host.add(SwingFactory.transparentRow(VerifyConstants.GAP, title, countdown, BorderLayout.EAST),
		        BorderLayout.NORTH);
		host.add(subtitle, BorderLayout.CENTER);
		host.add(buildButtons(), BorderLayout.SOUTH);
	}

	void applySubtitle(final String text)
	{
		subtitle.setText(text);
	}

	void applyCountdown(final long msLeft)
	{
		final long clamped = Math.max(0L, msLeft);
		countdown.setText(String.format(VerifyConstants.FMT_COUNTDOWN, clamped / VerifyConstants.MS_PER_MINUTE,
		        (clamped % VerifyConstants.MS_PER_MINUTE) / VerifyConstants.MS_PER_SECOND));
	}

	void applyPulse(final Color color)
	{
		host.setBorder(buildBorder(color));
	}

	private JPanel buildButtons()
	{
		final JPanel row = new JPanel(new GridLayout(1, 2, VerifyConstants.GAP, 0));
		row.setOpaque(false);
		SwingFactory.applySmall(confirmBtn, null);
		SwingFactory.applySmall(rejectBtn, null);
		SwingFactory.applyColors(confirmBtn, VerifyConstants.PULSE_GREEN, Color.WHITE);
		SwingFactory.applyColors(rejectBtn, VerifyConstants.BTN_REJECT, Color.WHITE);
		confirmBtn.setFocusPainted(false);
		rejectBtn.setFocusPainted(false);
		row.add(confirmBtn);
		row.add(rejectBtn);
		return row;
	}

	private static CompoundBorder buildBorder(final Color stroke)
	{
		return new CompoundBorder(new LineBorder(stroke, VerifyConstants.BORDER_WIDTH),
		        new EmptyBorder(PanelConstants.ROW_PADDING_Y, PanelConstants.ROW_PADDING_X,
		                PanelConstants.ROW_PADDING_Y, PanelConstants.ROW_PADDING_X));
	}
}
