package org.cytoscape.ding.impl.work;

public interface ProgressMonitor {
	
	
	void cancel();
	
	boolean isCancelled();
	
	void addProgress(double progress);
	
	
	void start(String taskName);
	
	void done();
	
	
	
	default void emptyTask(String taskName) {
		start(taskName);
		done();
	}
	
	default ProgressMonitorCloseable task(String taskName) {
		return new ProgressMonitorCloseable() {
			{ start(taskName); }
			@Override
			public void close() {
				done();
			}
		};
	}
	
	default DiscreteProgressMonitor toDiscrete(int totalWork) {
		return new DiscreteProgressMonitor(this, totalWork);
	}
	
	static ProgressMonitor notNull(ProgressMonitor pm) {
		return pm == null ? new NoOutputProgressMonitor() : pm;
	}
	
	default public ProgressMonitor[] split(double ... parts) {
		double sum = 0.0;
		for(double part : parts)
			sum += part;
		
		ProgressMonitor[] monitors = new ProgressMonitor[parts.length];
		for(int i = 0; i < parts.length; i++) {
			double percent = parts[i] / sum;
			monitors[i] = createSubProgressMonitor(percent);
		}
		return monitors;
	}
	
	default ProgressMonitor createSubProgressMonitor(double percent) {
		return new SubProgressMonitor(this, percent);
	}
	
	
}
