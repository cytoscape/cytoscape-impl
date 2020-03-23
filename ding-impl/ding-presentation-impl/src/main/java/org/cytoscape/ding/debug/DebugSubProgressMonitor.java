package org.cytoscape.ding.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.ding.impl.work.SubProgressMonitor;

public class DebugSubProgressMonitor extends SubProgressMonitor implements DebugProgressMonitor {

	private List<DebugSubProgressMonitor> subMonitors = Collections.emptyList();
	
	private String taskName;
	private long start;
	private long time = 0;
	
	protected DebugSubProgressMonitor(ProgressMonitor parent, double percent) {
		super(parent, percent);
	}

	@Override
	public ProgressMonitor[] split(double... parts) {
		subMonitors = new ArrayList<>(parts.length);
		return DebugProgressMonitor.super.split(parts);
	}
	
	@Override
	public void start(String taskName) {
		this.taskName = taskName;
		start = System.currentTimeMillis();
	}
	
	public String getTaskName() {
		return taskName;
	}
	
	@Override
	public void done() {
		super.done();
		long end = System.currentTimeMillis();
		time += end - start;
		start = 0;
	}
	
	@Override
	public void emptyTask(String taskName) {
		this.taskName = taskName;
	}
	
	@Override
	public ProgressMonitor createSubProgressMonitor(double percent) {
		var sub = new DebugSubProgressMonitor(this, percent);
		subMonitors.add(sub);
		return sub;
	}
	
	@Override
	public List<DebugSubProgressMonitor> getSubMonitors() {
		return subMonitors;
	}

	@Override
	public long getTime() {
		return time;
	}
}
