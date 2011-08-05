package org.cytoscape.view.model.internal;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.events.AboutToRemoveEdgesEvent;
import org.cytoscape.model.events.AboutToRemoveEdgesListener;
import org.cytoscape.model.events.AboutToRemoveNodesEvent;
import org.cytoscape.model.events.AboutToRemoveNodesListener;
import org.cytoscape.model.events.AddedEdgesEvent;
import org.cytoscape.model.events.AddedEdgesListener;
import org.cytoscape.model.events.AddedNodesEvent;
import org.cytoscape.model.events.AddedNodesListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.AboutToRemoveEdgeViewsEvent;
import org.cytoscape.view.model.events.AboutToRemoveNodeViewsEvent;
import org.cytoscape.view.model.events.AddedEdgeViewsEvent;
import org.cytoscape.view.model.events.AddedNodeViewsEvent;
import org.cytoscape.view.model.events.FitContentEvent;
import org.cytoscape.view.model.events.FitSelectedEvent;
import org.cytoscape.view.model.events.NetworkViewChangedEvent;
import org.cytoscape.view.model.events.UpdateNetworkPresentationEvent;
import org.cytoscape.view.model.events.ViewChangeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Row-oriented implementation of CyNetworkView model. This is a consolidated
 * view model representing a network.
 */
public class NetworkViewImpl extends ViewImpl<CyNetwork> implements CyNetworkView, AddedEdgesListener,
		AddedNodesListener, AboutToRemoveEdgesListener, AboutToRemoveNodesListener
{
	private static final Logger logger = LoggerFactory.getLogger(NetworkViewImpl.class);

	private final Map<CyNode, View<CyNode>> nodeViews;
	private final Map<CyEdge, View<CyEdge>> edgeViews;

	/**
	 * Create a new instance of a network view model.
	 * This constructor do NOT fire event for presentation layer.
	 * 
	 * @param network
	 * @param cyEventHelper
	 */
	public NetworkViewImpl(final CyNetwork network, final CyEventHelper cyEventHelper) {
		super(network, cyEventHelper);

		nodeViews = new HashMap<CyNode, View<CyNode>>();
		edgeViews = new HashMap<CyEdge, View<CyEdge>>();

		for (final CyNode node : network.getNodeList())
			nodeViews.put(node, new NodeViewImpl(node, cyEventHelper, this));

		for (CyEdge edge : network.getEdgeList())
			edgeViews.put(edge, new EdgeViewImpl(edge, cyEventHelper, this));
		
		logger.info("Network View Model Created.  Model ID = " + this.getModel().getSUID() + ", View Model ID = " + suid + " First phase of network creation process (model creation) is done. \n\n");
	}


	@Override
	public View<CyNode> getNodeView(CyNode node) {
		return this.nodeViews.get(node);
	}

	@Override
	public Collection<View<CyNode>> getNodeViews() {
		return this.nodeViews.values();
	}

	@Override
	public View<CyEdge> getEdgeView(final CyEdge edge) {
		return this.edgeViews.get(edge);
	}

	@Override
	public Collection<View<CyEdge>> getEdgeViews() {
		return this.edgeViews.values();
	}

	@Override
	public Collection<View<? extends CyTableEntry>> getAllViews() {
		final Set<View<? extends CyTableEntry>> views = new HashSet<View<? extends CyTableEntry>>();

		views.addAll(nodeViews.values());
		views.addAll(edgeViews.values());
		views.add(this);

		return views;
	}

	
	// /// Event Handlers //////

	@Override
	public void handleEvent(AboutToRemoveNodesEvent e) {
		if (model != e.getSource())
			return;

		List<View<CyNode>> nvs = new ArrayList<View<CyNode>>(e.getNodes().size());
		for ( CyNode n : e.getNodes()) {
			View<CyNode> v = nodeViews.get(n);
			if ( v != null)
				nvs.add(v);
		}
		
		if (nvs.size() <= 0)
			return;
		
		cyEventHelper.fireEvent(new AboutToRemoveNodeViewsEvent(this, nvs));

		for ( CyNode n : e.getNodes()) 
			nodeViews.remove(n);
		
	}

	@Override
	public void handleEvent(AboutToRemoveEdgesEvent e) {
		if (model != e.getSource())
			return;

		List<View<CyEdge>> evs = new ArrayList<View<CyEdge>>(e.getEdges().size());
		for ( CyEdge edge : e.getEdges() ) {
			View<CyEdge> v = edgeViews.get(edge);
			if ( v != null)
				evs.add(v);
		}

		if (evs.size() <= 0)
			return;

		cyEventHelper.fireEvent(new AboutToRemoveEdgeViewsEvent(this, evs));

		for ( CyEdge edge : e.getEdges() ) 
			edgeViews.remove(edge);
	}

	
	@Override
	public void handleEvent(final AddedNodesEvent e) {
		// Respond to the event only if the source is equal to the network model associated with this view.
		if (model != e.getSource())
			return;

		List<View<CyNode>> nl = new ArrayList<View<CyNode>>(e.getPayloadCollection().size());
		for ( CyNode node : e.getPayloadCollection()) {
			logger.debug("Creating new node view model: " + node.toString());
			final View<CyNode> nv = new NodeViewImpl(node, cyEventHelper, this);
			nodeViews.put(node, nv);
			nl.add(nv);
		}
		
		// Cascading event.
		cyEventHelper.fireEvent(new AddedNodeViewsEvent(this, nl));
	}

	
	@Override
	public void handleEvent(final AddedEdgesEvent e) {
		if (model != e.getSource())
			return;

		List<View<CyEdge>> el = new ArrayList<View<CyEdge>>(e.getPayloadCollection().size());
		for ( CyEdge edge : e.getPayloadCollection()) {
			final View<CyEdge> ev = new EdgeViewImpl(edge, cyEventHelper, this);
			edgeViews.put(edge, ev); // FIXME: View creation here and in initializer: should be in one place
			el.add(ev);
		}
		
		cyEventHelper.fireEvent(new AddedEdgeViewsEvent(this, el));
	}
	
	// The following methods are utilities for calling methods in upper layer (presentation)
	
	@Override
	public void fitContent() {
		logger.debug("Firing fitContent event from: View ID = " + this.suid);
		cyEventHelper.fireEvent( new FitContentEvent(this));
	}
	
	@Override
	public void fitSelected() {
		logger.debug("Firing fitSelected event from: View ID = " + this.suid);
		cyEventHelper.fireEvent( new FitSelectedEvent(this));
	}
	
	@Override
	public void updateView() {
		logger.debug("Firing update view event from: View ID = " + this.suid);
		cyEventHelper.fireEvent( new UpdateNetworkPresentationEvent(this));
	}


	@Override
	public <T, V extends T> void setVisualProperty(VisualProperty<? extends T> vp, V value) {
		if(value == null)
			this.visualProperties.remove(vp);
		else
			this.visualProperties.put(vp, value);
		
		cyEventHelper.addEventPayload((CyNetworkView)this, new ViewChangeRecord<CyNetwork>(this, vp, value), NetworkViewChangedEvent.class);	
	}

}
