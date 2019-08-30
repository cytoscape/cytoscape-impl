package org.cytoscape.ding.debug;

import org.cytoscape.ding.impl.work.ProgressMonitor;

public class DebugProgressMonitor implements ProgressMonitor {

	private final ProgressMonitor delegate;
	private final DebugCallback callback;
	private final DebugFrameType type;
	
	private long start;
	
	public DebugProgressMonitor(DebugFrameType type, ProgressMonitor delegate, DebugCallback callback) {
		this.type = type;
		this.delegate = ProgressMonitor.notNull(delegate);
		this.callback = callback;
	}

	@Override
	public void start() {
		start = System.currentTimeMillis();
		delegate.start();
	}
	
	@Override
	public void done() {
		delegate.done();
		long end = System.currentTimeMillis();
		long time = end - start;
		if(callback != null)
			callback.addFrameTime(type, time);
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
