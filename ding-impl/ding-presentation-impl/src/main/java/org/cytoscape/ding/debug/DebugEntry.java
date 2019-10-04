package org.cytoscape.ding.debug;

public class DebugEntry {

	private final long time;
	private final boolean cancelled;
	private final DebugFrameType type;
	private final int nodeCount;
	private final int edgeCountEstimate;
	
	public DebugEntry(long frameTime, boolean cancelled, DebugFrameType type, int nodeCount, int edgeCountEstimate) {
		this.time = frameTime;
		this.cancelled = cancelled;
		this.type = type;
		this.nodeCount = nodeCount;
		this.edgeCountEstimate = edgeCountEstimate;
	}

	public long getTime() {
		return time;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public int getNodeCount() {
		return nodeCount;
	}

	public int getEdgeCountEstimate() {
		return edgeCountEstimate;
	}
	
	public String getTimeMessage() {
		if(type == DebugFrameType.MAIN_ANNOTAITONS)
			return "(annotations) " + time;
		else if(type == DebugFrameType.MAIN_EDGES)
			return "(edges) " + time;
		else if(cancelled)
			return "(cancelled) " + time;
		else
			return "" + time;
	}
	
}
