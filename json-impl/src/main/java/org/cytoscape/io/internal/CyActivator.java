package org.cytoscape.io.internal;

import java.util.Properties;

import static org.cytoscape.work.ServiceProperties.ID;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.internal.read.json.CytoscapeJsNetworkReaderFactory;
import org.cytoscape.io.internal.write.json.JSONNetworkWriterFactory;
import org.cytoscape.io.internal.write.json.JSONVisualStyleWriterFactory;
import org.cytoscape.io.internal.write.json.serializer.CytoscapeJsNetworkModule;
import org.cytoscape.io.internal.write.json.serializer.D3jsModule;
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

		// Importing Services:
		StreamUtil streamUtil = getService(bc, StreamUtil.class);

		final CyNetworkViewFactory cyNetworkViewFactory = getService(bc, CyNetworkViewFactory.class);
		final CyNetworkFactory cyNetworkFactory = getService(bc, CyNetworkFactory.class);
		final CyApplicationManager applicationManager = getService(bc, CyApplicationManager.class);

		// ///////////////// Readers ////////////////////////////
		final BasicCyFileFilter cytoscapejsReaderFilter = new BasicCyFileFilter(new String[] { "cyjs" },
				new String[] { "application/json" }, "Cytoscape.js JSON format", DataCategory.NETWORK, streamUtil);
		final CytoscapeJsNetworkReaderFactory jsReaderFactory = new CytoscapeJsNetworkReaderFactory(
				cytoscapejsReaderFilter, cyNetworkViewFactory, cyNetworkFactory);

		final Properties cytoscapeJsNetworkReaderFactoryProps = new Properties();
		cytoscapeJsNetworkReaderFactoryProps.put(ID, "cytoscapeJsNetworkReaderFactory");
		registerService(bc, jsReaderFactory, InputStreamTaskFactory.class, cytoscapeJsNetworkReaderFactoryProps);

		// ///////////////// Writers ////////////////////////////
		ObjectMapper cytoscapeJsMapper = new ObjectMapper();
		cytoscapeJsMapper.registerModule(new CytoscapeJsNetworkModule());

		final ObjectMapper d3jsMapper = new ObjectMapper();
		d3jsMapper.registerModule(new D3jsModule());

		final BasicCyFileFilter cytoscapejsFilter = new BasicCyFileFilter(new String[] { "cyjs" },
				new String[] { "application/json" }, "Cytoscape.js JSON format", DataCategory.NETWORK, streamUtil);
		final BasicCyFileFilter d3jsFilter = new BasicCyFileFilter(new String[] { "d3" },
				new String[] { "application/json" }, "D3.js JSON format", DataCategory.NETWORK, streamUtil);

		final BasicCyFileFilter vizmapJsonFilter = new BasicCyFileFilter(new String[] { "json" },
				new String[] { "application/json" }, "Cytoscape.js Visual Style JSON format", DataCategory.VIZMAP,
				streamUtil);

		// For Cytoscape.js
		final JSONNetworkWriterFactory cytoscapeJsWriterFactory = new JSONNetworkWriterFactory(cytoscapejsFilter,
				cytoscapeJsMapper);
		registerAllServices(bc, cytoscapeJsWriterFactory, new Properties());

		// For D3.js Force layout
		final JSONNetworkWriterFactory d3jsWriterFactory = new JSONNetworkWriterFactory(d3jsFilter, d3jsMapper);
		registerAllServices(bc, d3jsWriterFactory, new Properties());

		final JSONVisualStyleWriterFactory jsonVSWriterFactory = new JSONVisualStyleWriterFactory(vizmapJsonFilter,
				applicationManager);
		registerAllServices(bc, jsonVSWriterFactory, new Properties());
	}
}