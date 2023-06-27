package org.cytoscape.ding.impl;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.impl.cyannotator.AnnotationFactoryManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewFactoryConfig;
import org.cytoscape.view.model.CyNetworkViewFactoryProvider;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.HandleFactory;

/**
 * Wraps the CyNetworkViewFactory provided by the viewmodel-impl bundle.
 * Maintains a Map of each DRenderingEngine created for each CyNetworkView.
 * 
 * Many methods in the Cytoscape API just pass the instance of CyNetworkView so the corresponding
 * DRenderingEngine can be looked up here.
 *
 */
public class DingNetworkViewFactory implements CyNetworkViewFactory, NetworkViewAboutToBeDestroyedListener {

	public static final Object ANIMATED_EDGES = "ANIMATED_EDGES";
	public static final Object SELECTED_NODES = "SELECTED_NODES";
	public static final Object HIDDEN_NODES = "HIDDEN_NODES";
	public static final Object HIDDEN_EDGES = "HIDDEN_EDGES";
	
	
	private final CyNetworkViewFactory delegateFactory;
	private final Map<CyNetworkView, DRenderingEngine> mainRenderingEngines = new ConcurrentHashMap<>();

	private final DVisualLexicon dingLexicon;
	private final AnnotationFactoryManager annMgr;
	private final CyServiceRegistrar registrar;
	private final DingGraphLOD dingGraphLOD;
	private final HandleFactory handleFactory; 
	
	public DingNetworkViewFactory(
			CyNetworkViewFactory delegateFactory, 
			DVisualLexicon dingLexicon,
			AnnotationFactoryManager annMgr,
			DingGraphLOD dingGraphLOD,
			HandleFactory handleFactory,
			CyServiceRegistrar registrar) {
		
		this.delegateFactory = delegateFactory;
		this.dingLexicon = dingLexicon;
		this.annMgr = annMgr;
		this.handleFactory = handleFactory;
		this.dingGraphLOD = dingGraphLOD;
		this.registrar = registrar;
	}

	/**
	 * Get the default config and add a tracked visual property for animated edges. 
	 * This makes it easy to quickly find the animated edges in DRenderingEngine.
	 */
	public static CyNetworkViewFactoryConfig getNetworkViewConfig(CyNetworkViewFactoryProvider factoryFactory, DVisualLexicon dVisualLexicon) {
		CyNetworkViewFactoryConfig config = factoryFactory.createConfig(dVisualLexicon);
		// Do not track selected edges, its too performance intensive
		config.addTrackedVisualProperty(SELECTED_NODES, BasicVisualLexicon.NODE_SELECTED, Boolean.TRUE::equals);
		config.addTrackedVisualProperty(ANIMATED_EDGES, BasicVisualLexicon.EDGE_LINE_TYPE, dVisualLexicon::isAnimated);
		// This is an optimization for CyNetworkViewMediator
		config.addTrackedVisualProperty(HIDDEN_NODES, BasicVisualLexicon.NODE_VISIBLE, Boolean.FALSE::equals);
		config.addTrackedVisualProperty(HIDDEN_EDGES, BasicVisualLexicon.EDGE_VISIBLE, Boolean.FALSE::equals);
		return config;
	}
	
	@Override
	public CyNetworkView createNetworkView(CyNetwork network) {
		// Create a CyNetworkView AND a DRenderingEngine for it.
		CyNetworkView netView = delegateFactory.createNetworkView(network);
		
		DRenderingEngine re = new DRenderingEngine(netView, dingLexicon, annMgr, dingGraphLOD, handleFactory, registrar);
		netView.addNetworkViewListener(re);
		
		mainRenderingEngines.put(netView, re);
		
		return netView;
	}
	
	public DRenderingEngine getRenderingEngine(CyNetworkView networkView) {
		if(networkView == null)
			return null;
		return mainRenderingEngines.get(networkView);
	}

	public void removeRenderingEngine(CyNetworkView networkView) {
		if(networkView == null)
			return;
		mainRenderingEngines.remove(networkView);
	}
	
	@Override
	public void handleEvent(NetworkViewAboutToBeDestroyedEvent e) {
		removeRenderingEngine(e.getNetworkView());
	}
	
}
