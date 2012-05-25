
package org.cytoscape.work.internal.sync;


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
		logger.error("Exception executing task!", exception);
	}
}
