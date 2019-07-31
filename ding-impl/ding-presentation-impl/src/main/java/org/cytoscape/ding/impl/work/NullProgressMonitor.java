package org.cytoscape.ding.impl.work;

public class NullProgressMonitor implements ProgressMonitor {

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public void setStatusMessage(String message) {
	}

	@Override
	public void addProgress(double progress) {
	}

	@Override
	public void done() {
	}

}
