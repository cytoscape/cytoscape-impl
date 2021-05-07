package org.cytoscape.log.internal;

/*
 * #%L
 * Cytoscape Log Swing Impl (log-swing-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

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
