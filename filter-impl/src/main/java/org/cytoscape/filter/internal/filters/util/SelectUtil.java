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
