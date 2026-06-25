package com.clansocket.panel.sections;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import net.runelite.api.gameval.SpriteID;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;

import com.clansocket.panel.PanelConstants;
import com.clansocket.panel.swingfactory.SwingFactory;
import com.clansocket.panel.widgets.CollapsibleSection;

final class PresetSectionWidgets
{
	private PresetSectionWidgets() {
	}

	static CollapsibleSection makeSection(final SpriteManager spriteManager)
	{
		final CollapsibleSection s = new CollapsibleSection("Presets", null);
		spriteManager.getSpriteAsync(SpriteID.SideIcons.MUSIC, 0, s::setIconImage);
		return s;
	}

	static JPanel makeRow(final JButton... btns)
	{
		return paddedWrap(oneRowGrid(PanelConstants.PRESETS_BUTTON_GAP, btns));
	}

	static JButton button(final String label, final ActionListener action)
	{
		final JButton b = SwingFactory.applySmall(new JButton(label), null);
		b.addActionListener(action);
		return b;
	}

	static void applyModeActive(final JButton btn)
	{
		btn.setBackground(PanelConstants.COLOR_GOLD_FILL);
		btn.setForeground(Color.BLACK);
	}

	static void applyModeInactive(final JButton btn)
	{
		btn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		btn.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
	}

	static JLabel slotButton(final int slot, final Consumer<MouseEvent> onClick)
	{
		final JLabel btn = SwingFactory.applySmall(new JLabel(Integer.toString(slot), SwingConstants.CENTER), null);
		btn.setOpaque(true);
		btn.setBorder(BorderFactory.createLineBorder(ColorScheme.DARK_GRAY_HOVER_COLOR, 1));
		btn.setPreferredSize(new Dimension(0, PanelConstants.SLOT_BUTTON_HEIGHT));
		btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		SwingFactory.onMousePressed(btn, onClick);
		applyEmpty(btn);
		return btn;
	}

	static void applyEmpty(final JLabel btn)
	{
		SwingFactory.applyColors(btn, ColorScheme.DARKER_GRAY_COLOR, ColorScheme.MEDIUM_GRAY_COLOR);
	}

	static void applySaved(final JLabel btn)
	{
		SwingFactory.applyColors(btn, ColorScheme.DARK_GRAY_HOVER_COLOR, ColorScheme.LIGHT_GRAY_COLOR);
	}

	static void applyActive(final JLabel btn)
	{
		SwingFactory.applyColors(btn, PanelConstants.COLOR_GOLD_FILL, Color.BLACK);
	}

	static JPanel makeSlotRow(final JButton saveBtn, final BiConsumer<Integer, Boolean> onClick, final JLabel... slots)
	{
		initSlotButtons(onClick, slots);
		final JPanel row = new JPanel(new BorderLayout(PanelConstants.PRESET_NUM_BUTTON_GAP, 0));
		row.setOpaque(false);
		row.add(saveBtn, BorderLayout.WEST);
		row.add(oneRowGrid(PanelConstants.PRESET_NUM_BUTTON_GAP, slots), BorderLayout.CENTER);
		return paddedWrap(row);
	}

	private static void initSlotButtons(final BiConsumer<Integer, Boolean> onClick, final JLabel... slots)
	{
		for (int i = 0; i < slots.length; i++)
		{
			final int slot = i + 1;
			slots[i] = slotButton(slot, e -> onClick.accept(slot, e.isShiftDown()));
		}
	}

	private static JPanel oneRowGrid(final int gap, final JComponent... items)
	{
		final JPanel grid = new JPanel(new GridLayout(1, items.length, gap, 0));
		grid.setOpaque(false);
		for (final JComponent item : items)
		{
			grid.add(item);
		}
		return grid;
	}

	private static JPanel paddedWrap(final JComponent content)
	{
		final JPanel wrap = new JPanel(new BorderLayout());
		wrap.setOpaque(false);
		wrap.setBorder(new EmptyBorder(PanelConstants.ROW_PADDING_Y, PanelConstants.ROW_PADDING_X,
		        PanelConstants.ROW_PADDING_Y, PanelConstants.ROW_PADDING_X));
		wrap.add(content, BorderLayout.NORTH);
		return wrap;
	}
}
