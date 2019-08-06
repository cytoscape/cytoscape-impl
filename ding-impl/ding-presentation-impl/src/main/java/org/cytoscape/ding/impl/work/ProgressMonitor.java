package org.cytoscape.ding.impl.work;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface ProgressMonitor {
	
	void cancel();
	
	boolean isCancelled();
	
	default void setStatusMessage(String message) {};
	
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
	
	
	public static final ProgressMonitor NULL = new ProgressMonitor() {
		
		@Override
		public boolean isCancelled() { return false; }
		
		@Override
		public void cancel() { }

		@Override
		public void setStatusMessage(String message) { }

		@Override
		public void addProgress(double progress) { }

		@Override
		public void done() { }

		@Override
		public <T> List<ProgressMonitor> split(double... parts) {
			return Collections.nCopies(parts.length, NULL);
		}
	};
	
}
