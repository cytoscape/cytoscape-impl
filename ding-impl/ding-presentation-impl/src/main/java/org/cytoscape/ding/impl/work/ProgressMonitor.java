package org.cytoscape.ding.impl.work;

import java.util.ArrayList;
import java.util.List;

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
	
	default public List<ProgressMonitor> split(double ... parts) {
		double sum = 0.0;
		for(double part : parts)
			sum += part;
		
		List<ProgressMonitor> monitors = new ArrayList<>(parts.length);
		for(double part : parts) {
			double percent = part / sum;
			ProgressMonitor subMonitor = createSubProgressMonitor(percent);
			monitors.add(subMonitor);
		}
		return monitors;
	}
	
	default ProgressMonitor createSubProgressMonitor(double percent) {
		return new SubProgressMonitor(this, percent);
	}
	
	
}
