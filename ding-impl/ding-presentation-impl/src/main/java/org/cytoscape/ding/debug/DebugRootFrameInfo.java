package org.cytoscape.ding.debug;

import static org.cytoscape.ding.debug.DebugUtil.map;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DebugRootFrameInfo extends DebugFrameInfo {
	
	private static AtomicInteger frameCounter = new AtomicInteger();
	
	private final DebugFrameType type;
	private final boolean cancelled;
	private final int nodes;
	private final int edges;
	private final int frameNumber;
	private final long start;
	private final long end;
	
	
	private DebugRootFrameInfo(
			String task, 
			long start, 
			long end, 
			DebugFrameType type, 
			boolean cancelled, 
			int nodes, 
			int edges, 
			List<DebugFrameInfo> subFrames
	) {
		super(task, end - start, subFrames);
		this.type = type;
		this.cancelled = cancelled;
		this.nodes = nodes;
		this.edges = edges;
		this.start = start;
		this.end = end;
		this.frameNumber = frameCounter.incrementAndGet();
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
		builder.append(getStartTime());
		builder.append(", end=");
		builder.append(getEndTime());
		builder.append(", nodes=");
		builder.append(nodes);
		builder.append(", edges=");
		builder.append(edges);
		builder.append(", frameNumber=");
		builder.append(frameNumber);
		builder.append("]");
		return builder.toString();
	}


	public static DebugRootFrameInfo fromProgressMonitor(DebugRootProgressMonitor pm) {
		DebugFrameType type = pm.getType();
		boolean cancelled = pm.isCancelled();
		long start = pm.getStartTime();
		long end = pm.getEndTime();
		int nodes = pm.getNodeCount();
		int edges = pm.getEdgeCountEstimate();
		String task = pm.getTaskName();
		var subInfos = map(pm.getSubMonitors(), x -> fromSubPM(x));
		return new DebugRootFrameInfo(task, start, end, type, cancelled, nodes, edges, subInfos);
	}
	
	
}
