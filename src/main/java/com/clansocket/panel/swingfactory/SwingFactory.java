package com.clansocket.panel.swingfactory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import com.clansocket.panel.PanelStats;

public final class SwingFactory
{
	@SuppressWarnings("checkstyle:StaticFinalOutsideConstants")
	private static final Map<PanelStats.ConnectionState, Color> CONNECTION_COLORS = Map.of(
	        PanelStats.ConnectionState.CONNECTED, ColorScheme.PROGRESS_COMPLETE_COLOR,
	        PanelStats.ConnectionState.CONNECTING, ColorScheme.PROGRESS_INPROGRESS_COLOR,
	        PanelStats.ConnectionState.RECONNECTING, ColorScheme.PROGRESS_INPROGRESS_COLOR);

	private SwingFactory() {
	}

	public static JLabel smallLabel(final String text, final Color color)
	{
		return smallLabel(text, SwingConstants.LEADING, color);
	}

	public static JLabel smallLabel(final String text, final int alignment, final Color color)
	{
		final JLabel l = new JLabel(text, alignment);
		l.setFont(FontManager.getRunescapeSmallFont());
		l.setForeground(color);
		return l;
	}

	public static <T extends JComponent> T applySmall(final T component, final Color fg)
	{
		component.setFont(FontManager.getRunescapeSmallFont());
		if (fg != null)
		{
			component.setForeground(fg);
		}
		return component;
	}

	public static <T extends JComponent> T applyColors(final T component, final Color bg, final Color fg)
	{
		component.setBackground(bg);
		component.setForeground(fg);
		return component;
	}

	public static Color colorFor(final PanelStats.ConnectionState state)
	{
		return CONNECTION_COLORS.getOrDefault(state, ColorScheme.PROGRESS_ERROR_COLOR);
	}

	public static JPanel transparentRow(final int hgap, final Component leading, final Component fill,
	        final String fillSide)
	{
		final JPanel row = new JPanel(new BorderLayout(hgap, 0));
		row.setOpaque(false);
		row.add(leading, BorderLayout.WEST);
		row.add(fill, fillSide);
		return row;
	}

	public static void onHoverColorSwap(final Component c, final Color hoverFg, final Color restingFg,
	        final Runnable onClick)
	{
		c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		c.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(final MouseEvent e)
			{
				onClick.run();
			}

			@Override
			public void mouseEntered(final MouseEvent e)
			{
				c.setForeground(hoverFg);
			}

			@Override
			public void mouseExited(final MouseEvent e)
			{
				c.setForeground(restingFg);
			}
		});
	}

	public static void onMousePressed(final Component c, final Consumer<MouseEvent> action)
	{
		c.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(final MouseEvent e)
			{
				action.accept(e);
			}
		});
	}
}
