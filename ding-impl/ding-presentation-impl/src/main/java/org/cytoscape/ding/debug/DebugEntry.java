package org.cytoscape.ding.debug;

public class DebugEntry {

	private final long time;
	private final boolean cancelled;
	private final boolean annotations;
	private final int nodeCount;
	private final int edgeCountEstimate;
	
	public DebugEntry(long frameTime, boolean cancelled, boolean annotations, int nodeCount, int edgeCountEstimate) {
		this.time = frameTime;
		this.cancelled = cancelled;
		this.annotations = annotations;
		this.nodeCount = nodeCount;
		this.edgeCountEstimate = edgeCountEstimate;
	}

	public long getTime() {
		return time;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public boolean isAnnotations() {
		return annotations;
	}

	public int getNodeCount() {
		return nodeCount;
	}

	public int getEdgeCountEstimate() {
		return edgeCountEstimate;
	}
	
	public String getTimeMessage() {
		if(annotations)
			return "(annotations) " + time;
		else if(cancelled)
			return "(cancelled) " + time;
		else
			return "" + time;
	}
	
}
