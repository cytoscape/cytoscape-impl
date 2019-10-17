package org.cytoscape.ding.debug;

public interface DebugCallback {
	
	void start(DebugFrameType type);
	
	void done(DebugFrameType type, boolean cancelled, int nodeCount, int edgeCountEstimate, long time);
	
}
