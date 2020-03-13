package org.cytoscape.ding.debug;

import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;

public class DebugProgressMonitor implements ProgressMonitor {

	private final ProgressMonitor delegate;
	private final DebugProgressMonitorCallback callback;
	private final DebugFrameType type;
	
	private long start;
	
	public DebugProgressMonitor(DebugFrameType type, ProgressMonitor delegate, DebugProgressMonitorCallback callback) {
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
		done();
		if(callback != null) {
			long end = System.currentTimeMillis();
			long time = end - start;
			boolean cancelled = isCancelled();
			int nodes = flags.getVisibleNodeCount();
			int edges = flags.getEstimatedEdgeCount();
//			DebugFrameInfo debugFrameInfo = new DebugFrameInfo(type, startTime, endTime, cancelled, nodeCount, edgeCountEstimate)
//			callback.addFrame(type, cancelled, nodes, edges, time);
		}
	}
	
	@Override
	public ProgressMonitor createSubProgressMonitor(double percent) {
		return new DebugSubProgressMonitor(this, percent);
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
	
}
