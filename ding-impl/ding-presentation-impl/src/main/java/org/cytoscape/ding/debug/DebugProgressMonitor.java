package org.cytoscape.ding.debug;

import java.util.List;

import org.cytoscape.ding.impl.work.ProgressMonitor;

public interface DebugProgressMonitor extends ProgressMonitor {

	List<DebugSubProgressMonitor> getSubMonitors();
	
	long getTime();
	
}
