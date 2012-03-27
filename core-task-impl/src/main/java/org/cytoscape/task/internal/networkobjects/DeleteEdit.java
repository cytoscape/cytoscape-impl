package org.cytoscape.task.internal.networkobjects;


import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.undo.AbstractCyEdit;



/**
 * An undoable edit that will undo and redo deletion of nodes and edges.
 */ 
final class DeleteEdit extends AbstractCyEdit {
	private final List<CyNode> nodes;
	private final Set<CyEdge> edges;
	private final double[] xPos;
	private final double[] yPos;
	private final CySubNetwork net;
	private final CyNetworkViewManager netViewMgr;
	private final VisualMappingManager visualMappingManager;
	private final CyEventHelper eventHelper;
	
	DeleteEdit(final CySubNetwork net, final List<CyNode> nodes, final Set<CyEdge> edges,
		   final CyNetworkViewManager netViewMgr,
		   final VisualMappingManager visualMappingManager,
		   final CyEventHelper eventHelper)
	{
		super("Delete");

		if (net == null)
			throw new NullPointerException("network is null");
		this.net = net;

		if (netViewMgr == null)
			throw new NullPointerException("network manager is null");
		this.netViewMgr = netViewMgr;

		if (nodes == null)
			throw new NullPointerException("nodes is null");
		this.nodes = nodes; 

		if (edges == null)
			throw new NullPointerException("edges is null");
		this.edges = edges; 

		if (visualMappingManager == null)
			throw new NullPointerException("visualMappingManager is null");
		this.visualMappingManager = visualMappingManager;

		if (eventHelper == null)
			throw new NullPointerException("eventHelper is null");
		this.eventHelper = eventHelper;

		// save the positions of the nodes
		xPos = new double[nodes.size()]; 
		yPos = new double[nodes.size()];
		final Collection<CyNetworkView> views = netViewMgr.getNetworkViews(net);
		CyNetworkView netView = null;
		if(views.size() != 0)
			netView = views.iterator().next();

		if (netView != null) {
			int i = 0;
			for (CyNode n : nodes) {
				View<CyNode> nv = netView.getNodeView(n);
				xPos[i] = nv.getVisualProperty(NODE_X_LOCATION);
				yPos[i] = nv.getVisualProperty(NODE_Y_LOCATION);
				i++;
			}
		}
	}

	public void redo() {
		net.removeNodes(nodes);
		net.removeEdges(edges);

		final Collection<CyNetworkView> views = netViewMgr.getNetworkViews(net);
		CyNetworkView netView = null;
		if(views.size() != 0)
			netView = views.iterator().next();
		
		// Manually call update presentation
		netView.updateView();
	}

	public void undo() {
		for (CyNode n : nodes)
			net.addNode(n);
		for (CyEdge e : edges)
			net.addEdge(e);

		eventHelper.flushPayloadEvents();

		final Collection<CyNetworkView> views = netViewMgr.getNetworkViews(net);
		CyNetworkView netView = null;
		if(views.size() != 0)
			netView = views.iterator().next();
		
		if (netView != null) {
			int i = 0;
			for (final CyNode node : nodes) {
				View<CyNode> nodeView = netView.getNodeView(node);
				nodeView.setVisualProperty(NODE_X_LOCATION, xPos[i]);
				nodeView.setVisualProperty(NODE_Y_LOCATION, yPos[i] );
				i++;
			}
		}
		visualMappingManager.getVisualStyle(netView).apply(netView);

		netView.updateView();
	}
}
