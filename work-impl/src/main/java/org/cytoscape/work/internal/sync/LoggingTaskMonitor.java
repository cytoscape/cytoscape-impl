package org.cytoscape.work.internal.sync;

/*
 * #%L
 * org.cytoscape.work-impl
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


import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class LoggingTaskMonitor implements TaskMonitor {
	
	private static final Logger logger = LoggerFactory.getLogger(LoggingTaskMonitor.class);

	private Task task;

	public LoggingTaskMonitor() {
	}

	public void setTask(final Task newTask) {
		this.task = newTask;
	}

	public void setTitle(String title) {
		logger.info("Task (" + task.toString() + ") title: " + title);
	}

	public void setStatusMessage(String statusMessage) {
		logger.info("Task (" + task.toString() + ") status: " + statusMessage);
	}

	public void setProgress(double progress) {
		int prog = (int) Math.floor(progress * 100);
		logger.info("Task (" + task.toString() + ") progress: " + prog + "%");
	}

	public void showException(Exception exception) {
		logger.error("Exception executing task.", exception);
	}
}
