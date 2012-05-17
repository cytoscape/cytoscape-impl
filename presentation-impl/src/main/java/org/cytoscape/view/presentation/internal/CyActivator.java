package org.cytoscape.view.presentation.internal;

import java.util.Properties;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		
		final CyEventHelper eventHelper = getService(bc, CyEventHelper.class);
		RenderingEngineManagerImpl renderingEngineManager = new RenderingEngineManagerImpl(eventHelper);

		Properties renderingEngineManagerProps = new Properties();
		renderingEngineManagerProps.setProperty("service.type", "manager");
		registerService(bc, renderingEngineManager, RenderingEngineManager.class, renderingEngineManagerProps);

		registerServiceListener(bc, renderingEngineManager, "addRenderingEngineFactory",
				"removeRenderingEngineFactory", RenderingEngineFactory.class);
	}
}
