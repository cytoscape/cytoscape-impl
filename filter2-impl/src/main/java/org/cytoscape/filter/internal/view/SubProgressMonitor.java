package org.cytoscape.filter.internal.view;

public class SubProgressMonitor implements ProgressMonitor {

	private final ProgressMonitor wrapped;
	private double low;
	private double high;
	
	public SubProgressMonitor(ProgressMonitor wrapped, double low, double high) {
		this.wrapped = wrapped;
		this.low = low;
		this.high = high;
	}
	
	@Override
	public void setProgress(double progress) {
		double mappedProgress = low + (high - low) * progress;
		wrapped.setProgress(mappedProgress);
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

}
