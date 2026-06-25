package com.clansocket.bus;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Singleton;

import com.clansocket.ClanSocketConstants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public final class PendingQueue
{
	private final Deque<String> queue = new ConcurrentLinkedDeque<>();
	private final AtomicInteger size = new AtomicInteger();
	private long dropped;

	public void offer(final String json)
	{
		queue.offerLast(json);
		if (size.incrementAndGet() <= ClanSocketConstants.QUEUE_CAP)
		{
			return;
		}
		if (queue.pollFirst() != null)
		{
			size.decrementAndGet();
			noteDropped();
		}
	}

	public String poll()
	{
		final String head = queue.pollFirst();
		if (head != null)
		{
			size.decrementAndGet();
		}
		return head;
	}

	public void clear()
	{
		queue.clear();
		size.set(0);
	}

	private void noteDropped()
	{
		dropped++;
		if (dropped % ClanSocketConstants.QUEUE_DROP_LOG_INTERVAL == 0)
		{
			log.warn("ClanSocket WS pending queue at cap ({}), dropped {} events while disconnected",
			        ClanSocketConstants.QUEUE_CAP, dropped);
		}
	}
}
