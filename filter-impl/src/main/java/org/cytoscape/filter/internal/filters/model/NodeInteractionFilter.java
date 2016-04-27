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
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNode;


public class NodeInteractionFilter extends InteractionFilter {
	
	public NodeInteractionFilter(CyApplicationManager applicationManager) {
		super(applicationManager);
		//Set selection for node
		advancedSetting.setNode(true);
		advancedSetting.setEdge(false);
	}

	public NodeInteractionFilter(String pName, CyApplicationManager applicationManager) {
		super(applicationManager);
		name = pName;
		//	Set selection for node
		advancedSetting.setNode(true);
		advancedSetting.setEdge(false);
	}
		
	
	public void apply() {
		if ( !childChanged ) 
			return;

		if (network == null) {
			setNetwork(applicationManager.getCurrentNetwork());
		}
		
		if (network == null) {
			return;
		}
		
		//Make sure the pass filter is current
		if (passFilter == null) {
			passFilter = new CompositeFilter("None");
		}
		
		if (!passFilter.getName().equalsIgnoreCase("None")) {
			passFilter.setNetwork(network);
			passFilter.apply();		
		}	

		List<CyNode> nodes_list = network.getNodeList();
		List<CyEdge> edges = network.getEdgeList();

		// NodeInteractionFilter will select node only
		int objectCount = nodes_list.size();
		nodeBits = new BitSet(objectCount); // all the bits are false at very beginning
			
		if ((nodeType != NODE_UNDEFINED)&&(!passFilter.getName().equalsIgnoreCase("None"))) {
			for (int i=0; i<objectCount; i++) {
				if (isHit(nodes_list.get(i), edges)) {
					nodeBits.set(i);
				}
			}			
		}
			
		if (negation) {
			nodeBits.flip(0, objectCount);
		}

		childChanged = false;
	}


	private boolean isHit(CyNode pNode, List<CyEdge> edges) {
		
		// Get the list of relevant edges for this node
		List<CyEdge> adjacentEdges;
		
		if (nodeType == NODE_SOURCE) {
			adjacentEdges = network.getAdjacentEdgeList(pNode, Type.OUTGOING);
		}
		else if (nodeType == NODE_TARGET) {
			adjacentEdges = network.getAdjacentEdgeList(pNode, Type.INCOMING);
		}
		else if (nodeType == NODE_SOURCE_TARGET) {
			adjacentEdges = network.getAdjacentEdgeList(pNode, Type.ANY);
		}
		else { //nodeType == NODE_UNDEFINED --Neither source or target is selected
			return false;
		}
		
		if (adjacentEdges == null || adjacentEdges.size() == 0) {
			return false;
		}
				
		BitSet passFilter_edgeBits = passFilter.getEdgeBits();
		if (passFilter_edgeBits == null) return false;

		int edgeIndex;
		for (int i=0; i < adjacentEdges.size(); i++) {
			edgeIndex = edges.indexOf(adjacentEdges.get(i));
		
			if (passFilter_edgeBits.get(edgeIndex) == true) {
				return true;
			}
		}
		
		return false;
	}
}
