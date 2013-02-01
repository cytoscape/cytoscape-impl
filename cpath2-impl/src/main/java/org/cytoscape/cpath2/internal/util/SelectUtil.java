package org.cytoscape.cpath2.internal.util;

/*
 * #%L
 * Cytoscape CPath2 Impl (cpath2-impl)
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
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyIdentifiable;

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

	static void setSelectedState(CyNetwork network, Collection<? extends CyIdentifiable> list, Boolean selected) {
		for (CyIdentifiable edge : list) {
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
	
	static <T extends CyIdentifiable> Set<T> getSelected(CyNetwork network, Collection<T> items) {
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
		selectAll(cyNetwork, cyNetwork.getNodeList());
	}
	
	public static void selectAllEdges(CyNetwork cyNetwork) {
		selectAll(cyNetwork, cyNetwork.getEdgeList());
	}
	
	static <T extends CyIdentifiable> void selectAll(CyNetwork network, Collection<T> items) {
		for (T item : items) {
			CyRow row = network.getRow(item);
			if (!row.get(CyNetwork.SELECTED, Boolean.class)) {
				row.set(CyNetwork.SELECTED, Boolean.TRUE);
			}
		}
	}

	public static int getSelectedNodeCount(CyNetwork cyNetwork) {
		return countSelected(cyNetwork,cyNetwork.getNodeList());
	}

	public static int getSelectedEdgeCount(CyNetwork cyNetwork) {
		return countSelected(cyNetwork,cyNetwork.getNodeList());
	}
	
	static <T extends CyIdentifiable> int countSelected(CyNetwork network,Collection<T> items) {
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
