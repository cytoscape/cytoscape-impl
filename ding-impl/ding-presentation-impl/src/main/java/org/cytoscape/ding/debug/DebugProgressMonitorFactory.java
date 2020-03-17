package org.cytoscape.ding.debug;

import org.cytoscape.ding.impl.work.ProgressMonitor;

public class DebugProgressMonitorFactory {

	private final DingDebugMediator mediator;
	
	public DebugProgressMonitorFactory(DingDebugMediator mediator) {
		this.mediator = mediator;
	}
	
	public DebugRootProgressMonitor create(DebugFrameType type, ProgressMonitor delegate) {
		return new DebugRootProgressMonitor(type, delegate, mediator);
	}
}
