package org.cytoscape.work.internal.sync;


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
