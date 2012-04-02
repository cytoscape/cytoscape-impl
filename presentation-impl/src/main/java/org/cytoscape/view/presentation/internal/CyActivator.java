package org.cytoscape.view.presentation.internal;

import java.util.Properties;

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		RenderingEngineManagerImpl renderingEngineManager = new RenderingEngineManagerImpl();

		Properties renderingEngineManagerProps = new Properties();
		renderingEngineManagerProps.setProperty("service.type", "manager");
		registerService(bc, renderingEngineManager, RenderingEngineManager.class, renderingEngineManagerProps);

		registerServiceListener(bc, renderingEngineManager, "addRenderingEngineFactory",
				"removeRenderingEngineFactory", RenderingEngineFactory.class);
	}
}
