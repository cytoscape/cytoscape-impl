package org.cytoscape.io.internal;

import java.util.Properties;

import org.cytoscape.io.DataCategory;
import org.cytoscape.io.internal.read.graphml.GraphMLFileFilter;
import org.cytoscape.io.internal.read.graphml.GraphMLReaderFactory;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.subnetwork.CyRootNetworkFactory;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.osgi.framework.BundleContext;

/**
 * Configurator/Activator of this bundle.
 *
 */
public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		// Import required Services
		StreamUtil streamUtilRef = getService(bc, StreamUtil.class);
		CyLayoutAlgorithmManager cyLayoutsServiceRef = getService(bc, CyLayoutAlgorithmManager.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc, CyNetworkFactory.class);
		CyNetworkViewFactory cyNetworkViewFactoryServiceRef = getService(bc, CyNetworkViewFactory.class);
		CyRootNetworkFactory cyRootNetworkFactoryServiceRef = getService(bc, CyRootNetworkFactory.class);

		GraphMLFileFilter graphMLFilter = new GraphMLFileFilter(new String[] { "graphml", "xml" }, new String[] {
				"text/graphml", "text/graphml+xml" }, "GraphML files", DataCategory.NETWORK, streamUtilRef);
		GraphMLReaderFactory graphMLReaderFactory = new GraphMLReaderFactory(graphMLFilter, cyLayoutsServiceRef,
				cyNetworkViewFactoryServiceRef, cyNetworkFactoryServiceRef, cyRootNetworkFactoryServiceRef);

		registerService(bc, graphMLReaderFactory, InputStreamTaskFactory.class, new Properties());		
	}
}