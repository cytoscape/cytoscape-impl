package org.cytoscape.filter.internal.filters.util;

import java.util.Collection;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableUtil;

public class SelectUtil {
	public static void unselectAllNodes(CyNetwork network) {
		setSelectedState(network, CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true), false);
	}
	
	public static void unselectAllEdges(CyNetwork network) {
		setSelectedState(network, CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, true), false);
	}

	public static void setSelectedNodeState(CyNetwork network, Collection<CyNode> list, boolean selected) {
		setSelectedState(network, list, selected);
	}

	public static void setSelectedEdgeState(CyNetwork network, Collection<CyEdge> list, boolean selected) {
		setSelectedState(network, list, selected);
	}

	static void setSelectedState(CyNetwork network, Collection<? extends CyIdentifiable> list, Boolean selected) {
		for (CyIdentifiable edge : list) {
			CyRow row = network.getRow(edge);
			row.set(CyNetwork.SELECTED, selected);
		}
		
	}
	
	public static List<CyNode> getSelectedNodes(CyNetwork cyNetwork) {
		return CyTableUtil.getNodesInState(cyNetwork, CyNetwork.SELECTED, true);
	}

	public static List<CyEdge> getSelectedEdges(CyNetwork cyNetwork) {
		return CyTableUtil.getEdgesInState(cyNetwork, CyNetwork.SELECTED, true);
	}
	
	public static void selectAllNodes(CyNetwork cyNetwork) {
		selectAll(cyNetwork, CyTableUtil.getNodesInState(cyNetwork, CyNetwork.SELECTED, false));
	}
	
	public static void selectAllEdges(CyNetwork cyNetwork) {
		selectAll(cyNetwork, CyTableUtil.getEdgesInState(cyNetwork, CyNetwork.SELECTED, false));
	}
	
	static <T extends CyIdentifiable> void selectAll(CyNetwork network, Collection<T> items) {
		for (T item : items) {
			CyRow row = network.getRow(item);
			row.set(CyNetwork.SELECTED, Boolean.TRUE);
		}
	}
}
