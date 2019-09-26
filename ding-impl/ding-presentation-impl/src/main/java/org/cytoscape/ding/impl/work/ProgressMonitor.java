package org.cytoscape.ding.impl.work;

import java.util.ArrayList;
import java.util.List;

public interface ProgressMonitor {
	
	void cancel();
	
	boolean isCancelled();
	
	void addProgress(double progress);
	
	default void start() {};
	
	void done();
	
	
	default DiscreteProgressMonitor toDiscrete(int totalWork) {
		return new DiscreteProgressMonitor(this, totalWork);
	}
	
	default <T> List<ProgressMonitor> split(double ... parts) {
		double sum = 0.0;
		for(double part : parts)
			sum += part;
		
		List<ProgressMonitor> monitors = new ArrayList<>(parts.length);
		for(double part : parts) {
			double percent = part / sum;
			monitors.add(new SubProgressMonitor(this, percent));
		}
		return monitors;
	}
	
	static ProgressMonitor notNull(ProgressMonitor pm) {
		return pm == null ? new NoOutputProgressMonitor() : pm;
	}
}
