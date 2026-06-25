package com.clansocket.transport;

import javax.inject.Singleton;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public final class LifecycleListeners
{
	@Setter
	private Runnable onOpenListener;
	@Setter
	private Runnable onReidentifyListener;

	@SuppressWarnings("checkstyle:IllegalCatch")
	public void fireOnOpen()
	{
		runListener(onOpenListener, "onOpen");
	}

	@SuppressWarnings("checkstyle:IllegalCatch")
	public void fireOnReidentify()
	{
		runListener(onReidentifyListener, "onReidentify");
	}

	@SuppressWarnings("checkstyle:IllegalCatch")
	private void runListener(final Runnable listener, final String label)
	{
		if (listener == null)
		{
			return;
		}
		try
		{
			listener.run();
		} catch (final RuntimeException e)
		{
			log.warn("ClanSocket {} listener failed: {}", label, e.getMessage());
		}
	}
}
