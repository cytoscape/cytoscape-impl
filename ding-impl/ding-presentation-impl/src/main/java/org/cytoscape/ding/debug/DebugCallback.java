package org.cytoscape.ding.debug;

public interface DebugCallback {
	
	void addFrame(DebugFrameType type, boolean cancelled, int nodeCount, int edgeCountEstimate, long time);
	
}
