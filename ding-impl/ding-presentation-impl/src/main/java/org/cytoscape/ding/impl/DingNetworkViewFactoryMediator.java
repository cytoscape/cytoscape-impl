package org.cytoscape.ding.impl;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.impl.cyannotator.AnnotationFactoryManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.presentation.property.values.HandleFactory;

public class DingNetworkViewFactoryMediator implements CyNetworkViewFactory, NetworkViewAboutToBeDestroyedListener {

	private final CyNetworkViewFactory delegateFactory;
	private final Map<CyNetworkView, DRenderingEngine> mainRenderingEngines = new HashMap<>();

	private final DVisualLexicon dingLexicon;
	private final AnnotationFactoryManager annMgr;
	private final CyServiceRegistrar registrar;
	private final DingGraphLOD dingGraphLOD;
	private final HandleFactory handleFactory; 
	
	public DingNetworkViewFactoryMediator(
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

	@Override
	public CyNetworkView createNetworkView(CyNetwork network) {
		CyNetworkView networkView = delegateFactory.createNetworkView(network);
		
		DRenderingEngine re = createRenderingEngine(networkView);

		// MKTODO Do we still need to do this???
		networkView.addNetworkViewListener(re);
		mainRenderingEngines.put(networkView, re);
		
		return networkView;
	}
	
	private DRenderingEngine createRenderingEngine(CyNetworkView networkView) {
		DRenderingEngine re = new DRenderingEngine(networkView, dingLexicon, annMgr, dingGraphLOD, handleFactory, registrar);
		return re;
	}
	
	public DRenderingEngine getRenderingEngine(CyNetworkView networkView) {
		return mainRenderingEngines.get(networkView);
	}

	@Override
	public void handleEvent(NetworkViewAboutToBeDestroyedEvent e) {
		mainRenderingEngines.remove(e.getNetworkView());
	}
	
}
