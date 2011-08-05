package org.cytoscape.cpath2.internal.util;

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
		setSelectedState(network.getNodeList(), Boolean.FALSE);
	}
	
	public static void unselectAllEdges(CyNetwork network) {
		setSelectedState(network.getEdgeList(), Boolean.FALSE);
	}

	public static void setSelectedNodeState(Collection<CyNode> list, boolean selected) {
		setSelectedState(list, selected);
	}

	public static void setSelectedEdgeState(Collection<CyEdge> list, boolean selected) {
		setSelectedState(list, selected);
	}

	static void setSelectedState(Collection<? extends CyTableEntry> list, Boolean selected) {
		for (CyTableEntry edge : list) {
			CyRow row = edge.getCyRow();
			row.set(CyNetwork.SELECTED, selected);
		}
		
	}
	
	public static Set<CyNode> getSelectedNodes(CyNetwork cyNetwork) {
		return getSelected(cyNetwork.getNodeList());
	}

	public static Set<CyEdge> getSelectedEdges(CyNetwork cyNetwork) {
		return getSelected(cyNetwork.getEdgeList());
	}
	
	static <T extends CyTableEntry> Set<T> getSelected(Collection<T> items) {
		Set<T> entries = new HashSet<T>();
		for (T item : items) {
			CyRow row = item.getCyRow();
			if (row.get(CyNetwork.SELECTED, Boolean.class)) {
				entries.add(item);
			}
		}
		return entries;
	}
	
	public static void selectAllNodes(CyNetwork cyNetwork) {
		selectAll(cyNetwork.getNodeList());
	}
	
	public static void selectAllEdges(CyNetwork cyNetwork) {
		selectAll(cyNetwork.getEdgeList());
	}
	
	static <T extends CyTableEntry> void selectAll(Collection<T> items) {
		for (T item : items) {
			CyRow row = item.getCyRow();
			if (!row.get(CyNetwork.SELECTED, Boolean.class)) {
				row.set(CyNetwork.SELECTED, Boolean.TRUE);
			}
		}
	}

	public static int getSelectedNodeCount(CyNetwork cyNetwork) {
		return countSelected(cyNetwork.getNodeList());
	}

	public static int getSelectedEdgeCount(CyNetwork cyNetwork) {
		return countSelected(cyNetwork.getNodeList());
	}
	
	static <T extends CyTableEntry> int countSelected(Collection<T> items) {
		int count = 0;
		for (T item : items) {
			CyRow row = item.getCyRow();
			if (row.get(CyNetwork.SELECTED, Boolean.class)) {
				count++;
			}
		}
		return count;
	}
}
