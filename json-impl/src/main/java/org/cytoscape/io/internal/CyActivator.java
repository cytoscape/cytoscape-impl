package org.cytoscape.io.internal;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.internal.read.json.CytoscapejsFileFilter;
import org.cytoscape.io.internal.write.json.JSONNetworkWriterFactory;
import org.cytoscape.io.internal.write.json.JSONVisualStyleWriterFactory;
import org.cytoscape.io.internal.write.json.serializer.CytoscapeJsModule;
import org.cytoscape.io.internal.write.json.serializer.D3TreeModule;
import org.cytoscape.io.internal.write.json.serializer.D3jsModule;
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
		final CyNetworkManager cyNetworkManager = getService(bc, CyNetworkManager.class);
		final CyRootNetworkManager cyRootNetworkManager = getService(bc, CyRootNetworkManager.class);
		final CyApplicationManager applicationManager = getService(bc, CyApplicationManager.class);

		// ///////////////// Readers ////////////////////////////

		final CyFileFilter cytoscapejsReaderFilter = new CytoscapejsFileFilter(new String[] { "json" },
				new String[] { "application/json" }, "Cytoscape.js JSON format", DataCategory.NETWORK, streamUtil);

		// ///////////////// Writers ////////////////////////////

		ObjectMapper cytoscapeJsMapper = new ObjectMapper();
		cytoscapeJsMapper.registerModule(new CytoscapeJsModule());

		final ObjectMapper d3jsMapper = new ObjectMapper();
		d3jsMapper.registerModule(new D3jsModule());

		final ObjectMapper d3jsTreeMapper = new ObjectMapper();
		d3jsTreeMapper.registerModule(new D3TreeModule());

		final BasicCyFileFilter cytoscapejsFilter = new BasicCyFileFilter(new String[] { "json" },
				new String[] { "application/json" }, "Cytoscape.js JSON format", DataCategory.NETWORK, streamUtil);
		final BasicCyFileFilter d3jsFilter = new BasicCyFileFilter(new String[] { "json" },
				new String[] { "application/json" }, "D3.js JSON format", DataCategory.NETWORK, streamUtil);
		final BasicCyFileFilter d3jsTreeFilter = new BasicCyFileFilter(new String[] { "json" },
				new String[] { "application/json" }, "D3.js Tree JSON files", DataCategory.NETWORK, streamUtil);

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

		final JSONNetworkWriterFactory d3jsTreeWriterFactory = new JSONNetworkWriterFactory(d3jsTreeFilter,
				d3jsTreeMapper);
		registerAllServices(bc, d3jsTreeWriterFactory, new Properties());

		final JSONVisualStyleWriterFactory jsonVSWriterFactory = new JSONVisualStyleWriterFactory(vizmapJsonFilter,
				applicationManager);
		registerAllServices(bc, jsonVSWriterFactory, new Properties());

		// final JSONNetworkWriterFactory cytoscapeJsonWriterFactory = new
		// JSONNetworkWriterFactory(fullJsonFilter, fullJsonMapper);
		// registerAllServices(bc, cytoscapeJsonWriterFactory, new
		// Properties());

	}
}