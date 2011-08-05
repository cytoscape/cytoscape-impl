package org.cytoscape.log.internal;

import java.util.concurrent.BlockingQueue;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;

/**
 * @author Pasteur
 */
abstract class QueueProcesser implements Runnable
{
	public abstract void processEvent(PaxLoggingEvent event);

	final BlockingQueue<PaxLoggingEvent> queue;

	public QueueProcesser(BlockingQueue<PaxLoggingEvent> queue)
	{
		this.queue = queue;
	}

	public void run()
	{
		while (true)
		{
			PaxLoggingEvent event = null;
			try
			{
				event = queue.take();
			}
			catch (InterruptedException e)
			{
				break;
			}
			processEvent(event);
		}
	}
}
