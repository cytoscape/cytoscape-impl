/*
 File: AddDeleteHandler.java

 Copyright (c) 2008, 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.ding.impl;


import org.cytoscape.ding.GraphView;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.events.AboutToRemoveEdgeViewsEvent;
import org.cytoscape.view.model.events.AboutToRemoveEdgeViewsListener;
import org.cytoscape.view.model.events.AboutToRemoveNodeViewsEvent;
import org.cytoscape.view.model.events.AboutToRemoveNodeViewsListener;
import org.cytoscape.view.model.events.AddedEdgeViewsEvent;
import org.cytoscape.view.model.events.AddedEdgeViewsListener;
import org.cytoscape.view.model.events.AddedNodeViewsEvent;
import org.cytoscape.view.model.events.AddedNodeViewsListener;



/**
 * Listens for Add/Delete Node/Edge events and updated a GraphView accordingly. 
 */
public class AddDeleteHandler 
	implements AddedEdgeViewsListener, 
	           AddedNodeViewsListener,
	           AboutToRemoveEdgeViewsListener, 
	           AboutToRemoveNodeViewsListener
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

	public void handleEvent(AboutToRemoveEdgeViewsEvent e) {
		if ( e.getSource() != this.networkView )
			return;
		
		for ( View<CyEdge> edgeView : e.getEdgeViews()) 
			view.removeEdgeView(edgeView.getModel());

		view.updateView();
	}

	public void handleEvent(AboutToRemoveNodeViewsEvent e) {
		if ( e.getSource() != this.networkView )
			return;
		
		for ( View<CyNode> nodeView : e.getNodeViews()) 
			view.removeNodeView(nodeView.getModel());

		view.updateView();
	}
}
