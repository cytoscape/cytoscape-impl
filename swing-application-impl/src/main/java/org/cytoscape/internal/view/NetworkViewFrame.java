package org.cytoscape.internal.view;

import javax.swing.JFrame;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

@SuppressWarnings("serial")
public class NetworkViewFrame extends JFrame {

	private final CyNetworkView networkView;
	private final RenderingEngineFactory<CyNetwork> engineFactory;
	private final RenderingEngine<CyNetwork> renderingEngine;
	
	private final CyServiceRegistrar serviceRegistrar;

	public NetworkViewFrame(final String name, final CyNetworkView networkView,
			final RenderingEngineFactory<CyNetwork> engineFactory, final CyServiceRegistrar serviceRegistrar) {
		super(networkView.getVisualProperty(BasicVisualLexicon.NETWORK_TITLE));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		this.networkView = networkView;
		this.engineFactory = engineFactory;
		this.serviceRegistrar = serviceRegistrar;
		
		setName(name);
		
		renderingEngine = engineFactory.createRenderingEngine(this, networkView);
	}
	
	protected RenderingEngine<CyNetwork> getRenderingEngine() {
		return renderingEngine;
	}
	
	protected CyNetworkView getNetworkView() {
		return networkView;
	}
}
