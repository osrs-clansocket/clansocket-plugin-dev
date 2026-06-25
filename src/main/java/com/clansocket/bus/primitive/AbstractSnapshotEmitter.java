package com.clansocket.bus.primitive;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import net.runelite.api.Client;

import org.slf4j.Logger;

import com.clansocket.bus.EventBatcher;
import com.clansocket.protocol.common.Payload;

@SuppressWarnings("checkstyle:IllegalCatch")
public abstract class AbstractSnapshotEmitter
{
	@Inject
	protected Client client;
	@Inject
	protected EventBatcher batcher;

	private final String listField;
	private final String emitKind;
	private final Logger logger;
	private volatile String lastFingerprint;
	private volatile Payload lastSnapshot;

	protected AbstractSnapshotEmitter(final String listField, final String emitKind, final Logger logger) {
		this.listField = listField;
		this.emitKind = emitKind;
		this.logger = logger;
	}

	protected abstract Optional<Payload> buildSnapshot();

	public final void clearFingerprint()
	{
		lastFingerprint = null;
	}

	public final Optional<Payload> snapshotNow()
	{
		return Optional.ofNullable(lastSnapshot);
	}

	public final void emit()
	{
		try
		{
			final Optional<Payload> opt = buildSnapshot();
			if (opt.isEmpty())
			{
				return;
			}
			final Payload snapshot = opt.get();
			lastSnapshot = snapshot;
			final String fp = (String) snapshot.get("fingerprint");
			if (fp.equals(lastFingerprint))
			{
				return;
			}
			lastFingerprint = fp;
			batcher.enqueue(snapshot);
			final Object list = snapshot.get(listField);
			final int size = list instanceof List ? ((List<?>) list).size() : 0;
			logger.debug("ClanSocket {} clan={} count={} fingerprint={}", emitKind, snapshot.get("clanName"), size, fp);
		} catch (final RuntimeException t)
		{
			logger.debug("ClanSocket {} snapshot failed", emitKind, t);
		}
	}
}
