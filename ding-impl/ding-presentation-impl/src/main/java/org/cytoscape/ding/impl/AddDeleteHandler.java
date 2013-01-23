package org.cytoscape.ding.impl;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
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


import org.cytoscape.ding.GraphView;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.events.AddedEdgeViewsEvent;
import org.cytoscape.view.model.events.AddedEdgeViewsListener;
import org.cytoscape.view.model.events.AddedNodeViewsEvent;
import org.cytoscape.view.model.events.AddedNodeViewsListener;



/**
 * Listens for Add/Delete Node/Edge events and updated a GraphView accordingly. 
 */
public class AddDeleteHandler 
	implements AddedEdgeViewsListener, 
	           AddedNodeViewsListener
{
	private final GraphView view;
	private final CyNetworkView networkView;

	public AddDeleteHandler(final GraphView view) {
		this.view = view;
		this.networkView = view.getViewModel();
	}

	public void handleEvent(final AddedEdgeViewsEvent e) {
		if ( networkView != e.getSource() )
			return;

		for ( View<CyEdge> ev : e.getPayloadCollection() )
			view.addEdgeView(ev.getModel());

		view.updateView();
	}

	public void handleEvent(final AddedNodeViewsEvent e) {
		if ( networkView != e.getSource() )
			return;

		for ( View<CyNode> nv : e.getPayloadCollection())
			view.addNodeView(nv.getModel());
		
		view.updateView();
	}

}
