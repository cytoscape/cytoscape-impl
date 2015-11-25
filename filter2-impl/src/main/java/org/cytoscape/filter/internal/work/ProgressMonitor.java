package org.cytoscape.filter.internal.work;

public interface ProgressMonitor {
	
	void cancel();
	
	boolean isCancelled();
	
	void setProgress(double progress);
	
	void setStatusMessage(String message);
	
	default void start() {
		setProgress(0.0); // is this necessary?
	}
	default void done() {
		setProgress(1.0);
	}
	
	
	public static ProgressMonitor nullMonitor() {
		return new ProgressMonitor() {
			@Override
			public void setStatusMessage(String message) {
			}
			@Override
			public void setProgress(double progress) {
			}
			@Override
			public boolean isCancelled() {
				return false;
			}
			@Override
			public void cancel() {
			}
		};
	}
	
}
