package org.cytoscape.ding.debug;

import static org.cytoscape.ding.debug.DebugUtil.map;
import static org.cytoscape.ding.debug.DebugUtil.map2;
import static org.cytoscape.ding.debug.DebugUtil.reduce;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DebugFrameInfo {

	private final String task;
	private final long time;
	
	private final Map<String,DebugFrameInfo> subFrames;
	
	public DebugFrameInfo(String task, long time, List<DebugFrameInfo> subFrames) {
		this.task = task == null ? "null" : task;
		this.time = time;
		this.subFrames = new LinkedHashMap<>();
		for(DebugFrameInfo sub : subFrames) {
			this.subFrames.put(sub.task, sub);
		}
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
	
	public Collection<DebugFrameInfo> getSubFrames() {
		return subFrames.values();
	}
	
	
	public static DebugFrameInfo fromSubPM(DebugSubProgressMonitor pm) {
		String task = pm.getTaskName();
		long time = pm.getTime();
		var subInfos = map(pm.getSubMonitors(), x -> fromSubPM(x));
		return new DebugFrameInfo(task, time, subInfos);
	}
	
	
	
	public static DebugFrameInfo merge(DebugFrameInfo info1, DebugFrameInfo info2) {
		try {
			return mergeFast(info1, info2);
		} catch(IllegalArgumentException e) {
			return mergeSlow(info1, info2);
		}
	}

	private static DebugFrameInfo mergeFast(DebugFrameInfo info1, DebugFrameInfo info2) {
		// For now, assume both sub frame lists have the exact same tasks
		if(!Objects.equals(info1.task, info2.task))
			throw new IllegalArgumentException("Cannot merge DebugFrameInfo, wrong tasks " + info1.task + ", " + info2.task);
		if(info1.subFrames.size() != info2.subFrames.size())
			throw new IllegalArgumentException("Cannot merge DebugFrameInfo, not same number of tasks " + info1.subFrames.keySet() + "..." + info2.subFrames.keySet());
		
		List<DebugFrameInfo> mergedSubFrames = map2(info1.subFrames.values(), info2.subFrames.values(), DebugFrameInfo::mergeFast);
		return new DebugFrameInfo(info1.task, info1.time + info2.time, mergedSubFrames);
	}
	
	
	public static DebugFrameInfo mergeSlow(DebugFrameInfo info1, DebugFrameInfo info2) {
		if(!Objects.equals(info1.task, info2.task))
			throw new IllegalArgumentException("Cannot merge DebugFrameInfo, wrong tasks " + info1.task + ", " + info2.task);
		
		Set<String> tasks = new LinkedHashSet<>();
		tasks.addAll(info1.subFrames.keySet());
		tasks.addAll(info2.subFrames.keySet());
		
		List<DebugFrameInfo> mergedSubFrames = new ArrayList<>(tasks.size());
		for(String task : tasks) {
			DebugFrameInfo sub1 = info1.subFrames.get(task);
			DebugFrameInfo sub2 = info2.subFrames.get(task);
			if(sub1 != null && sub2 != null) {
				mergedSubFrames.add(mergeSlow(sub1,sub2));
			} else if(sub2 == null) {
				mergedSubFrames.add(sub1);
			} else if(sub1 == null) {
				mergedSubFrames.add(sub2);
			}
			
		}
		return new DebugFrameInfo(info1.task, info1.time + info2.time, mergedSubFrames);
	}
	
	
	public static DebugFrameInfo merge(List<DebugFrameInfo> list) {
		return reduce(list, DebugFrameInfo::merge);
	}
	
}