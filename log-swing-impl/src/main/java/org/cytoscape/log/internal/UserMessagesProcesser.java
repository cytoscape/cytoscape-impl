package org.cytoscape.log.internal;

/*
 * #%L
 * Cytoscape Log Swing Impl (log-swing-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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
