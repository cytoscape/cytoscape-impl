package org.cytoscape.filter.internal.work;

public class DiscreteProgressMonitor implements ProgressMonitor {

	private final ProgressMonitor wrapped;
	private double low;
	private double high;
	
	private int totalWork = 100;
	private int currentWork = 0;
	
	public DiscreteProgressMonitor(ProgressMonitor wrapped, double low, double high) {
		this.wrapped = wrapped;
		this.low = low;
		this.high = high;
	}
	
	public DiscreteProgressMonitor(ProgressMonitor wrapped) {
		this(wrapped, 0.0, 1.0);
	}
	
	private double map(double in, double inStart, double inEnd, double outStart, double outEnd) {
		double slope = (outEnd - outStart) / (inEnd - inStart);
		return outStart + slope * (in - inStart);
	}
	
	@Override
	public void setProgress(double progress) {
		double mappedProgress = map(progress, 0.0, 1.0, low, high);
		wrapped.setProgress(mappedProgress);
	}
	
	public void setTotalWork(int totalWork) {
		this.totalWork = totalWork;
	}
	
	public void setWork(int currentWork) {
		this.currentWork = currentWork;
		double mappedProgress = map(currentWork, 0, totalWork, 0.0, 1.0);
		setProgress(mappedProgress);
	}
	
	public void addWork(int workToAdd) {
		setWork(currentWork + workToAdd);
	}
	
	@Override
	public void cancel() {
		wrapped.cancel();
	}

	@Override
	public boolean isCancelled() {
		return wrapped.isCancelled();
	}

	@Override
	public void setStatusMessage(String message) {
		wrapped.setStatusMessage(message);
	}

	@Override
	public String toString() {
		return "DiscreteProgressMonitor: (" + currentWork + "/" + totalWork + ")";
	}
}
