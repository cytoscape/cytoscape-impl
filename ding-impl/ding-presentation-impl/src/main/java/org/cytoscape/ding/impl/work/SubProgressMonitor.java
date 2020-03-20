package org.cytoscape.ding.impl.work;

public class SubProgressMonitor implements ProgressMonitor {

	private final ProgressMonitor parent;
	private final double percentage; // eg 0.2 = 20 percent
	
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
		parent.addProgress(progress * percentage);
	}

	@Override
	public void start(String taskName) {
	}
	
	/**
	 * This just completes the sub monitor, not the parent monitor.
	 */
	@Override
	public void done() {
	}
	
}
