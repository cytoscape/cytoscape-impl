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


import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;


public class EdgeInteractionFilter extends InteractionFilter {
	
	public EdgeInteractionFilter(CyApplicationManager applicationManager) {
		super(applicationManager);
		//Set selection for edge
		advancedSetting.setNode(false);
		advancedSetting.setEdge(true);
	}

	
	public EdgeInteractionFilter(CyApplicationManager applicationManager, String pName) {
		super(applicationManager);
		name = pName;
		//Set selection for edge
		advancedSetting.setNode(false);
		advancedSetting.setEdge(true);
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
		BitSet passFilter_nodeBits = passFilter.getNodeBits();

		List<CyEdge> edges_list = network.getEdgeList();
		List<CyNode> nodes = network.getNodeList();

		// EdgeInteractionFilter will select edge only
		int objectCount = edges_list.size();
		edgeBits = new BitSet(objectCount); // all the bits are false at very beginning

		if (nodeType != NODE_UNDEFINED) {
			for (int i=0; i<objectCount; i++) {
				if (isHit(edges_list.get(i), passFilter_nodeBits, nodes)) {
					edgeBits.set(i);
				}
			}			
		}
			
		if (negation) {
			edgeBits.flip(0, objectCount);
		}

		childChanged = false;
	}


	private boolean isHit(CyEdge pEdge, BitSet pPassFilter_nodeBits, List<CyNode> nodes) {
		
		// Get the list of relevant nodes for this edge
		List<CyNode> adjacentNodes = new ArrayList<>();
		
		if (nodeType == NODE_SOURCE) {
			adjacentNodes.add(pEdge.getSource());
		}
		else if (nodeType == NODE_TARGET) {
			adjacentNodes.add(pEdge.getTarget());
		}
		else if (nodeType == NODE_SOURCE_TARGET) {
			adjacentNodes.add(pEdge.getSource());
			adjacentNodes.add(pEdge.getTarget());		}
		else { //nodeType == NODE_UNDEFINED --Neither source or target is selected
			return false;
		}
						
		int nodeIndex = -1;
		for (int i=0; i < adjacentNodes.size(); i++) {
			nodeIndex = nodes.indexOf(adjacentNodes.get(i));
		
			if (pPassFilter_nodeBits.get(nodeIndex) == true) {
				return true;
			}
		}
		
		return false;
	}
}
