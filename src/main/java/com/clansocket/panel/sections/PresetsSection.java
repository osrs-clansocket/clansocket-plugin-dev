package com.clansocket.panel.sections;

import java.awt.Dimension;
import java.util.OptionalInt;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;

import com.clansocket.ClanSocketConfig;
import com.clansocket.ClanSocketConstants;
import com.clansocket.config.preset.PresetConstants;
import com.clansocket.config.preset.PresetManager;
import com.clansocket.panel.PanelConstants;
import com.clansocket.panel.widgets.CollapsibleSection;

@Singleton
public final class PresetsSection extends JPanel
{
	private final ClanSocketConfig config;
	private final PresetManager presetManager;
	private final ConfigManager configManager;
	private final JLabel[] slotButtons = new JLabel[PresetConstants.MAX_SLOTS];
	private final JButton manualBtn;
	private final JButton clanBtn;
	private final JPanel slotRow;
	private final CollapsibleSection section;

	@Inject
	@SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
	public PresetsSection(final ClanSocketConfig config, final PresetManager presetManager,
	        final ConfigManager configManager, final SpriteManager spriteManager) {
		this.config = config;
		this.presetManager = presetManager;
		this.configManager = configManager;
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setLayout(new DynamicGridLayout(0, 1, 0, 0));
		this.section = PresetSectionWidgets.makeSection(spriteManager);
		this.manualBtn = PresetSectionWidgets.button(PanelConstants.PRESETS_BTN_MANUAL, e -> onModeChange(false));
		this.clanBtn = PresetSectionWidgets.button(PanelConstants.PRESETS_BTN_CLAN, e -> onModeChange(true));
		final JButton saveBtn = PresetSectionWidgets.button(PanelConstants.PRESETS_BTN_SAVE, e -> onSave());
		this.slotRow = PresetSectionWidgets.makeSlotRow(saveBtn, this::onSlotClick, slotButtons);
		section.addItem(PresetSectionWidgets.makeRow(manualBtn, clanBtn));
		add(section);
		refresh();
	}

	public void refresh()
	{
		final boolean clanMode = config.mode() == ClanSocketConfig.ConfigMode.CLAN;
		PresetSectionWidgets.applyModeActive(clanMode ? clanBtn : manualBtn);
		PresetSectionWidgets.applyModeInactive(clanMode ? manualBtn : clanBtn);
		final boolean mounted = slotRow.getParent() != null;
		if (clanMode && mounted)
		{
			section.removeItem(slotRow);
		} else if (!clanMode && !mounted)
		{
			section.addItem(slotRow);
		}
		if (!clanMode)
		{
			refreshSlots();
		}
	}

	private void refreshSlots()
	{
		final int active = presetManager.getActiveSlot();
		for (int i = 0; i < slotButtons.length; i++)
		{
			final int slot = i + 1;
			final JLabel btn = slotButtons[i];
			if (!presetManager.slotExists(slot))
			{
				PresetSectionWidgets.applyEmpty(btn);
			} else if (slot == active)
			{
				PresetSectionWidgets.applyActive(btn);
			} else
			{
				PresetSectionWidgets.applySaved(btn);
			}
		}
	}

	@Override
	public Dimension getMaximumSize()
	{
		return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
	}

	private void onModeChange(final boolean toClan)
	{
		configManager.setConfiguration(ClanSocketConstants.CONFIG_GROUP, ClanSocketConstants.CONFIG_KEY_MODE,
		        toClan ? ClanSocketConfig.ConfigMode.CLAN : ClanSocketConfig.ConfigMode.MANUAL);
		refresh();
	}

	@SuppressWarnings("PMD.UnusedPrivateMethod")
	private void onSlotClick(final int slot, final boolean delete)
	{
		if (delete)
		{
			if (presetManager.delete(slot))
			{
				refresh();
			}
			return;
		}
		if (presetManager.useSlot(slot))
		{
			refresh();
		}
	}

	private void onSave()
	{
		final OptionalInt saved = presetManager.saveCurrent();
		if (saved.isPresent())
		{
			presetManager.setActiveSlot(saved.getAsInt());
			refresh();
		}
	}
}
