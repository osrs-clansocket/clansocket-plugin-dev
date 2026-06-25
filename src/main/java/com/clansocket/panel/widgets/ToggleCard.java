package com.clansocket.panel.widgets;

import java.awt.Color;
import java.awt.Cursor;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import net.runelite.client.ui.ColorScheme;

import com.clansocket.panel.PanelConstants;
import com.clansocket.panel.swingfactory.SwingFactory;

public abstract class ToggleCard extends JPanel
{
	private boolean active;
	private boolean locked;

	@SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
	protected ToggleCard(final Consumer<Boolean> onToggle) {
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		SwingFactory.onMousePressed(this, e ->
		{
			if (locked)
			{
				return;
			}
			active = !active;
			applyVisual();
			onToggle.accept(active);
		});
	}

	public final void setActive(final boolean value)
	{
		if (value == active)
		{
			return;
		}
		active = value;
		applyVisual();
	}

	public final void setLocked(final boolean value)
	{
		if (value == locked)
		{
			return;
		}
		locked = value;
		setCursor(Cursor.getPredefinedCursor(value ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));
	}

	protected final boolean active()
	{
		return active;
	}

	protected final void applyVisual()
	{
		final Color borderColor = active ? PanelConstants.COLOR_GOLD_TOGGLE : ColorScheme.DARKER_GRAY_HOVER_COLOR;
		setBackground(active ? ColorScheme.DARKER_GRAY_COLOR : ColorScheme.DARK_GRAY_COLOR);
		setBorder(new CompoundBorder(BorderFactory.createLineBorder(borderColor, 1),
		        new EmptyBorder(PanelConstants.CARD_PADDING_Y, PanelConstants.CARD_PADDING_X,
		                PanelConstants.CARD_PADDING_Y, PanelConstants.CARD_PADDING_X)));
		renderState(active);
	}

	protected abstract void renderState(boolean enabled);
}
