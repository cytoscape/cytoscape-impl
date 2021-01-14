package org.cytoscape.ding.debug;

import static org.cytoscape.ding.debug.DebugUtil.map;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.cytoscape.ding.impl.canvas.CompositeImageCanvas.PaintParameters;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;

public class DebugRootFrameInfo extends DebugFrameInfo {
	
	private static AtomicInteger frameCounter = new AtomicInteger();
	
	private final DebugFrameType type;
	private final boolean cancelled;
	private final int frameNumber;
	private final long start;
	private final long end;
	
	private final RenderDetailFlags flags;
	private final PaintParameters paintParams;
	
	
	private DebugRootFrameInfo(
			String task, 
			long start, 
			long end, 
			DebugFrameType type, 
			boolean cancelled, 
			RenderDetailFlags flags,
			PaintParameters paintParams,
			List<DebugFrameInfo> subFrames
	) {
		super(task, end - start, subFrames);
		this.type = type;
		this.cancelled = cancelled;
		this.flags = flags;
		this.paintParams = paintParams;
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
	
	public RenderDetailFlags getRenderDetailFlags() {
		return flags;
	}

	public PaintParameters getPaintParameters() {
		return paintParams;
	}
	
	public boolean isCancelled() {
		return cancelled;
	}
	
	public int getFrameNumber() {
		return frameNumber;
	}
	
	public static DebugRootFrameInfo fromProgressMonitor(DebugRootProgressMonitor pm) {
		DebugFrameType type = pm.getType();
		boolean cancelled = pm.isCancelled();
		long start = pm.getStartTime();
		long end = pm.getEndTime();
		RenderDetailFlags flags = pm.getRenderDetailFlags();
		PaintParameters paintParams = pm.getPaintParametsr();
		String task = pm.getTaskName();
		var subInfos = map(pm.getSubMonitors(), x -> fromSubPM(x));
		return new DebugRootFrameInfo(task, start, end, type, cancelled, flags, paintParams, subInfos);
	}
	
	
}
