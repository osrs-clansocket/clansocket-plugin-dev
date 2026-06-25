package com.clansocket.panel;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.client.config.ConfigManager;

import com.clansocket.panel.widgets.StreamGate;

@Singleton
public class PanelStats
{
	public enum ConnectionState
	{
		DISCONNECTED, CONNECTING, CONNECTED, RECONNECTING
	}

	private final ConfigManager configManager;
	private final AtomicReference<ConnectionState> connectionState = new AtomicReference<>(
	        ConnectionState.DISCONNECTED);
	private volatile String endpoint = "";
	private volatile String clanReason;
	private final AtomicLong[] counts;
	private final RateBuffer[] rates;

	@Inject
	public PanelStats(final ConfigManager configManager) {
		this.configManager = configManager;
		final int n = StreamGate.ALL.size();
		this.counts = new AtomicLong[n];
		this.rates = new RateBuffer[n];
		for (int i = 0; i < n; i++)
		{
			counts[i] = new AtomicLong();
			rates[i] = new RateBuffer();
		}
	}

	public ConnectionState getConnectionState()
	{
		return connectionState.get();
	}

	public String getEndpoint()
	{
		return endpoint;
	}

	public String getClanReason()
	{
		return clanReason;
	}

	public void setClanReason(final String reason)
	{
		this.clanReason = reason;
	}

	public long count(final StreamGate gate)
	{
		return counts[gate.ordinal()].get();
	}

	public void bump(final StreamGate gate)
	{
		final int idx = gate.ordinal();
		counts[idx].incrementAndGet();
		rates[idx].bump();
	}

	public int[] rateSnapshot(final StreamGate gate)
	{
		return rates[gate.ordinal()].snapshot();
	}

	public void fillRateSnapshot(final StreamGate gate, final int[] out)
	{
		rates[gate.ordinal()].fillSnapshot(out);
	}

	public long lastEventAt(final StreamGate gate)
	{
		return rates[gate.ordinal()].lastEventAt();
	}

	public void loadCounts()
	{
		PanelStatsPersistence.load(configManager, counts, rates);
	}

	public void flushCounts()
	{
		PanelStatsPersistence.flush(configManager, counts, rates);
	}

	public void resetCounts()
	{
		PanelStatsPersistence.reset(configManager, counts, rates);
	}

	public void markConnecting(final String target)
	{
		endpoint = target == null ? "" : target;
		connectionState.set(ConnectionState.CONNECTING);
	}

	public void markConnected()
	{
		connectionState.set(ConnectionState.CONNECTED);
	}

	public void markDisconnected()
	{
		connectionState.set(ConnectionState.DISCONNECTED);
	}

	public void markReconnectAttempt(final int attempt)
	{
		connectionState.set(attempt == 0 ? ConnectionState.RECONNECTING : ConnectionState.DISCONNECTED);
	}
}
