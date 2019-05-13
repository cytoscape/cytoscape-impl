package org.cytoscape.ding.impl;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.impl.cyannotator.AnnotationFactoryManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewConfig;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewFactoryFactory;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.presentation.property.values.HandleFactory;

public class DingNetworkViewFactory implements CyNetworkViewFactory, NetworkViewAboutToBeDestroyedListener {

	public static final Object ANIMATED_EDGES = new Object();
	
	
	private final CyNetworkViewFactory delegateFactory;
	private final Map<CyNetworkView, DRenderingEngine> mainRenderingEngines = new HashMap<>();

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

	public static CyNetworkViewConfig getNetworkViewConfig(CyNetworkViewFactoryFactory factoryFactory, DVisualLexicon dVisualLexicon) {
		CyNetworkViewConfig config = factoryFactory.createConfig(dVisualLexicon);
		config.addTrackedVisualProperty(ANIMATED_EDGES, DVisualLexicon.EDGE_LINE_TYPE, dVisualLexicon::isAnimated);
		return config;
	}
	
	@Override
	public CyNetworkView createNetworkView(CyNetwork network) {
		CyNetworkView netView = delegateFactory.createNetworkView(network);
		
		DRenderingEngine re = new DRenderingEngine(netView, dingLexicon, annMgr, dingGraphLOD, handleFactory, registrar);
		netView.addNetworkViewListener(re);
		
		mainRenderingEngines.put(netView, re);
		return netView;
	}
	
	public DRenderingEngine getRenderingEngine(CyNetworkView networkView) {
		return mainRenderingEngines.get(networkView);
	}

	public void removeRenderingEngine(CyNetworkView networkView) {
		mainRenderingEngines.remove(networkView);
	}
	
	@Override
	public void handleEvent(NetworkViewAboutToBeDestroyedEvent e) {
		mainRenderingEngines.remove(e.getNetworkView());
	}
	
}