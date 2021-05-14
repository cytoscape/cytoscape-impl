package org.cytoscape.work.internal.task;

import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.internal.view.TaskMediator;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
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

public class CyUserLogAppender implements PaxAppender {

	private final TaskMediator taskMediator;
	private final TaskHistory history;

	public CyUserLogAppender(TaskMediator taskMediator, TaskHistory history) {
		this.taskMediator = taskMediator;
		this.history = history;
	}

	@Override
	public void doAppend(final PaxLoggingEvent event) {
		final TaskMonitor.Level level = getCorrespondingLevel(event.getLevel().toInt());
		final String message = event.getMessage();
		
		history.addUnnestedMessage(level, message);
		taskMediator.setTitle(level, message);

		if (event.getThrowableStrRep() != null) {
			for (String line : event.getThrowableStrRep())
				System.err.println(line);
		}
	}

	private static TaskMonitor.Level getCorrespondingLevel(final int level) {
		switch (level) {
			case PaxLogger.LEVEL_ERROR:
				return TaskMonitor.Level.ERROR;
			case PaxLogger.LEVEL_WARNING:
				return TaskMonitor.Level.WARN;
			default:
				return TaskMonitor.Level.INFO;
		}
	}
}
