package org.cytoscape.internal;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		
		CyApplicationManager appManager = getService(bc, CyApplicationManager.class);
		
		RenderingEngineManager renderManager = getService(bc, RenderingEngineManager.class);
		
		RenderingEngineFactory renderFactory = getService(bc,
                RenderingEngineFactory.class,
                "(id=ding)");
		
		VisualMappingManager vmanager = getService(bc, VisualMappingManager.class);
		
		NetworkAddedSetCurrent myListener = new NetworkAddedSetCurrent(appManager, renderManager, renderFactory, vmanager);
		
		registerService(bc, myListener, NetworkViewAddedListener.class, new Properties());
	}
	
}
