package org.cytoscape.task.internal.creation;

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


import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_CENTER_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_CENTER_Z_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_SCALE_FACTOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Z_LOCATION;

import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.work.undo.AbstractCyEdit;


/** An undoable edit that will undo and redo the creation of a network view. */ 
final class CreateNetworkViewEdit extends AbstractCyEdit {
	private final CyEventHelper eventHelper;
	private final CyNetwork network;
	private final CyNetworkViewFactory viewFactory;
	private final CyNetworkViewManager networkViewManager;
	private Map<CyNode, NodeLocations> nodesAndLocations;
	private double networkCenterX;
	private double networkCenterY;
	private double networkCenterZ;
	private double networkScaleFactor;

	CreateNetworkViewEdit(final CyEventHelper eventHelper, final CyNetwork network,
	                      final CyNetworkViewFactory viewFactory,
			      final CyNetworkViewManager networkViewManager)
	{
		super("Create Network View");

		this.eventHelper        = eventHelper;
		this.network            = network;
		this.viewFactory        = viewFactory;
		this.networkViewManager = networkViewManager;
	}

	public void redo() {
		;

		final CyNetworkView view =
			viewFactory.createNetworkView(network);
		networkViewManager.addNetworkView(view);

		for (final View<CyNode> nodeView : view.getNodeViews())
			nodesAndLocations.get(nodeView.getModel()).restoreLocations(nodeView);

		view.setVisualProperty(NETWORK_CENTER_X_LOCATION, networkCenterX);
		view.setVisualProperty(NETWORK_CENTER_Y_LOCATION, networkCenterY);
		view.setVisualProperty(NETWORK_CENTER_Z_LOCATION, networkCenterZ);
		view.setVisualProperty(NETWORK_SCALE_FACTOR, networkScaleFactor);

		eventHelper.flushPayloadEvents();
		view.updateView();
	}

	public void undo() {
		final Collection<CyNetworkView> views = networkViewManager.getNetworkViews(network);
		CyNetworkView view = null;
		if(views.size() != 0)
			view = views.iterator().next();

		networkCenterX = view.getVisualProperty(NETWORK_CENTER_X_LOCATION);
		networkCenterY = view.getVisualProperty(NETWORK_CENTER_Y_LOCATION);
		networkCenterZ = view.getVisualProperty(NETWORK_CENTER_Z_LOCATION);
		networkScaleFactor = view.getVisualProperty(NETWORK_SCALE_FACTOR);

		final Collection<View<CyNode>> nodeViews = view.getNodeViews();
		nodesAndLocations = new WeakHashMap<CyNode, NodeLocations>(nodeViews.size());
		for (final View<CyNode> nodeView : nodeViews)
			nodesAndLocations.put(nodeView.getModel(), new NodeLocations(nodeView));

		networkViewManager.destroyNetworkView(view);
	}
}


final class NodeLocations {
	private final double xLocation;
	private final double yLocation;
	private final double zLocation;

	NodeLocations(final View<CyNode> nodeView) {
		xLocation = nodeView.getVisualProperty(NODE_X_LOCATION);
		yLocation = nodeView.getVisualProperty(NODE_Y_LOCATION);
		zLocation = nodeView.getVisualProperty(NODE_Z_LOCATION);
	}

	void restoreLocations(final View<CyNode> nodeView) {
		nodeView.setVisualProperty(NODE_X_LOCATION, xLocation);
		nodeView.setVisualProperty(NODE_Y_LOCATION, yLocation);
		nodeView.setVisualProperty(NODE_Z_LOCATION, zLocation);
	}
}
