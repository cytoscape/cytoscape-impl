package org.cytoscape.ding.impl.work;

public class SubProgressMonitor implements ProgressMonitor {

	private final ProgressMonitor parent;
	private final double percentage; // eg 0.2 = 20 percent
	private double currentProgress;
	
	protected SubProgressMonitor(ProgressMonitor parent, double percentage) {
		this.parent = parent;
		this.percentage = percentage;
	}
	
	@Override
	public boolean isCancelled() {
		return parent.isCancelled();
	}
	
	@Override
	public void cancel() {
	}

	@Override
	public void addProgress(double progress) {
		currentProgress += progress;
		parent.addProgress(progress * percentage);
	}

	@Override
	public void done() {
		addProgress(1.0 - currentProgress);
	}

}
