package org.cytoscape.ding.debug;

public interface DebugCallback {
	
	void addFrameTime(DebugFrameType type, boolean cancelled, long time);

}
