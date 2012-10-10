package org.cytoscape.app.internal.net;

import org.cytoscape.work.TaskMonitor;

public class DownloadStatus {
	private final TaskMonitor taskMonitor;
	private volatile boolean isCanceled;

	public DownloadStatus(TaskMonitor taskMonitor) {
		this.taskMonitor = taskMonitor;
	}
	
	public TaskMonitor getTaskMonitor() {
		return taskMonitor;
	}
	
	public boolean isCanceled() {
		return isCanceled;
	}
	
	public void cancel() {
		isCanceled = true;
	}
}
