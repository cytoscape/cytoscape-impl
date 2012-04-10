
package org.cytoscape.group.data.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.ContainsTunables;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.data.Aggregator;
import org.cytoscape.group.data.CyGroupAggregationManager;

public class CyGroupAggregationManagerImpl 
	implements CyGroupAggregationManager {

	CyGroupManager cyGroupManager;
	Map<Class, List<Aggregator>>aggMap = 
		new HashMap<Class, List<Aggregator>>();

	public CyGroupAggregationManagerImpl(CyGroupManager mgr) {
		this.cyGroupManager = mgr;
	}

	@Override
	public void addAggregator(Aggregator aggregator) {
		Class type = aggregator.getSupportedType();
		List<Aggregator> aggList = null;
		if (aggMap.containsKey(type))
			aggList = aggMap.get(type);
		else {
			aggList = new ArrayList<Aggregator>();
			aggMap.put(type, aggList);
		}

		aggList.add(aggregator);
	}

	@Override
	public void removeAggregator(Aggregator aggregator) {
		Class type = aggregator.getSupportedType();
		if (aggMap.containsKey(type)) {
			List<Aggregator> aggList = aggMap.get(type);
			aggList.remove(aggregator);
		}
	}

	@Override
	public List<Aggregator> getAggregators(Class type) {
		if (aggMap.containsKey(type))
			return aggMap.get(type);
		return new ArrayList<Aggregator>();
	}

	@Override
	public List<Aggregator> getAggregators() {
		List<Aggregator> allAggs = new ArrayList<Aggregator>();
		for (Class c: aggMap.keySet()) {
			allAggs.addAll(getAggregators(c));
		}
		return allAggs;
	}

	@Override
	public List<Class> getSupportedClasses() {
		return new ArrayList<Class>(aggMap.keySet());
	}
}
