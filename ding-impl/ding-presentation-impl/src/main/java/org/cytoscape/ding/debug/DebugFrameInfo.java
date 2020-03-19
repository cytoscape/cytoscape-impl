package org.cytoscape.ding.debug;

import static org.cytoscape.ding.debug.DebugUtil.map;
import static org.cytoscape.ding.debug.DebugUtil.map2;
import static org.cytoscape.ding.debug.DebugUtil.reduce;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DebugFrameInfo {

	private final String task;
	private final long time;
	
	private final List<DebugFrameInfo> subFrames;
	
	public DebugFrameInfo(String task, long time, List<DebugFrameInfo> subFrames) {
		this.task = task == null ? "null" : task;
		this.time = time;
		this.subFrames = new ArrayList<>(subFrames);
	}

	public String getTask() {
		return task;
	}

	@Override
	public String toString() {
		return task;
	}
	
	public long getTime() {
		return time;
	}
	
	public List<DebugFrameInfo> getSubFrames() {
		return subFrames;
	}
	
	
	public static DebugFrameInfo fromSubPM(DebugSubProgressMonitor pm) {
		long start = pm.getStartTime();
		long end = pm.getEndTime();
		String task = pm.getTaskName();
		var subInfos = map(pm.getSubMonitors(), x -> fromSubPM(x));
		return new DebugFrameInfo(task, end - start, subInfos);
	}

	
	public DebugFrameInfo merge(DebugFrameInfo other) {
		// For now, assume both sub frame lists have the exact same tasks
		if(!Objects.equals(task, other.task))
			throw new IllegalArgumentException("Cannot merge DebugFrameInfo, wrong tasks " + task + ", " + other.task);
		if(subFrames.size() != other.subFrames.size())
			throw new IllegalArgumentException("Cannot merge DebugFrameInfo, not same number of tasks");
		
		List<DebugFrameInfo> mergedSubFrames;
		try {
			mergedSubFrames = map2(subFrames, other.subFrames, DebugFrameInfo::merge);
		} catch(IllegalArgumentException e) {
			System.out.println("sub frames this :" + subFrames);
			System.out.println("sub frames other:" + other.subFrames);
			throw e;
		}
		return new DebugFrameInfo(task, time + other.time, mergedSubFrames);
	}
	
	
	public static DebugFrameInfo merge(List<DebugFrameInfo> list) {
		return reduce(list, DebugFrameInfo::merge);
	}
	
}