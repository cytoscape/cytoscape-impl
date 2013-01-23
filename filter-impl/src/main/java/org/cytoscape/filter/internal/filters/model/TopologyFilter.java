package org.cytoscape.filter.internal.filters.model;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
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


import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.slf4j.LoggerFactory;


public class TopologyFilter extends CompositeFilter {
	
	private int minNeighbors = 1;
	private int withinDistance = 1;
	private CompositeFilter passFilter = null;
	
	public TopologyFilter(CyApplicationManager applicationManager) {
		super(applicationManager);
		super.advancedSetting.setNode(true);
	}

	public TopologyFilter(String pName, CyApplicationManager applicationManager) {
		super(applicationManager);
		name = pName;
		super.advancedSetting.setNode(true);
	}

	public void setPassFilter(CompositeFilter pFilter) {
		passFilter = pFilter;
		childChanged = true;
	}

	public CompositeFilter getPassFilter() {
		return passFilter;
	}

	public void setMinNeighbors(int pNeighbors) {
		minNeighbors = pNeighbors;
		childChanged = true;
	}

	public int getMinNeighbors() {
		return minNeighbors;
	}

	public void setDistance(int pDistance) {
		withinDistance = pDistance;
		childChanged = true;
	}

	public int getDistance() {
		return withinDistance;
	}

	@Override
	public BitSet getNodeBits() {
		apply();
		return nodeBits;
	}
	
	@Override
	public BitSet getEdgeBits(){
		apply();
		return edgeBits;		
	}
	
	@Override
	public void apply() {
		if ( !childChanged ) 
			return;

		if (network == null) {
			return;
		}
		
		//Make sure the pass filter is current
		if (passFilter == null) {
			passFilter = new TopologyFilter("None", applicationManager);
		}
		
		if (!passFilter.getName().equalsIgnoreCase("None")) {
			passFilter.setNetwork(network);
			passFilter.apply();			
		}	

		List<CyNode> nodes_list = null;

		int objectCount = -1;
		
		if (advancedSetting.isNodeChecked()) {
			nodes_list = network.getNodeList();
			objectCount = nodes_list.size();
			
			//Create an index mapping between RootGraphIndex and index in current network
			HashMap<CyNode, Integer> indexMap = new HashMap<CyNode, Integer>();
			for (int i = 0; i < objectCount; i++) {
				CyNode node = nodes_list.get(i);
				indexMap.put(node, i);
			}
			
			//
			nodeBits = new BitSet(objectCount); // all the bits are false at very beginning
			
			for (int i=0; i<objectCount; i++) {
				if (isHit(nodes_list.get(i), indexMap)) {
					nodeBits.set(i);
				}
			}			
		} else {
			LoggerFactory.getLogger(TopologyFilter.class).error("objectType is undefined.");
			return;
		}

		if (negation) {
			if (advancedSetting.isNodeChecked()) {
				nodeBits.flip(0, objectCount);
			}
		}

		childChanged = false;
	}

	public void setNodeBits(BitSet b) {
		nodeBits = b;
		//parent.childChanged();
	}

	public void setEdgeBits(BitSet b) {
		edgeBits = b;
		//parent.childChanged();
	}

	@Override
	public void setNetwork(CyNetwork pNetwork) {
		if (network != null && network == pNetwork) {
			return;
		}
		network = pNetwork;
		if (passFilter != null) {
			passFilter.setNetwork(network);			
		}

		childChanged();
	}
	
	@Override
	public String toSerializedForm() {
		String retStr = "<TopologyFilter>\n";
		
		retStr = retStr + "name=" + name + "\n";
		retStr = retStr + advancedSetting.toString() + "\n";
		retStr = retStr + "Negation=" + negation + "\n";
		retStr = retStr + "minNeighbors=" + minNeighbors + "\n";
		retStr = retStr + "withinDistance=" + withinDistance + "\n";

		if (passFilter == null) {
			retStr += "passFilter=null\n";			
		} else {
			retStr += "passFilter=" + passFilter.getName()+"\n";						
		}
		
		retStr += "</TopologyFiler>";

		return retStr;
	}
	
	private boolean isHit(CyNode pObj, HashMap<CyNode, Integer> pIndexMap) {
		// Get all the neighbors for pNode that pass the given filter
		Set<CyNode> neighborSet = new HashSet<CyNode>();
		getNeighbors(pObj, neighborSet, withinDistance);
		
		//Exclude self from the neighbor
		if (neighborSet.contains(pObj)) {
			neighborSet.remove(pObj);
		}

		// Obviously, this does not meet the criteria, don't do extra work 
		if (neighborSet.size() < minNeighbors) {
			return false;
		}
		
		// remove all the neighbors that do not pass the given filter
		if (!passFilter.getName().equalsIgnoreCase("None")) {
			Iterator<CyNode> iterator = neighborSet.iterator();
			
			CyNode node;
			while (iterator.hasNext()) {
				node = iterator.next();
				int nodeIndex = pIndexMap.get(node).intValue();
				
				if (!passFilter.getNodeBits().get(nodeIndex)) {
					iterator.remove();
				}
			}
		}
		
		if (neighborSet.size() < minNeighbors) {
			return false;
		}
		
		return true;
	}
	
	// Get all the neighbors for pNode within pDistance
	private void getNeighbors(CyNode pObj, Set<CyNode> pNeighborSet, int pDistance) {
		if (pDistance == 0) {
			if (!pNeighborSet.contains(pObj)) {
				pNeighborSet.add(pObj);
			}
			return;
		}
		
		List<CyNode> neighbors = network.getNeighborList(pObj, Type.ANY);

		for (CyNode node : neighbors) {
			if (!pNeighborSet.contains(node)) {
				pNeighborSet.add(node);
			}
			getNeighbors(node, pNeighborSet, pDistance-1);
		}
	}
	
}
