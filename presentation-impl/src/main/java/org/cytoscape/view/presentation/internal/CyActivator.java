
package org.cytoscape.view.presentation.internal;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.view.presentation.internal.RenderingEngineManagerImpl;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.osgi.framework.BundleContext;
import org.cytoscape.service.util.AbstractCyActivator;
import java.util.Properties;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		CyEventHelper cyEventHelperServiceRef = getService(bc,CyEventHelper.class);
		
		RenderingEngineManagerImpl renderingEngineManager = new RenderingEngineManagerImpl();
		
		Properties renderingEngineManagerProps = new Properties();
		renderingEngineManagerProps.setProperty("service.type","manager");
		registerService(bc,renderingEngineManager,RenderingEngineManager.class, renderingEngineManagerProps);

		registerServiceListener(bc,renderingEngineManager,"addRenderingEngineFactory","removeRenderingEngineFactory",RenderingEngineFactory.class);

	}
}

