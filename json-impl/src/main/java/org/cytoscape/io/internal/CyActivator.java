package org.cytoscape.io.internal;

//import org.cytoscape.rest.internal.net.server.CytoBridgePostResponder;
import java.util.Properties;

import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.internal.write.json.JSONNetworkWriterFactory;
import org.cytoscape.io.internal.write.json.JSONVisualStyleWriterFactory;
import org.cytoscape.io.internal.write.json.serializer.CytoscapejsModule;
import org.cytoscape.io.internal.write.json.serializer.D3TreeModule;
import org.cytoscape.io.internal.write.json.serializer.D3jsModule;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.service.util.AbstractCyActivator;
import org.osgi.framework.BundleContext;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		// Importing Services:
		StreamUtil streamUtil = getService(bc, StreamUtil.class);

		// ///////////////// Writers ////////////////////////////
		final ObjectMapper jsMapper = new ObjectMapper();
		jsMapper.registerModule(new CytoscapejsModule());

		final ObjectMapper graphsonMapper = new ObjectMapper();
		graphsonMapper.registerModule(new CytoscapejsModule());

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

		final JSONNetworkWriterFactory jsonWriterFactory = new JSONNetworkWriterFactory(cytoscapejsFilter, jsMapper);
		registerAllServices(bc, jsonWriterFactory, new Properties());
		
		final JSONNetworkWriterFactory d3jsWriterFactory = new JSONNetworkWriterFactory(d3jsFilter, d3jsMapper);
		registerAllServices(bc, d3jsWriterFactory, new Properties());
		
		final JSONNetworkWriterFactory d3jsTreeWriterFactory = new JSONNetworkWriterFactory(d3jsTreeFilter, d3jsTreeMapper);
		registerAllServices(bc, d3jsTreeWriterFactory, new Properties());
		
		final JSONVisualStyleWriterFactory jsonVSWriterFactory = new JSONVisualStyleWriterFactory(vizmapJsonFilter,
				jsMapper);
		registerAllServices(bc, jsonVSWriterFactory, new Properties());

		// final JSONNetworkWriterFactory cytoscapeJsonWriterFactory = new
		// JSONNetworkWriterFactory(fullJsonFilter, fullJsonMapper);
		// registerAllServices(bc, cytoscapeJsonWriterFactory, new
		// Properties());

	}
}