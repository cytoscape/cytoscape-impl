package org.cytoscape.log.internal;


import java.util.concurrent.BlockingQueue;
import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;

class UserMessagesProcesser extends QueueProcesser
{
	final CyStatusBar statusBar;
	final UserMessagesDialog dialog;

	public UserMessagesProcesser(
			final BlockingQueue<PaxLoggingEvent> queue,
			final CyStatusBar statusBar,
			final UserMessagesDialog dialog)
	{
		super(queue);
		this.statusBar = statusBar;
		this.dialog = dialog;
	}

	public void processEvent(final PaxLoggingEvent event)
	{
		statusBar.setMessage(event.getLevel().toString(), event.getMessage());
		dialog.addMessage(event.getLevel().toString(), event.getMessage());
	}
}
