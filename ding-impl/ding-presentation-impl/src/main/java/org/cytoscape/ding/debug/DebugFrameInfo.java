package org.cytoscape.ding.debug;

public class DebugFrameInfo {

	private final DebugFrameType type;
	private final long startTime;
	private final long endTime;
	private final boolean cancelled;
	private final int nodeCount;
	private final int edgeCountEstimate;
	
	public DebugFrameInfo(DebugFrameType type, long startTime, long endTime, boolean cancelled, int nodeCount, int edgeCountEstimate) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.cancelled = cancelled;
		this.type = type;
		this.nodeCount = nodeCount;
		this.edgeCountEstimate = edgeCountEstimate;
	}

	public DebugFrameType getType() {
		return type;
	}
	
	public int getNodeCount() {
		return nodeCount;
	}

	public int getEdgeCountEstimate() {
		return edgeCountEstimate;
	}
	
	public boolean isCancelled() {
		return cancelled;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public long getEndTime() {
		return endTime;
	}
	
	public long getTime() {
		return endTime - startTime;
	}
	
	public String getTimeMessage() {
		long time = getTime();
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
