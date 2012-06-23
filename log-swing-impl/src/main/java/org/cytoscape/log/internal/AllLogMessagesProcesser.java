package org.cytoscape.log.internal;

import java.util.concurrent.BlockingQueue;
import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;

class AllLogMessagesProcesser extends QueueProcesser
{
	final ConsoleDialog dialog;

	public AllLogMessagesProcesser(
			final BlockingQueue<PaxLoggingEvent> queue,
			final ConsoleDialog dialog)
	{
		super(queue);
		this.dialog = dialog;
	}

	public void processEvent(final PaxLoggingEvent event)
	{
		dialog.addLogEvent(event);
	}
}
