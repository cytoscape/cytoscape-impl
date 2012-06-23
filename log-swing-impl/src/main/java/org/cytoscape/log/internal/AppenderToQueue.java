package org.cytoscape.log.internal;

import java.util.concurrent.BlockingQueue;

import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;

public class AppenderToQueue implements PaxAppender
{
	final BlockingQueue<PaxLoggingEvent> queue;

	public AppenderToQueue(BlockingQueue<PaxLoggingEvent> queue)
	{
		this.queue = queue;
	}

	public void doAppend(PaxLoggingEvent event)
	{
		queue.offer(event);
	}
}
