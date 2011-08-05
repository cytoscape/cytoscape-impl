package org.cytoscape.filter.internal.filters.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.filter.internal.filters.CompositeFilter;
import org.cytoscape.filter.internal.filters.FilterPlugin;
import org.cytoscape.filter.internal.quickfind.util.QuickFind;
import org.cytoscape.filter.internal.quickfind.util.QuickFindFactory;
import org.cytoscape.filter.internal.quickfind.util.TaskMonitorBase;
import org.cytoscape.filter.internal.widgets.autocomplete.index.GenericIndex;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.session.CyApplicationManager;


public class FilterUtil {
		
	// do selection on given network
	public static void doSelection(CompositeFilter pFilter, CyApplicationManager applicationManager) {
		//System.out.println("Entering FilterUtil.doSelection() ...");
		
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
				node.getCyRow().set(CyNetwork.SELECTED, true);
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
				edge.getCyRow().set(CyNetwork.SELECTED, true);
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
	
	
	public static boolean isFilterNameDuplicated(FilterPlugin filterPlugin, String pFilterName) {
		Vector<CompositeFilter> allFilterVect = filterPlugin.getAllFilterVect();
		if (allFilterVect == null || allFilterVect.size() == 0)
			return false;
		
		for (int i=0; i<allFilterVect.size(); i++) {
			CompositeFilter theFilter = allFilterVect.elementAt(i);
			if (pFilterName.equalsIgnoreCase(theFilter.getName().trim())) {
				return true;
			}
		}
		return false;
	}
	
	
	public static GenericIndex getQuickFindIndex(String pCtrlAttribute, CyNetwork pNetwork, int pIndexType) {
		final QuickFind quickFind = QuickFindFactory.getGlobalQuickFindInstance();
		quickFind.reindexNetwork(pNetwork, pIndexType, pCtrlAttribute, new TaskMonitorBase());
		
		return quickFind.getIndex(pNetwork);		
	}
	
	
	public static boolean hasSuchAttribute(CyNetwork network, String pAttribute, int pType) {
		if (pType == QuickFind.INDEX_NODES) {
			List<CyNode> nodes = network.getNodeList();
			if (nodes.size() == 0) {
				return false;
			}
			CyNode node = nodes.get(0);
			return node.getCyRow().getTable().getColumn(pAttribute) != null;
		}
		else if (pType == QuickFind.INDEX_EDGES) {
			List<CyEdge> edges = network.getEdgeList();
			if (edges.size() == 0) {
				return false;
			}
			CyEdge edge = edges.get(0);
			return edge.getCyRow().getTable().getColumn(pAttribute) != null;
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
//		String dynamicFilterThreshold = CytoscapeInit.getProperties().getProperty(FilterPlugin.DYNAMIC_FILTER_THRESHOLD);
//		if (dynamicFilterThreshold == null) { // threshold not defined, use the default value
			dynamicFilterThresholdValue = FilterPlugin.DEFAULT_DYNAMIC_FILTER_THRESHOLD;
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


