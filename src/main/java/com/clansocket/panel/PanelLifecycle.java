package com.clansocket.panel;

import java.awt.image.BufferedImage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.swing.SwingUtilities;

import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import com.clansocket.ClanSocketConfig;

@Singleton
public class PanelLifecycle
{
	private final ClientToolbar clientToolbar;
	private final Provider<ClanSocketPanel> panelProvider;
	private final PanelStats panelStats;
	private final ClanSocketConfig config;
	private final ScheduledExecutorService executor;

	private ClanSocketPanel panel;
	private NavigationButton navButton;
	private ScheduledFuture<?> refreshTask;
	private ScheduledFuture<?> flushTask;

	@Inject
	@SuppressWarnings("checkstyle:ParameterNumber")
	public PanelLifecycle(final ClientToolbar clientToolbar, final Provider<ClanSocketPanel> panelProvider,
	        final PanelStats panelStats, final ClanSocketConfig config, final ScheduledExecutorService executor) {
		this.clientToolbar = clientToolbar;
		this.panelProvider = panelProvider;
		this.panelStats = panelStats;
		this.config = config;
		this.executor = executor;
	}

	public void install()
	{
		panelStats.loadCounts();
		panel = panelProvider.get();
		final BufferedImage icon = ImageUtil.loadImageResource(ClanSocketPanel.class, PanelConstants.ICON_RESOURCE);
		navButton = NavigationButton.builder().tooltip(PanelConstants.NAV_TOOLTIP).icon(icon)
		        .priority(PanelConstants.NAV_PRIORITY).panel(panel).build();
		clientToolbar.addNavigation(navButton);
		refreshTask = executor.scheduleAtFixedRate(this::refresh, 0, PanelConstants.REFRESH_INTERVAL_MS,
		        TimeUnit.MILLISECONDS);
		flushTask = executor.scheduleAtFixedRate(panelStats::flushCounts, PanelConstants.COUNT_FLUSH_INTERVAL_MS,
		        PanelConstants.COUNT_FLUSH_INTERVAL_MS, TimeUnit.MILLISECONDS);
	}

	public void shutdown()
	{
		if (refreshTask != null)
		{
			refreshTask.cancel(false);
			refreshTask = null;
		}
		if (flushTask != null)
		{
			flushTask.cancel(false);
			flushTask = null;
		}
		panelStats.flushCounts();
		if (navButton != null)
		{
			clientToolbar.removeNavigation(navButton);
			navButton = null;
		}
		panel = null;
	}

	private void refresh()
	{
		final ClanSocketPanel current = panel;
		if (current == null)
		{
			return;
		}
		SwingUtilities.invokeLater(() -> current.refresh(panelStats, config));
	}
}
