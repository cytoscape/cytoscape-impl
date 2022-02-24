package org.cytoscape.search.internal.progress;


class SubProgressMonitor implements ProgressMonitor {

	private final ProgressMonitor parent;
	private final double percentage; // eg 0.2 = 20 percent
	
	protected SubProgressMonitor(ProgressMonitor parent, double percentage) {
		this.parent = parent;
		this.percentage = percentage;
	}

	@Override
	public void addProgress(double progress) {
		parent.addProgress(progress * percentage);
	}

	/**
	 * This just completes the sub monitor, not the parent monitor.
	 */
	@Override
	public void done() {
		addProgress(100.0 - percentage);
	}

}
