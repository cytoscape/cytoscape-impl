package org.cytoscape.view.vizmap.internal;

import java.util.Properties;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.internal.mappings.ContinuousMappingFactory;
import org.cytoscape.view.vizmap.internal.mappings.DiscreteMappingFactory;
import org.cytoscape.view.vizmap.internal.mappings.PassthroughMappingFactory;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		final CyServiceRegistrar serviceRegistrarServiceRef = getService(bc, CyServiceRegistrar.class);
		CyEventHelper cyEventHelperServiceRef = getService(bc, CyEventHelper.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc, CyNetworkManager.class);

		VisualLexiconManager visualLexiconManager = new VisualLexiconManager();
		VisualStyleFactoryImpl visualStyleFactory = new VisualStyleFactoryImpl(visualLexiconManager,
				serviceRegistrarServiceRef, cyNetworkManagerServiceRef);
		VisualMappingManagerImpl visualMappingManager = new VisualMappingManagerImpl(cyEventHelperServiceRef,
				visualStyleFactory, visualLexiconManager);
		DiscreteMappingFactory discreteMappingFactory = new DiscreteMappingFactory();
		ContinuousMappingFactory continuousMappingFactory = new ContinuousMappingFactory();
		PassthroughMappingFactory passthroughMappingFactory = new PassthroughMappingFactory();

		registerService(bc, visualMappingManager, VisualMappingManager.class, new Properties());
		registerService(bc, visualStyleFactory, VisualStyleFactory.class, new Properties());

		Properties discreteMappingFactoryProps = new Properties();
		discreteMappingFactoryProps.setProperty("service.type", "factory");
		discreteMappingFactoryProps.setProperty("mapping.type", "discrete");
		registerService(bc, discreteMappingFactory, VisualMappingFunctionFactory.class, discreteMappingFactoryProps);

		Properties continuousMappingFactoryProps = new Properties();
		continuousMappingFactoryProps.setProperty("service.type", "factory");
		continuousMappingFactoryProps.setProperty("mapping.type", "continuous");
		registerService(bc, continuousMappingFactory, VisualMappingFunctionFactory.class, continuousMappingFactoryProps);

		Properties passthroughMappingFactoryProps = new Properties();
		passthroughMappingFactoryProps.setProperty("service.type", "factory");
		passthroughMappingFactoryProps.setProperty("mapping.type", "passthrough");
		registerService(bc, passthroughMappingFactory, VisualMappingFunctionFactory.class,
				passthroughMappingFactoryProps);

		registerServiceListener(bc, visualLexiconManager, "addRenderingEngineFactory", "removeRenderingEngineFactory",
				RenderingEngineFactory.class);
	}
}
