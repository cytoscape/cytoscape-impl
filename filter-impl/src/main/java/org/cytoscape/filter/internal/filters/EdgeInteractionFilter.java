
/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/

package org.cytoscape.filter.internal.filters;

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

		List<CyEdge> edges_list = null;

		// EdgeInteractionFilter will select edge only
		edges_list = network.getEdgeList();
		int objectCount = edges_list.size();
		edge_bits = new BitSet(objectCount); // all the bits are false at very beginning

		if (nodeType != NODE_UNDEFINED) {
			for (int i=0; i<objectCount; i++) {
				if (isHit(edges_list.get(i), passFilter_nodeBits)) {
					edge_bits.set(i);
				}
			}			
		}
			
		if (negation) {
			edge_bits.flip(0, objectCount);
		}

		childChanged = false;
	}


	private boolean isHit(CyEdge pEdge, BitSet pPassFilter_nodeBits) {
		
		// Get the list of relevant nodes for this edge
		List<CyNode> adjacentNodes = new ArrayList<CyNode>();
		
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
			nodeIndex = network.getNodeList().indexOf(adjacentNodes.get(i));
		
			if (pPassFilter_nodeBits.get(nodeIndex) == true) {
				return true;
			}
		}
		
		return false;
	}
}
