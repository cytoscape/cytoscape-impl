package org.cytoscape.io.internal.write.json.serializer;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;


/**
 * ObjectMpper for Cytoscape.js networks and tables
 * 
 */
public class CytoscapeJsModule extends SimpleModule {

	private static final long serialVersionUID = -3553426112109820245L;

	public CytoscapeJsModule() {
		super("CytoscapeJsModule", new Version(1, 0, 0, null, null, null));
		addSerializer(new CytoscapeJsNetworkSerializer());
		addSerializer(new JsRowSerializer());
		addSerializer(new JsNodeViewSerializer());
		addSerializer(new CytoscapeJsViewSerializer());
	}
}