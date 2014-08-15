package org.cytoscape.io.internal;

import static org.cytoscape.work.ServiceProperties.ID;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyVersion;
import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.internal.read.json.CytoscapeJsNetworkReaderFactory;
import org.cytoscape.io.internal.write.json.CytoscapeJsVisualStyleWriterFactory;
import org.cytoscape.io.internal.write.json.CytoscapeJsNetworkWriterFactory;
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
		final CyVersion cyVersion = getService(bc, CyVersion.class);
		final StreamUtil streamUtil = getService(bc, StreamUtil.class);
		final CyNetworkViewFactory cyNetworkViewFactory = getService(bc, CyNetworkViewFactory.class);
		final CyNetworkFactory cyNetworkFactory = getService(bc, CyNetworkFactory.class);
		final CyApplicationManager applicationManager = getService(bc, CyApplicationManager.class);
		final CyNetworkManager cyNetworkManager = getService(bc, CyNetworkManager.class);
		final CyRootNetworkManager cyRootNetworkManager = getService(bc, CyRootNetworkManager.class);
		
		
		// ///////////////// Readers ////////////////////////////
		final BasicCyFileFilter cytoscapejsReaderFilter = new BasicCyFileFilter(new String[] { "cyjs", "json" },
				new String[] { "application/json" }, "Cytoscape.js JSON", DataCategory.NETWORK, streamUtil);
		final CytoscapeJsNetworkReaderFactory jsReaderFactory = new CytoscapeJsNetworkReaderFactory(
				cytoscapejsReaderFilter, cyNetworkViewFactory, cyNetworkFactory, cyNetworkManager, cyRootNetworkManager);
		final Properties cytoscapeJsNetworkReaderFactoryProps = new Properties();

		// This is the unique identifier for this reader.  3rd party developer can use this service by using this ID.
		cytoscapeJsNetworkReaderFactoryProps.put(ID, "cytoscapejsNetworkReaderFactory");
		registerService(bc, jsReaderFactory, InputStreamTaskFactory.class, cytoscapeJsNetworkReaderFactoryProps);


		// ///////////////// Writers ////////////////////////////
		final ObjectMapper cytoscapeJsMapper = new ObjectMapper();
		cytoscapeJsMapper.registerModule(new CytoscapeJsNetworkModule(cyVersion));

		final BasicCyFileFilter cytoscapejsFilter = new BasicCyFileFilter(new String[] { "cyjs" },
				new String[] { "application/json" }, "Cytoscape.js JSON", DataCategory.NETWORK, streamUtil);
		final BasicCyFileFilter vizmapJsonFilter = new BasicCyFileFilter(new String[] { "json" },
				new String[] { "application/json" }, "Style for cytoscape.js", DataCategory.VIZMAP, streamUtil);

		// For Cytoscape.js
		final CytoscapeJsNetworkWriterFactory cytoscapejsWriterFactory = new CytoscapeJsNetworkWriterFactory(cytoscapejsFilter, cytoscapeJsMapper);
		
		// Use this ID to get this service in other bundles.
		final Properties jsWriterFactoryProperties = new Properties();
		jsWriterFactoryProperties.put(ID, "cytoscapejsNetworkWriterFactory");
		registerAllServices(bc, cytoscapejsWriterFactory, jsWriterFactoryProperties);

		// For Visual Style
		final CytoscapeJsVisualStyleWriterFactory jsonVSWriterFactory = new CytoscapeJsVisualStyleWriterFactory(vizmapJsonFilter, applicationManager, cyVersion);
		
		// Use this ID to get this service in other bundles.
		final Properties jsVisualStyleWriterFactoryProperties = new Properties();
		jsWriterFactoryProperties.put(ID, "cytoscapejsVisualStyleWriterFactory");
		registerAllServices(bc, jsonVSWriterFactory, jsVisualStyleWriterFactoryProperties);
	}
}