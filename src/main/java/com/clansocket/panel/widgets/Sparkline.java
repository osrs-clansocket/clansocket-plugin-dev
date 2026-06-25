package com.clansocket.panel.widgets;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;

import net.runelite.client.ui.ColorScheme;

import com.clansocket.panel.PanelConstants;

public class Sparkline extends JComponent
{
	private int[] samples = new int[0];

	@SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
	public Sparkline() {
		setPreferredSize(new Dimension(PanelConstants.SPARK_W, PanelConstants.SPARK_H));
		setOpaque(false);
	}

	@SuppressWarnings("PMD.UseVarargs")
	public void setSamples(final int[] data)
	{
		if (data == null || data.length == 0)
		{
			this.samples = new int[0];
			repaint();
			return;
		}
		if (this.samples.length != data.length)
		{
			this.samples = new int[data.length];
		}
		System.arraycopy(data, 0, this.samples, 0, data.length);
		repaint();
	}

	@Override
	public Dimension getMaximumSize()
	{
		return new Dimension(PanelConstants.SPARK_W, PanelConstants.SPARK_H);
	}

	@Override
	protected void paintComponent(final Graphics g)
	{
		super.paintComponent(g);
		if (samples.length < 2)
		{
			return;
		}
		final Graphics2D g2 = (Graphics2D) g.create();
		try
		{
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			drawLine(g2);
		} finally
		{
			g2.dispose();
		}
	}

	private void drawLine(final Graphics2D g2)
	{
		final int w = getWidth();
		final int h = getHeight();
		final int max = computeMax();
		if (max <= 0)
		{
			g2.setColor(ColorScheme.MEDIUM_GRAY_COLOR);
			g2.drawLine(0, h - 1, w, h - 1);
			return;
		}
		g2.setColor(PanelConstants.COLOR_GOLD_FILL);
		int prevX = 0;
		int prevY = pointY(samples[0], max, h);
		for (int i = 1; i < samples.length; i++)
		{
			final int x = (int) ((long) i * (w - 1) / (samples.length - 1));
			final int y = pointY(samples[i], max, h);
			g2.drawLine(prevX, prevY, x, y);
			prevX = x;
			prevY = y;
		}
	}

	private int computeMax()
	{
		int m = 0;
		for (final int v : samples)
		{
			if (v > m)
			{
				m = v;
			}
		}
		return m;
	}

	private static int pointY(final int value, final int max, final int h)
	{
		if (max <= 0)
		{
			return h - 1;
		}
		final int top = 1;
		final int bottom = h - 1;
		final int range = bottom - top;
		return bottom - (int) ((long) value * range / max);
	}

}
