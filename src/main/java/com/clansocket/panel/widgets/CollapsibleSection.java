package com.clansocket.panel.widgets;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.util.ImageUtil;

import com.clansocket.panel.PanelConstants;
import com.clansocket.panel.swingfactory.SwingFactory;

public class CollapsibleSection extends JPanel
{
	private final JLabel iconLabel = new JLabel();
	private final JPanel body;
	private final JLabel chevron = new JLabel(PanelConstants.CHEVRON_EXPANDED);
	private boolean expanded = true;

	@SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
	public CollapsibleSection(final String title, final String iconName) {
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		final ImageIcon icon = loadIcon(iconName);
		if (icon != null)
		{
			iconLabel.setIcon(icon);
		}
		body = new JPanel();
		body.setLayout(new DynamicGridLayout(0, 1, 0, 0));
		body.setBackground(ColorScheme.DARK_GRAY_COLOR);
		add(makeHeader(title), BorderLayout.NORTH);
		add(body, BorderLayout.CENTER);
	}

	public void addItem(final Component c)
	{
		body.add(c);
		body.revalidate();
		body.repaint();
	}

	public void removeItem(final Component c)
	{
		body.remove(c);
		body.revalidate();
		body.repaint();
	}

	public void setExpanded(final boolean value)
	{
		if (value == expanded)
		{
			return;
		}
		expanded = value;
		body.setVisible(value);
		chevron.setText(value ? PanelConstants.CHEVRON_EXPANDED : PanelConstants.CHEVRON_COLLAPSED);
		revalidate();
		repaint();
	}

	@Override
	public Dimension getMaximumSize()
	{
		return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
	}

	public void setIconImage(final BufferedImage img)
	{
		if (img == null)
		{
			return;
		}
		final int w = Math.max(1, img.getWidth() / 2);
		final int h = Math.max(1, img.getHeight() / 2);
		final Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
		SwingUtilities.invokeLater(() ->
		{
			iconLabel.setIcon(new ImageIcon(scaled));
			iconLabel.revalidate();
			iconLabel.repaint();
		});
	}

	private JPanel makeHeader(final String title)
	{
		final JPanel header = new JPanel(new BorderLayout(PanelConstants.SECTION_HEADER_GAP, 0));
		header.setBackground(ColorScheme.DARK_GRAY_COLOR);
		header.setBorder(new EmptyBorder(PanelConstants.SECTION_HEADER_PADDING_Y, PanelConstants.ROW_PADDING_X,
		        PanelConstants.SECTION_HEADER_PADDING_Y, PanelConstants.ROW_PADDING_X));
		header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		SwingFactory.onMousePressed(header, e -> setExpanded(!expanded));
		header.add(makeLeft(title), BorderLayout.WEST);
		SwingFactory.applySmall(chevron, ColorScheme.MEDIUM_GRAY_COLOR);
		header.add(chevron, BorderLayout.EAST);
		return header;
	}

	private JPanel makeLeft(final String title)
	{
		final JPanel left = new JPanel(new BorderLayout(PanelConstants.SECTION_HEADER_GAP, 0));
		left.setOpaque(false);
		left.add(iconLabel, BorderLayout.WEST);
		left.add(SwingFactory.smallLabel(title, PanelConstants.COLOR_GOLD_TEXT), BorderLayout.CENTER);
		return left;
	}

	@SuppressWarnings("checkstyle:IllegalCatch")
	private static ImageIcon loadIcon(final String name)
	{
		if (name == null)
		{
			return null;
		}
		try
		{
			final BufferedImage img = ImageUtil.loadImageResource(CollapsibleSection.class,
			        String.format(PanelConstants.SECTION_ICON_PATH_FMT, name));
			return img == null ? null : new ImageIcon(img);
		} catch (final RuntimeException ex)
		{
			return null;
		}
	}
}
