package org.cytoscape.log.internal;

import java.util.Queue;

import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;

/**
 * @author Pasteur
 */
public class SimpleQueueAppender implements PaxAppender
{
	final Queue<PaxLoggingEvent> queue;

	public SimpleQueueAppender(Queue<PaxLoggingEvent> queue)
	{
		this.queue = queue;
	}

	public void doAppend(PaxLoggingEvent event)
	{
		if (	!event.getLoggerName().startsWith("org.springframework") &&
			(event.getLevel().toString().compareToIgnoreCase("info") == 0 ||
			 event.getLevel().toString().compareToIgnoreCase("warn") == 0))
			queue.offer(event);
	}
}
