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

class DelegatingTaskMonitor implements TaskMonitor {
	
	private double expectedNumTasks; 
	private double currentTaskNum = -1.0; // so that the first task is numbered 0

	private TaskMonitor actualTaskMonitor;

	/**
	 * Based on the expected number of tasks, this is the fraction of the overall
	 * task monitor that a given task is allocated. So, if there are 4 tasks 
	 * executed with this task monitor, each task is allocated 0.25 of the 
	 * space in the progress bar.
	 */
	private double fractionOfOverall = 1.0;

	DelegatingTaskMonitor(TaskMonitor tm, int expectedNumTasks) {
		this.actualTaskMonitor = tm;
		this.expectedNumTasks = (double)expectedNumTasks;
		this.fractionOfOverall = 1.0/this.expectedNumTasks;
	}

	public void setTask(final Task newTask) {
		this.currentTaskNum += 1.0;	
	}

	public void setTitle(String title) {
		actualTaskMonitor.setTitle(title);
	}

	public void setStatusMessage(String statusMessage) {
		actualTaskMonitor.setStatusMessage(statusMessage);
	}

	public void setProgress(double progress) {
		if ( progress < 0 ) {
			actualTaskMonitor.setProgress(-1.0);
		} else {
			double completed = currentTaskNum/expectedNumTasks;
			double adjustedProgress = (progress * fractionOfOverall) + completed;
			actualTaskMonitor.setProgress(adjustedProgress);
		}
	}
}
