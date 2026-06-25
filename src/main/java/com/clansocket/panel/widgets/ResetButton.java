package com.clansocket.panel.widgets;

import javax.swing.JLabel;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import com.clansocket.panel.PanelConstants;
import com.clansocket.panel.swingfactory.SwingFactory;

public class ResetButton extends JLabel
{
	@SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
	public ResetButton(final String text, final String tooltip, final Runnable onClick) {
		super(text);
		setFont(FontManager.getRunescapeSmallFont());
		setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		setToolTipText(tooltip);
		SwingFactory.onHoverColorSwap(this, PanelConstants.COLOR_GOLD_TEXT, ColorScheme.LIGHT_GRAY_COLOR, onClick);
	}
}
