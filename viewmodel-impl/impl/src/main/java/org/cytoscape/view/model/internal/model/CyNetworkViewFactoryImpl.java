package org.cytoscape.view.model.internal.model;

import java.util.Properties;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewListener;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.internal.CyNetworkViewConfigImpl;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;



public class CyNetworkViewFactoryImpl implements CyNetworkViewFactory {

	private final CyServiceRegistrar registrar;
	private final VisualLexicon visualLexicon;
	private final String rendererId;
	private final CyNetworkViewConfigImpl config;
	
	public CyNetworkViewFactoryImpl(CyServiceRegistrar registrar, VisualLexicon visualLexicon, String rendererId, CyNetworkViewConfigImpl config) {
		this.registrar = registrar;
		this.visualLexicon = visualLexicon;
		this.rendererId = rendererId;
		this.config = config;
	}
	
	@Override
	public CyNetworkView createNetworkView(CyNetwork network) {
		CyNetworkViewImpl networkView = createNetworkViewImpl(network);
		listenForModelChanges(networkView);
		return networkView;
	}
	
	
	private CyNetworkViewImpl createNetworkViewImpl(CyNetwork network) {
		CyNetworkViewImpl networkViewImpl = new CyNetworkViewImpl(registrar, network, visualLexicon, rendererId, config);
		
		for(CyNode node : network.getNodeList()) {
			View<CyNode> view = networkViewImpl.addNode(node);
			// calling getDefaultNodeTable() is faster than calling getModel().getRow(node).
			CyRow row = network.getDefaultNodeTable().getRow(node.getSUID());
			Boolean selected = row.get(CyNetwork.SELECTED, Boolean.class);
			view.setVisualProperty(BasicVisualLexicon.NODE_SELECTED, selected);
		}
		
		for(CyEdge edge : network.getEdgeList()) {
			View<CyEdge> view = networkViewImpl.addEdge( edge);
			if(view != null) {
				// calling getDefaultNodeTable() is faster than calling getModel().getRow(node).
				CyRow row = network.getDefaultEdgeTable().getRow(edge.getSUID());
				Boolean selected = row.get(CyNetwork.SELECTED, Boolean.class);
				view.setVisualProperty(BasicVisualLexicon.EDGE_SELECTED, selected);
			}
		}
		
		return networkViewImpl;
	}
	
	
	private void listenForModelChanges(CyNetworkViewImpl networkView) {
		NetworkModelListener modelListener = new NetworkModelListener(networkView, registrar);
		
		networkView.addNetworkViewListener(new CyNetworkViewListener() {
			@Override public void handleDispose() {
				registrar.unregisterAllServices(modelListener);
			}
		}); 
		
		registrar.registerAllServices(modelListener, new Properties());
	}
}
