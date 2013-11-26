package org.cytoscape.io.internal;

import static org.cytoscape.work.ServiceProperties.ID;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.internal.read.json.CytoscapeJsNetworkReaderFactory;
import org.cytoscape.io.internal.write.json.CytoscapeJsVisualStyleWriterFactory;
import org.cytoscape.io.internal.write.json.JSONNetworkWriterFactory;
import org.cytoscape.io.internal.write.json.serializer.CytoscapeJsNetworkModule;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.osgi.framework.BundleContext;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Activator for JSON support module.
 * 
 * 
 */
public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		
		// Importing Services
		final StreamUtil streamUtil = getService(bc, StreamUtil.class);
		final CyNetworkViewFactory cyNetworkViewFactory = getService(bc, CyNetworkViewFactory.class);
		final CyNetworkFactory cyNetworkFactory = getService(bc, CyNetworkFactory.class);
		final CyApplicationManager applicationManager = getService(bc, CyApplicationManager.class);
		final CyNetworkManager cyNetworkManager = getService(bc, CyNetworkManager.class);
		final CyRootNetworkManager cyRootNetworkManager = getService(bc, CyRootNetworkManager.class);

		// ///////////////// Readers ////////////////////////////
		final BasicCyFileFilter cytoscapejsReaderFilter = new BasicCyFileFilter(new String[] { "cyjs" },
				new String[] { "application/json" }, "Cytoscape.js JSON", DataCategory.NETWORK, streamUtil);
		final CytoscapeJsNetworkReaderFactory jsReaderFactory = new CytoscapeJsNetworkReaderFactory(
				cytoscapejsReaderFilter, cyNetworkViewFactory, cyNetworkFactory, cyNetworkManager, cyRootNetworkManager);
		final Properties cytoscapeJsNetworkReaderFactoryProps = new Properties();

		cytoscapeJsNetworkReaderFactoryProps.put(ID, "cytoscapeJsNetworkReaderFactory");
		registerService(bc, jsReaderFactory, InputStreamTaskFactory.class, cytoscapeJsNetworkReaderFactoryProps);


		// ///////////////// Writers ////////////////////////////
		final ObjectMapper cytoscapeJsMapper = new ObjectMapper();
		cytoscapeJsMapper.registerModule(new CytoscapeJsNetworkModule());

		final BasicCyFileFilter cytoscapejsFilter = new BasicCyFileFilter(new String[] { "cyjs" },
				new String[] { "application/json" }, "Cytoscape.js JSON", DataCategory.NETWORK, streamUtil);
		final BasicCyFileFilter vizmapJsonFilter = new BasicCyFileFilter(new String[] { "json" },
				new String[] { "application/json" }, "Style for cytoscape.js", DataCategory.VIZMAP, streamUtil);

		// For Cytoscape.js
		final JSONNetworkWriterFactory cytoscapejsWriterFactory = new JSONNetworkWriterFactory(cytoscapejsFilter, cytoscapeJsMapper);
		final Properties jsWriterFactoryProperties = new Properties();
		jsWriterFactoryProperties.put(ID, "cytoscapejsWriterFactory");
		registerAllServices(bc, cytoscapejsWriterFactory, jsWriterFactoryProperties);


		// For Visual Style
		final CytoscapeJsVisualStyleWriterFactory jsonVSWriterFactory = new CytoscapeJsVisualStyleWriterFactory(vizmapJsonFilter, applicationManager);
		final Properties jsVisualStyleWriterFactoryProperties = new Properties();
		jsWriterFactoryProperties.put(ID, "cytoscapeJsVisualStyleWriterFactory");
		registerAllServices(bc, jsonVSWriterFactory, jsVisualStyleWriterFactoryProperties);
	}
}