package org.cytoscape.task.internal.hide;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskIterator;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public abstract class AbstractUnHideTaskFactory extends AbstractNetworkViewTaskFactory {

	private final String description;
	private final boolean unhideNodes;
	private final boolean unhideEdges;
	private final boolean justSelected;
	private final CyServiceRegistrar serviceRegistrar;

	public AbstractUnHideTaskFactory(
			String description,
			boolean unhideNodes,
			boolean unhideEdges,
			boolean justSelected,
			CyServiceRegistrar serviceRegistrar
	) {
		this.description = description;
		this.unhideNodes = unhideNodes;
		this.unhideEdges = unhideEdges;
		this.justSelected = justSelected;
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView view) {
		return new TaskIterator(new UnHideFromSelectionTask(description, unhideNodes, unhideEdges, justSelected, view,
				serviceRegistrar));
	}
	
	@Override
	public boolean isReady(CyNetworkView networkView) {
		if (super.isReady(networkView)) {
			if (unhideNodes && hasHiddenNodes(networkView))
				return true;
			
			if (unhideEdges && hasHiddenEdges(networkView))
				return true;
		}
	
		return false;
	}
	
	private boolean hasHiddenNodes(CyNetworkView networkView) {
		// fast path
		if (networkView.supportsSnapshots()) {
			var snapshot = networkView.createSnapshot();
			if (snapshot != null && snapshot.isTrackedNodeKey("HIDDEN_NODES")) {
				return snapshot.getTrackedNodeCount("HIDDEN_NODES") > 0;
			}
		}
		
		// slow path
		var views = networkView.getNodeViewsIterable();
		
		return hasHidden(views, BasicVisualLexicon.NODE_VISIBLE);
	}
	
	private boolean hasHiddenEdges(CyNetworkView networkView) {
		// fast path
		if (networkView.supportsSnapshots()) {
			var snapshot = networkView.createSnapshot();
			if (snapshot != null && snapshot.isTrackedEdgeKey("HIDDEN_EDGES")) {
				return snapshot.getTrackedEdgeCount("HIDDEN_EDGES") > 0;
			}
		}

		// Maybe bail out, checking all edges for visibility doesn't scale for large networks
		if (networkView.getModel().getEdgeCount() > 400000)
			return true;
		
		// slow path
		var views = networkView.getEdgeViewsIterable();
		
		return hasHidden(views, BasicVisualLexicon.EDGE_VISIBLE);
	}
	
	private <T> boolean hasHidden(Iterable<View<T>> views, VisualProperty<Boolean> vp) {
		for (var v : views) {
			if (Boolean.FALSE.equals(v.getVisualProperty(vp)))
				return true;
		}
		
		return false;
	}

	public String getDescription() {
		return description;
	}
}
