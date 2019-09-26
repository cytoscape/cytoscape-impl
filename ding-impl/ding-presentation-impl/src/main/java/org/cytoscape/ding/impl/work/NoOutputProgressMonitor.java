package org.cytoscape.ding.impl.work;

public class NoOutputProgressMonitor implements ProgressMonitor {

	private boolean canceled;
	
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
	}

	@Override
	public void done() {
	}

}
