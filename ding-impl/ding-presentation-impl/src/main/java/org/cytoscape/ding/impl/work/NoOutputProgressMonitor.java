package org.cytoscape.ding.impl.work;

public class NoOutputProgressMonitor implements ProgressMonitor {

	private boolean canceled;
	private double progress = 0.0;
	
	@Override
	public void cancel() {
		canceled = true;
	}

	@Override
	public boolean isCancelled() {
		return canceled;
	}

	@Override
	public void addProgress(double progress) {
		this.progress += progress;
	}

	@Override
	public void done() {
		this.progress = 1.0;
	}
	
	public double getProgress() {
		return progress;
	}

	@Override
	public void start(String taskName) {
	}
}
