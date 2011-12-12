package org.cytoscape.filter.internal.filters.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableEntry;

public class SelectUtil {
	public static void unselectAllNodes(CyNetwork network) {
		setSelectedState(network, network.getNodeList(), Boolean.FALSE);
	}
	
	public static void unselectAllEdges(CyNetwork network) {
		setSelectedState(network, network.getEdgeList(), Boolean.FALSE);
	}

	public static void setSelectedNodeState(CyNetwork network, Collection<CyNode> list, boolean selected) {
		setSelectedState(network, list, selected);
	}

	public static void setSelectedEdgeState(CyNetwork network, Collection<CyEdge> list, boolean selected) {
		setSelectedState(network, list, selected);
	}

	static void setSelectedState(CyNetwork network, Collection<? extends CyTableEntry> list, Boolean selected) {
		for (CyTableEntry edge : list) {
			CyRow row = network.getRow(edge);
			row.set(CyNetwork.SELECTED, selected);
		}
		
	}
	
	public static Set<CyNode> getSelectedNodes(CyNetwork cyNetwork) {
		return getSelected(cyNetwork, cyNetwork.getNodeList());
	}

	public static Set<CyEdge> getSelectedEdges(CyNetwork cyNetwork) {
		return getSelected(cyNetwork, cyNetwork.getEdgeList());
	}
	
	static <T extends CyTableEntry> Set<T> getSelected(CyNetwork network, Collection<T> items) {
		Set<T> entries = new HashSet<T>();
		for (T item : items) {
			CyRow row = network.getRow(item);
			if (row.get(CyNetwork.SELECTED, Boolean.class)) {
				entries.add(item);
			}
		}
		return entries;
	}
	
	public static void selectAllNodes(CyNetwork cyNetwork) {
		selectAll(cyNetwork,cyNetwork.getNodeList());
	}
	
	public static void selectAllEdges(CyNetwork cyNetwork) {
		selectAll(cyNetwork,cyNetwork.getEdgeList());
	}
	
	static <T extends CyTableEntry> void selectAll(CyNetwork network, Collection<T> items) {
		for (T item : items) {
			CyRow row = network.getRow(item);
			if (!row.get(CyNetwork.SELECTED, Boolean.class)) {
				row.set(CyNetwork.SELECTED, Boolean.TRUE);
			}
		}
	}

	public static int getSelectedNodeCount(CyNetwork cyNetwork) {
		return countSelected(cyNetwork, cyNetwork.getNodeList());
	}

	public static int getSelectedEdgeCount(CyNetwork cyNetwork) {
		return countSelected(cyNetwork, cyNetwork.getNodeList());
	}
	
	static <T extends CyTableEntry> int countSelected(CyNetwork network, Collection<T> items) {
		int count = 0;
		for (T item : items) {
			CyRow row = network.getRow(item);
			if (row.get(CyNetwork.SELECTED, Boolean.class)) {
				count++;
			}
		}
		return count;
	}
	

}
