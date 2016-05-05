package org.cytoscape.group.internal.data;

/*
 * #%L
 * Cytoscape Group Data Impl (group-data-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

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

import org.cytoscape.group.internal.data.aggregators.BooleanAggregator;
import org.cytoscape.group.internal.data.aggregators.DoubleAggregator;
import org.cytoscape.group.internal.data.aggregators.IntegerAggregator;
import org.cytoscape.group.internal.data.aggregators.LongAggregator;
import org.cytoscape.group.internal.data.aggregators.NoneAggregator;
import org.cytoscape.group.internal.data.aggregators.StringAggregator;

import org.cytoscape.group.internal.data.aggregators.DoubleListAggregator;
import org.cytoscape.group.internal.data.aggregators.IntegerListAggregator;
import org.cytoscape.group.internal.data.aggregators.LongListAggregator;
import org.cytoscape.group.internal.data.aggregators.StringListAggregator;

public class CyGroupAggregationManagerImpl 
	implements CyGroupAggregationManager {

	CyGroupManager cyGroupManager;
	Map<Class<?>, List<Aggregator<?>>>aggMap = new HashMap<>();
	Map<Class<?>, List<Aggregator<?>>>aggListMap = new HashMap<>();

	private final Object lock = new Object();
	
	public CyGroupAggregationManagerImpl(CyGroupManager mgr) {
		this.cyGroupManager = mgr;

		IntegerAggregator.registerAggregators(this);
		LongAggregator.registerAggregators(this);
		DoubleAggregator.registerAggregators(this);
		StringAggregator.registerAggregators(this);
		BooleanAggregator.registerAggregators(this);
		NoneAggregator.registerAggregators(this);

		StringListAggregator.registerAggregators(this);
		IntegerListAggregator.registerAggregators(this);
		LongListAggregator.registerAggregators(this);
		DoubleListAggregator.registerAggregators(this);
	}

	@Override
	public void addAggregator(Aggregator<?> aggregator) {
		Class<?> type = aggregator.getSupportedType();
		Map<Class<?>, List<Aggregator<?>>> map = aggMap;
		if (type.isAssignableFrom(List.class)) {
			type = aggregator.getSupportedListType();
			map = aggListMap;
		}
		List<Aggregator<?>> aggList = null;
		synchronized (lock) {
			if (map.containsKey(type))
				aggList = map.get(type);
			else {
				aggList = new ArrayList<>();
				map.put(type, aggList);
			}
	
			aggList.add(aggregator);
		}
	}

	@Override
	public void removeAggregator(Aggregator<?> aggregator) {
		Class<?> type = aggregator.getSupportedType();
		Map<Class<?>, List<Aggregator<?>>> map = aggMap;
		if (type.isAssignableFrom(List.class)) {
			type = aggregator.getSupportedListType();
			map = aggListMap;
		}
		synchronized (lock) {
			if (map.containsKey(type)) {
				List<Aggregator<?>> aggList = map.get(type);
				aggList.remove(aggregator);
			}
		}
	}

	@Override
	public List<Aggregator<?>> getAggregators(Class<?> type) {
		synchronized (lock) {
			if (aggMap.containsKey(type))
				return aggMap.get(type);
			return new ArrayList<>();
		}
	}

	@Override
	public List<Aggregator<?>> getListAggregators(Class<?> type) {
		synchronized (lock) {
			if (aggListMap.containsKey(type))
				return aggListMap.get(type);
			return new ArrayList<>();
		}
	}

	public Aggregator<?> getAggregator(Class<?> type, String name) {
		for (Aggregator<?> agg: getAggregators(type)) {
			if (agg.toString().equals(name))
				return agg;
		}
		return null;
	}

	public Aggregator<?> getListAggregator(Class<?> type, String name) {
		for (Aggregator<?> agg: getListAggregators(type)) {
			if (agg.toString().equals(name))
				return agg;
		}
		return null;
	}

	@Override
	public List<Aggregator<?>> getAggregators() {
		synchronized (lock) {
			List<Aggregator<?>> allAggs = new ArrayList<>();
			for (Class<?> c: aggMap.keySet()) {
				allAggs.addAll(getAggregators(c));
			}
			return allAggs;
		}
	}

	@Override
	public List<Aggregator<?>> getListAggregators() {
		synchronized (lock) {
			List<Aggregator<?>> allAggs = new ArrayList<>();
			for (Class<?> c: aggListMap.keySet()) {
				allAggs.addAll(getAggregators(c));
			}
			return allAggs;
		}
	}

	@Override
	public List<Class<?>> getSupportedClasses() {
		synchronized (lock) {
			return new ArrayList<>(aggMap.keySet());
		}
	}

	@Override
	public List<Class<?>> getSupportedListClasses() {
		synchronized (lock) {
			return new ArrayList<>(aggListMap.keySet());
		}
	}
}
