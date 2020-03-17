package org.cytoscape.ding.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;

public class DebugRootProgressMonitor implements DebugProgressMonitor {

	private final ProgressMonitor delegate;
	private final DebugProgressMonitorCallback callback;
	private final DebugFrameType type;
	
	private List<DebugProgressMonitor> subMonitors = Collections.emptyList();
	
	private long start, end;
	private int  nodes, edges;
	
	public DebugRootProgressMonitor(DebugFrameType type, ProgressMonitor delegate, DebugProgressMonitorCallback callback) {
		this.type = type;
		this.delegate = ProgressMonitor.notNull(delegate);
		this.callback = callback;
	}

	@Override
	public void start(String taskName) {
		start = System.currentTimeMillis();
		delegate.start(taskName);
	}
	
	@Override
	public void done() {
		delegate.done();
	}
	
	public void done(RenderDetailFlags flags) {
		end = System.currentTimeMillis();
		nodes = flags.getVisibleNodeCount();
		edges = flags.getEstimatedEdgeCount();
		done();
		callback.addFrame(this);
	}
	
	@Override
	public <T> List<ProgressMonitor> split(double... parts) {
		subMonitors = new ArrayList<>(parts.length);
		return DebugProgressMonitor.super.split(parts);
	}
	
	@Override
	public ProgressMonitor createSubProgressMonitor(double percent) {
		var sub = new DebugSubProgressMonitor(this, percent);
		subMonitors.add(sub);
		return sub;
	}
	
	@Override
	public void cancel() {
		delegate.cancel();
	}

	@Override
	public boolean isCancelled() {
		return delegate.isCancelled();
	}

	@Override
	public void addProgress(double progress) {
		delegate.addProgress(progress);
	}

	@Override
	public List<DebugProgressMonitor> getSubMonitors() {
		return subMonitors;
	}
	
	public DebugFrameType getType() {
		return type;
	}
	
	public int getNodeCount() {
		return nodes;
	}

	public int getEdgeCountEstimate() {
		return edges;
	}
	
	@Override
	public long getStartTime() {
		return start;
	}
	
	@Override
	public long getEndTime() {
		return end;
	}
	
	public String getTimeMessage() {
		var time = getTime();
		var type = getType();
		var cancelled = isCancelled();
		
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
