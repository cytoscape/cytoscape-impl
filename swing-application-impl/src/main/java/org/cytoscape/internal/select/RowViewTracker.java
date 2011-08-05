
package org.cytoscape.internal.select;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
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
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;

class RowViewTracker implements NetworkViewAddedListener, 
	AddedNodeViewsListener, AddedEdgeViewsListener, 
	AboutToRemoveNodeViewsListener, AboutToRemoveEdgeViewsListener {

	private Map<CyRow,View<?>> rowViewMap;

	public RowViewTracker() {
		rowViewMap = new HashMap<CyRow,View<?>>();
	}

	public void handleEvent(NetworkViewAddedEvent e) {
		final CyNetworkView view = e.getNetworkView();

		for ( View<CyNode> nv : view.getNodeViews() )
			rowViewMap.put( nv.getModel().getCyRow(), nv);
		
		for ( View<CyEdge> ev : view.getEdgeViews() ) 
			rowViewMap.put( ev.getModel().getCyRow(), ev);
	}
	
	public void handleEvent(AddedNodeViewsEvent e) {
		for ( View<CyNode> v : e.getNodeViews()) 
			rowViewMap.put( v.getModel().getCyRow(), v );
	}
	
	public void handleEvent(AddedEdgeViewsEvent e) {
		for ( View<CyEdge> v : e.getEdgeViews()) 
			rowViewMap.put( v.getModel().getCyRow(), v );
	}
	
	public void handleEvent(AboutToRemoveNodeViewsEvent e) {
		for ( View<CyNode> v : e.getNodeViews()) 
			rowViewMap.remove( v.getModel().getCyRow() );
	}
	
	public void handleEvent(AboutToRemoveEdgeViewsEvent e) {
		for ( View<CyEdge> v : e.getEdgeViews()) 
			rowViewMap.remove( v.getModel().getCyRow() );
	}
	
	public Map<CyRow,View<?>> getRowViewMap() {
		return Collections.unmodifiableMap(rowViewMap);  
	}
}
