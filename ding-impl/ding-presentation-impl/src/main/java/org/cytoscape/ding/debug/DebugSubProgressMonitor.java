package org.cytoscape.ding.debug;

import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.ding.impl.work.SubProgressMonitor;

public class DebugSubProgressMonitor extends SubProgressMonitor {

	protected DebugSubProgressMonitor(ProgressMonitor parent, double percent) {
		super(parent, percent);
	}

	@Override
	public ProgressMonitor createSubProgressMonitor(double percent) {
		return new DebugSubProgressMonitor(this, percent);
	}
	
	@Override
	public void start(String taskName) {
		super.start(taskName);
	}
	
	
	@Override
	public void done() {
		super.done();
	}

}
