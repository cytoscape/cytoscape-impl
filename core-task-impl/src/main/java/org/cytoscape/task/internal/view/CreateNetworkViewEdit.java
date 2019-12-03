package org.cytoscape.task.internal.view;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_CENTER_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_CENTER_Z_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_SCALE_FACTOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Z_LOCATION;

import java.util.Map;
import java.util.WeakHashMap;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.work.undo.AbstractCyEdit;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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

/** An undoable edit that will undo and redo the creation of a network view. */ 
final class CreateNetworkViewEdit extends AbstractCyEdit {
	
	private final CyNetwork network;
	private Map<CyNode, NodeLocations> nodesAndLocations;
	private double networkCenterX;
	private double networkCenterY;
	private double networkCenterZ;
	private double networkScaleFactor;
	
	private final CyServiceRegistrar serviceRegistrar;

	CreateNetworkViewEdit(CyNetwork network, CyServiceRegistrar serviceRegistrar) {
		super("Create Network View");

		this.network = network;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void redo() {
		var view = serviceRegistrar.getService(CyNetworkViewFactory.class).createNetworkView(network);
		serviceRegistrar.getService(CyNetworkViewManager.class).addNetworkView(view);

		for (var nv : view.getNodeViews())
			nodesAndLocations.get(nv.getModel()).restoreLocations(nv);

		view.setVisualProperty(NETWORK_CENTER_X_LOCATION, networkCenterX);
		view.setVisualProperty(NETWORK_CENTER_Y_LOCATION, networkCenterY);
		view.setVisualProperty(NETWORK_CENTER_Z_LOCATION, networkCenterZ);
		view.setVisualProperty(NETWORK_SCALE_FACTOR, networkScaleFactor);

		serviceRegistrar.getService(CyEventHelper.class).flushPayloadEvents();
		view.updateView();
	}

	@Override
	public void undo() {
		var netViewManager = serviceRegistrar.getService(CyNetworkViewManager.class);
		var views = netViewManager.getNetworkViews(network);
		CyNetworkView view = null;

		if (views.size() != 0)
			view = views.iterator().next();

		networkCenterX = view.getVisualProperty(NETWORK_CENTER_X_LOCATION);
		networkCenterY = view.getVisualProperty(NETWORK_CENTER_Y_LOCATION);
		networkCenterZ = view.getVisualProperty(NETWORK_CENTER_Z_LOCATION);
		networkScaleFactor = view.getVisualProperty(NETWORK_SCALE_FACTOR);

		var nodeViews = view.getNodeViews();
		nodesAndLocations = new WeakHashMap<>(nodeViews.size());

		for (var nv : nodeViews)
			nodesAndLocations.put(nv.getModel(), new NodeLocations(nv));

		netViewManager.destroyNetworkView(view);
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
