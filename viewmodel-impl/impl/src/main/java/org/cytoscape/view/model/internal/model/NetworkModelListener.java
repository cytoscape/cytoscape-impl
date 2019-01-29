package org.cytoscape.view.model.internal.model;

import java.util.Collection;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.AboutToRemoveEdgesEvent;
import org.cytoscape.model.events.AboutToRemoveEdgesListener;
import org.cytoscape.model.events.AboutToRemoveNodesEvent;
import org.cytoscape.model.events.AboutToRemoveNodesListener;
import org.cytoscape.model.events.AddedEdgesEvent;
import org.cytoscape.model.events.AddedEdgesListener;
import org.cytoscape.model.events.AddedNodesEvent;
import org.cytoscape.model.events.AddedNodesListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.events.AboutToRemoveEdgeViewsEvent;
import org.cytoscape.view.model.events.AboutToRemoveNodeViewsEvent;
import org.cytoscape.view.model.events.AddedEdgeViewsEvent;
import org.cytoscape.view.model.events.AddedNodeViewsEvent;

public class NetworkModelListener implements AddedNodesListener, AddedEdgesListener, AboutToRemoveNodesListener, AboutToRemoveEdgesListener {

	private final CyServiceRegistrar registrar;
	private final CyNetworkViewImpl networkView;
	
	
	public NetworkModelListener(CyNetworkViewImpl networkView, CyServiceRegistrar registrar) {
		this.networkView = networkView;
		this.registrar = registrar;
	}
	
	
	private CyEventHelper getEventHelper() {
		return registrar.getService(CyEventHelper.class);
	}
	
	
	@Override
	public void handleEvent(AddedNodesEvent e) {
		if(networkView.getModel() != e.getSource())
			return;
		
		Collection<CyNode> nodes = e.getPayloadCollection();
		for(CyNode node : nodes) {
			View<CyNode> view = networkView.addNode(node);
			if(view != null) {
				getEventHelper().addEventPayload(networkView, view, AddedNodeViewsEvent.class);
			}
		}
	}
	
	@Override
	public void handleEvent(AddedEdgesEvent e) {
		if(networkView.getModel() != e.getSource())
			return;
		
		Collection<CyEdge> edges = e.getPayloadCollection();
		for(CyEdge edge : edges) {
			View<CyEdge> view = networkView.addEdge(edge);
			if(view != null) {
				getEventHelper().addEventPayload(networkView, view, AddedEdgeViewsEvent.class);
			}
		}

	}
	
	@Override
	public void handleEvent(AboutToRemoveNodesEvent e) {
		if(networkView.getModel() != e.getSource())
			return;

		for(CyNode node : e.getNodes()) {
			View<CyNode> view = networkView.removeNode(node);
			getEventHelper().addEventPayload(networkView, view, AboutToRemoveNodeViewsEvent.class);
		}
	}
	
	@Override
	public void handleEvent(AboutToRemoveEdgesEvent e) {
		if(networkView.getModel() != e.getSource())
			return;

		for(CyEdge edge : e.getEdges()) {
			View<CyEdge> view = networkView.removeEdge(edge);
			getEventHelper().addEventPayload(networkView, view, AboutToRemoveEdgeViewsEvent.class);
		}
	}
	

}
