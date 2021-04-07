package org.cytoscape.ding.impl.work;

public class DiscreteProgressMonitor {

	private final ProgressMonitor wrapped;
	private final int totalWork;
	private int currentWork;
	
	DiscreteProgressMonitor(ProgressMonitor wrapped, int totalWork) {
		this.wrapped = wrapped;
		this.totalWork = totalWork;
	}
	
	
	public void addWork(int workToAdd) {
		double progress = (double)workToAdd / (double)totalWork;
		wrapped.addProgress(progress);
		currentWork += workToAdd;
	}
	
	public void increment() {
		addWork(1);
	}
	
	public int getCurrentWork() {
		return currentWork;
	}
	
	public void workFinished() {
		if(totalWork > 0)
			wrapped.addProgress((double)(totalWork - currentWork) / (double)totalWork);
	}
}
