package org.cytoscape.filter.internal.filters.util;

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


import java.util.Collection;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.filter.internal.filters.model.CompositeFilter;
import org.cytoscape.filter.internal.quickfind.util.QuickFind;
import org.cytoscape.filter.internal.quickfind.util.TaskMonitorBase;
import org.cytoscape.filter.internal.widgets.autocomplete.index.GenericIndex;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;


public class FilterUtil {
	
	public static final String FILTER_APP_NAME = "org.cytoscape.filter";
	public static final String DEFAULT_FILE_NAME = "default_filters.props";
	public static final String SESSION_FILE_NAME = "session_filters.props";
	public static final String DYNAMIC_FILTER_THRESHOLD = "dynamicFilterThreshold";
	public static final int DEFAULT_DYNAMIC_FILTER_THRESHOLD = 1000;
	
	// do selection on given network
	public static void doSelection(CompositeFilter pFilter, CyApplicationManager applicationManager) {
//		// TODO: What do we do about CyInit*?
//		CyInitParams init = CytoscapeInit.getCyInitParams();
//
//		if (init == null)
//			return;
//
//		if (pFilter.getNetwork() == null)
//			return;
//
//		// Set wait cursor
//		if ((init.getMode() == CyInitParams.GUI)
//			    || (init.getMode() == CyInitParams.EMBEDDED_WINDOW)) {
//			// set the wait cursor
//			Cytoscape.getDesktop().setCursor(
//					Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));		
//		}
				
		pFilter.apply();

		CyNetwork network = pFilter.getNetwork();

		SelectUtil.unselectAllNodes(network);
		SelectUtil.unselectAllEdges(network);
		
		final List<CyNode> nodes_list = network.getNodeList();
		final List<CyEdge> edges_list = network.getEdgeList();

		if (pFilter.getAdvancedSetting().isNodeChecked()&& (pFilter.getNodeBits() != null)) {
			// Select nodes
			CyNode node = null;

			for (int i=0; i< pFilter.getNodeBits().length(); i++) {
				int next_set_bit = pFilter.getNodeBits().nextSetBit(i);
				
				node = nodes_list.get(next_set_bit);
				network.getRow(node).set(CyNetwork.SELECTED, true);
				i = next_set_bit;
			}
		}
		if (pFilter.getAdvancedSetting().isEdgeChecked()&& (pFilter.getEdgeBits() != null)) {
			// Select edges
			CyEdge edge = null;
			for (int i=0; i< edges_list.size(); i++) {
				int next_set_bit = pFilter.getEdgeBits().nextSetBit(i);
				if (next_set_bit == -1) {
					break;
				}
				edge = edges_list.get(next_set_bit);
				network.getRow(edge).set(CyNetwork.SELECTED, true);
				i = next_set_bit;
			}
		}
		
//		// TODO: What do we do about CyInit*?
//		//Restore cursor
//		if ((init.getMode() == CyInitParams.GUI)
//			    || (init.getMode() == CyInitParams.EMBEDDED_WINDOW)) {
//			// set the wait cursor
//			Cytoscape.getDesktop().setCursor(
//					Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));		
//		}

	}
	
	
	public static boolean isFilterNameDuplicated(Collection<CompositeFilter> allFilterVect, String pFilterName) {
		// TODO
		//Vector<CompositeFilter> allFilterVect = ServicesUtil.filterReader.getProperties();
		if (allFilterVect == null || allFilterVect.isEmpty())
			return false;
		
		for (CompositeFilter theFilter : allFilterVect) {
			if (pFilterName.equalsIgnoreCase(theFilter.getName().trim())) {
				return true;
			}
		}
		
		return false;
	}
	
	
	public static GenericIndex getQuickFindIndex(final QuickFind quickFind, String pCtrlAttribute, CyNetwork pNetwork, int pIndexType) {
		quickFind.reindexNetwork(pNetwork, pIndexType, pCtrlAttribute, new TaskMonitorBase());
		
		return quickFind.getIndex(pNetwork);		
	}
	
	
	public static boolean hasSuchAttribute(CyNetwork network, String pAttribute, int pType) {
		if (network == null)
			return false;
		if (pType == QuickFind.INDEX_NODES) {
			if (network.getNodeCount() == 0) {
				return false;
			}
			return network.getDefaultNodeTable().getColumn(pAttribute) != null;
		}
		else if (pType == QuickFind.INDEX_EDGES) {
			if (network.getEdgeCount() == 0) {
				return false;
			}
			return network.getDefaultEdgeTable().getColumn(pAttribute) != null;
		}
		return false;
	}

	// If a network size (node count and edge count) is less than DYNAMIC_FILTER_THRESHOLD, return true
	// Otherwise, return false
	public static boolean isDynamicFilter(CompositeFilter pFilter) {
		CyNetwork theNetwork = pFilter.getNetwork();

		if (theNetwork == null) {
			return false;
		}
		
		int nodeCount = theNetwork.getNodeCount();
		int edgeCount = theNetwork.getEdgeCount();

		int dynamicFilterThresholdValue = -1; 
		
//		// TODO: What do we do about CyInit*?
//		String dynamicFilterThreshold = CytoscapeInit.getProperties().getProperty(DYNAMIC_FILTER_THRESHOLD);
//		if (dynamicFilterThreshold == null) { // threshold not defined, use the default value
			dynamicFilterThresholdValue = DEFAULT_DYNAMIC_FILTER_THRESHOLD;
//		}
//		else {
//			dynamicFilterThresholdValue = (new Integer(dynamicFilterThreshold)).intValue();
//		}
		
		if (pFilter.getAdvancedSetting().isNodeChecked() && pFilter.getAdvancedSetting().isEdgeChecked()) {
			// Select both nodes and edges
			if (nodeCount > dynamicFilterThresholdValue || edgeCount > dynamicFilterThresholdValue) {
				return false;
			}
			return true;
		}
		else if (pFilter.getAdvancedSetting().isNodeChecked()) {
			//Select node only
			if (nodeCount < dynamicFilterThresholdValue) {
				return true;
			}
		}
		else if (pFilter.getAdvancedSetting().isEdgeChecked()){
			// select edge only
			if (edgeCount < dynamicFilterThresholdValue) {
				return true;
			}
		}
		
		return false;
	}
}


