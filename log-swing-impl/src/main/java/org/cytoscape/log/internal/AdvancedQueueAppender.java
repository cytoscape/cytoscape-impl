package org.cytoscape.log.internal;

import java.util.concurrent.BlockingQueue;

import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;

/**
 * @author Pasteur
 */
public class AdvancedQueueAppender implements PaxAppender
{
	final BlockingQueue<PaxLoggingEvent> queue;

	public AdvancedQueueAppender(BlockingQueue<PaxLoggingEvent> queue)
	{
		this.queue = queue;
	}

	public void doAppend(PaxLoggingEvent event)
	{
		queue.offer(event);
	}
}
