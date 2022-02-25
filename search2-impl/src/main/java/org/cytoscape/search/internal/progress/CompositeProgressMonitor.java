package org.cytoscape.search.internal.progress;

import java.util.List;

public class CompositeProgressMonitor implements ProgressMonitor {

	private final List<ProgressMonitor> monitors;
	
	
	public CompositeProgressMonitor(List<ProgressMonitor> monitors) {
		this.monitors = monitors;
	}

	@Override
	public void addProgress(double progress) {
		for(var pm : monitors) {
			pm.addProgress(progress);
		}
	}

	@Override
	public void done() {
		for(var pm : monitors) {
			pm.done();
		}
	}

}
