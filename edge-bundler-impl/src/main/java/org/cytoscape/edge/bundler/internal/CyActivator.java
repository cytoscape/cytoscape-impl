package org.cytoscape.edge.bundler.internal;

import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.service.util.AbstractCyActivator;
import java.util.Properties;
import org.osgi.framework.BundleContext;

import static org.cytoscape.work.ServiceProperties.*;


public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		HandleFactory hf = getService(bc, HandleFactory.class);
		BendFactory bf = getService(bc, BendFactory.class);
		VisualMappingManager vmm = getService(bc, VisualMappingManager.class);
		VisualMappingFunctionFactory discreteFactory = getService(bc,VisualMappingFunctionFactory.class,"(mapping.type=discrete)");
		
		EdgeBundlerTaskFactory edgeBundlerTaskFactory = new EdgeBundlerTaskFactory(hf, bf, vmm, discreteFactory, 0);
		Properties edgeBundlerTaskFactoryProps = new Properties();
		edgeBundlerTaskFactoryProps.setProperty(PREFERRED_MENU,"Layout.Bundle Edges");
		edgeBundlerTaskFactoryProps.setProperty(MENU_GRAVITY,"11.0");
		edgeBundlerTaskFactoryProps.setProperty(TITLE,"All Nodes and Edges");
		registerService(bc,edgeBundlerTaskFactory,NetworkViewTaskFactory.class, edgeBundlerTaskFactoryProps);
		
		
		edgeBundlerTaskFactory = new EdgeBundlerTaskFactory(hf, bf, vmm, discreteFactory, 1);
		edgeBundlerTaskFactoryProps = new Properties();
		edgeBundlerTaskFactoryProps.setProperty(PREFERRED_MENU,"Layout.Bundle Edges");
		edgeBundlerTaskFactoryProps.setProperty(MENU_GRAVITY,"12.0");
		edgeBundlerTaskFactoryProps.setProperty(TITLE,"Selected Nodes Only");
		registerService(bc,edgeBundlerTaskFactory,NetworkViewTaskFactory.class, edgeBundlerTaskFactoryProps);
		
		
		edgeBundlerTaskFactory = new EdgeBundlerTaskFactory(hf, bf, vmm, discreteFactory, 2);
		edgeBundlerTaskFactoryProps = new Properties();
		edgeBundlerTaskFactoryProps.setProperty(PREFERRED_MENU,"Layout.Bundle Edges");
		edgeBundlerTaskFactoryProps.setProperty(MENU_GRAVITY,"13.0");
		edgeBundlerTaskFactoryProps.setProperty(TITLE,"Selected Edges Only");
		registerService(bc,edgeBundlerTaskFactory,NetworkViewTaskFactory.class, edgeBundlerTaskFactoryProps);
	}
}

