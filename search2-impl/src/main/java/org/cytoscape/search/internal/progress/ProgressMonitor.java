package org.cytoscape.search.internal.progress;

public interface ProgressMonitor {
	
	void addProgress(double progress);
	
	void done();
	
	
	
	public static ProgressMonitor nullMonitor() {
		return new ProgressMonitor() {
			@Override
			public void addProgress(double progress) {
			}
			@Override
			public void done() {
			}
		};
	}
	
	
	default DiscreteProgressMonitor toDiscrete(int totalWork) {
		return new DiscreteProgressMonitor(this, totalWork);
	}
	
	default ProgressMonitor subProgressMonitor(double percent) {
		return new SubProgressMonitor(this, percent);
	}
	
	default public ProgressMonitor[] split(double ... parts) {
		double sum = 0.0;
		for(double part : parts)
			sum += part;
		
		ProgressMonitor[] monitors = new ProgressMonitor[parts.length];
		for(int i = 0; i < parts.length; i++) {
			double percent = parts[i] / sum;
			monitors[i] = subProgressMonitor(percent);
		}
		return monitors;
	}
	
}
