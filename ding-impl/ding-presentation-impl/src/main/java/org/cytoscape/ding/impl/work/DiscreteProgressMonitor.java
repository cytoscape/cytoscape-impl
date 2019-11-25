package org.cytoscape.ding.impl.work;

public class DiscreteProgressMonitor implements ProgressMonitor {

	private final ProgressMonitor wrapped;
	private final int totalWork;
	private int currentWork;
	
	DiscreteProgressMonitor(ProgressMonitor wrapped, int totalWork) {
		this.wrapped = wrapped;
		this.totalWork = totalWork;
	}
	
	@Override
	public void addProgress(double progress) {
		wrapped.addProgress(progress);
	}
	
	public void addWork(int workToAdd) {
		double progress = (double)workToAdd / (double)totalWork;
		addProgress(progress);
		currentWork += workToAdd;
	}
	
	public void increment() {
		addWork(1);
	}
	
	@Override
	public boolean isCancelled() {
		return wrapped.isCancelled();
	}
	
	@Override
	public void cancel() {
		// you have to cancel the root PM
	}

	@Override
	public void done() {
		if(totalWork > 0)
			addProgress((double)(totalWork - currentWork) / (double)totalWork);
	}
}
