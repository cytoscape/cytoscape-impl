package org.cytoscape.task.internal.hide;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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


import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_VISIBLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_VISIBLE;

import java.util.Collection;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;


abstract class HideUtils {

	static void setVisibleNodes(Collection<CyNode> nodes, boolean visible, CyNetworkView view) {
		final CyNetwork net = view.getModel();
		
		for (CyNode n : nodes) {
			if (visible)
				view.getNodeView(n).clearValueLock(NODE_VISIBLE);
			else
				view.getNodeView(n).setLockedValue(NODE_VISIBLE, false);

			for (CyNode n2 : net.getNeighborList(n, CyEdge.Type.ANY)) {
				for (CyEdge e : net.getConnectingEdgeList(n, n2, CyEdge.Type.ANY)) {
					final View<CyEdge> ev = view.getEdgeView(e);
					
					if (visible)
						ev.clearValueLock(EDGE_VISIBLE);
					else
						ev.setLockedValue(EDGE_VISIBLE, false);
				}
			}
		}
	}

	static void setVisibleEdges(Collection<CyEdge> edges, boolean visible, CyNetworkView view) {
		for (CyEdge e : edges) {
			final View<CyEdge> ev = view.getEdgeView(e);
					
			if (visible)
				ev.clearValueLock(EDGE_VISIBLE);
			else
				ev.setLockedValue(EDGE_VISIBLE, false);
		}
	}
}
