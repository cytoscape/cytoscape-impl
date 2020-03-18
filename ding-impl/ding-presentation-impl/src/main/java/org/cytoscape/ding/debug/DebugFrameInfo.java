package org.cytoscape.ding.debug;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DebugFrameInfo {
	
	private static AtomicInteger frameCounter = new AtomicInteger();
	
	private final List<DebugFrameInfo> subFrames;
	private final DebugFrameType type;
	private final boolean cancelled;
	private final long start;
	private final long end;
	private final int nodes;
	private final int edges;
	private final int frameNumber;
	
	
	private DebugFrameInfo(DebugFrameType type, boolean cancelled, long start, long end, int nodes, int edges, List<DebugFrameInfo> subFrames) {
		this.type = type;
		this.cancelled = cancelled;
		this.start = start;
		this.end = end;
		this.nodes = nodes;
		this.edges = edges;
		this.subFrames = subFrames;
		this.frameNumber = frameCounter.incrementAndGet();
	}

	
	public List<DebugFrameInfo> getSubFrameInfo() {
		return subFrames;
	}
	
	public DebugFrameType getType() {
		return type;
	}
	
	public long getStartTime() {
		return start;
	}
	
	public long getEndTime() {
		return end;
	}
	
	public long getTime() {
		return getEndTime() - getStartTime();
	}

	public int getNodeCount() {
		return nodes;
	}

	public int getEdgeCountEstimate() {
		return edges;
	}
	
	public boolean isCancelled() {
		return cancelled;
	}
	
	public int getFrameNumber() {
		return frameNumber;
	}
	
	public String getTimeMessage() {
		var time = getTime();
		var type = getType();
		var cancelled = isCancelled();
		
		if(type == DebugFrameType.MAIN_ANNOTAITONS)
			return time + " (annotations)";
		else if(type == DebugFrameType.MAIN_EDGES)
			return time + " (edges)";
		else if(cancelled)
			return time + " (cancelled)";
		else
			return "" + time;
	}
	
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DebugFrameInfo [type=");
		builder.append(type);
		builder.append(", cancelled=");
		builder.append(cancelled);
		builder.append(", start=");
		builder.append(start);
		builder.append(", end=");
		builder.append(end);
		builder.append(", nodes=");
		builder.append(nodes);
		builder.append(", edges=");
		builder.append(edges);
		builder.append(", frameNumber=");
		builder.append(frameNumber);
		builder.append("]");
		return builder.toString();
	}


	public static DebugFrameInfo fromProgressMonitor(DebugRootProgressMonitor pm) {
		DebugFrameType type = pm.getType();
		boolean cancelled = pm.isCancelled();
		long start = pm.getStartTime();
		long end = pm.getEndTime();
		int nodes = pm.getNodeCount();
		int edges = pm.getEdgeCountEstimate();
		List<DebugFrameInfo> subFrames = null;
		
		return new DebugFrameInfo(type, cancelled, start, end, nodes, edges, subFrames);
	}
	
	
}
