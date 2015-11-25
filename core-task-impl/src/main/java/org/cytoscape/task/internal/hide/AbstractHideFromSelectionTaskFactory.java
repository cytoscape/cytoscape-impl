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

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskIterator;

public abstract class AbstractHideFromSelectionTaskFactory extends AbstractNetworkViewTaskFactory {
	
	protected final String description;
	protected final boolean hideNodes;
	protected final boolean hideEdges;
	protected final boolean selectionValue;
	protected final CyServiceRegistrar serviceRegistrar;
	
	public AbstractHideFromSelectionTaskFactory(
			final String description,
			final boolean hideNodes,
			final boolean hideEdges,
			final boolean selectionValue,
			final CyServiceRegistrar serviceRegistrar
	) {
		this.description = description;
		this.hideNodes = hideNodes;
		this.hideEdges = hideEdges;
		this.selectionValue = selectionValue;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public TaskIterator createTaskIterator(final CyNetworkView view) {
		return new TaskIterator(
				new HideFromSelectionTask(description, hideNodes, hideEdges, selectionValue, view, serviceRegistrar));
	}
	
	@Override
	public boolean isReady(final CyNetworkView networkView) {
		if (super.isReady(networkView)) {
			if (networkView != null && !selectionValue) {
				// Also check if there are any unselected elements that are also visible...
				final CyNetwork network = networkView.getModel();
				final List<CyIdentifiable> elements = new ArrayList<>();
				
				if (hideNodes)
					elements.addAll(CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, false));
				if (hideEdges)
					elements.addAll(CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, false));
				
				for (CyIdentifiable e : elements) {
					if (e instanceof CyNode) {
						final View<CyNode> nv = networkView.getNodeView((CyNode)e);
						
						if (nv != null && nv.getVisualProperty(BasicVisualLexicon.NODE_VISIBLE) == Boolean.TRUE)
							return true;
					} else if (e instanceof CyEdge) {
						final View<CyEdge> ev = networkView.getEdgeView((CyEdge)e);
						
						if (ev != null && ev.getVisualProperty(BasicVisualLexicon.EDGE_VISIBLE) == Boolean.TRUE)
							return true;
					}
				}
			} else {
				return true;
			}
		}
		
		return false;
	}
	
	public String getDescription() {
		return description;
	}
}
