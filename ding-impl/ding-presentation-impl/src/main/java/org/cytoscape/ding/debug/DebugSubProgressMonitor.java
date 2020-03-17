package org.cytoscape.ding.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.ding.impl.work.SubProgressMonitor;

public class DebugSubProgressMonitor extends SubProgressMonitor implements DebugProgressMonitor {

	private List<DebugProgressMonitor> subMonitors = Collections.emptyList();
	
	private long start, end;
	
	protected DebugSubProgressMonitor(ProgressMonitor parent, double percent) {
		super(parent, percent);
	}

	@Override
	public <T> List<ProgressMonitor> split(double... parts) {
		subMonitors = new ArrayList<>(parts.length);
		return DebugProgressMonitor.super.split(parts);
	}
	
	@Override
	public void start(String taskName) {
		start = System.currentTimeMillis();
		super.start(taskName);
	}
	
	@Override
	public void done() {
		super.done();
		end = System.currentTimeMillis();
	}

	@Override
	public ProgressMonitor createSubProgressMonitor(double percent) {
		var sub = new DebugSubProgressMonitor(this, percent);
		subMonitors.add(sub);
		return sub;
	}
	
	@Override
	public List<DebugProgressMonitor> getSubMonitors() {
		return subMonitors;
	}
	
	@Override
	public long getStartTime() {
		return start;
	}
	
	@Override
	public long getEndTime() {
		return end;
	}
}
