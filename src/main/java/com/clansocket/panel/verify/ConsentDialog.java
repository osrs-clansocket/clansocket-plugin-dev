package com.clansocket.panel.verify;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import net.runelite.client.util.ImageUtil;

import com.clansocket.panel.PanelConstants;

final class ConsentDialog
{
	private ConsentDialog() {
	}

	static void confirm(final Component parent, final String title, final String body, final Consumer<Boolean> callback)
	{
		SwingUtilities.invokeLater(() ->
		{
			final BufferedImage img = ImageUtil.loadImageResource(PanelConstants.class, PanelConstants.ICON_RESOURCE);
			final ImageIcon icon = img == null ? null : new ImageIcon(img);
			final int choice = JOptionPane.showConfirmDialog(parent, body, title, JOptionPane.YES_NO_OPTION,
			        JOptionPane.QUESTION_MESSAGE, icon);
			callback.accept(choice == JOptionPane.YES_OPTION);
		});
	}
}
