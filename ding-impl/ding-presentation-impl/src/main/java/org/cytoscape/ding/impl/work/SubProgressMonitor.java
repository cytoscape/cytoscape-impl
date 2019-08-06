package org.cytoscape.ding.impl.work;

public class SubProgressMonitor implements ProgressMonitor {

	private final ProgressMonitor wrapped;
	private final double percentage; // eg 0.2 = 20 percent
	
	SubProgressMonitor(ProgressMonitor wrapped, double percentage) {
		this.wrapped = wrapped;
		this.percentage = percentage;
	}
	
	@Override
	public boolean isCancelled() {
		return wrapped.isCancelled();
	}
	
	@Override
	public void cancel() {
	}

	@Override
	public void setStatusMessage(String message) {
		wrapped.setStatusMessage(message);
	}

	@Override
	public void addProgress(double progress) {
		wrapped.addProgress(progress * percentage);
	}

	@Override
	public void done() {
		wrapped.done();
	}

}
