package org.cytoscape.io.internal.write.json.serializer;

import org.cytoscape.application.CyVersion;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;


/**
 * ObjectMpper for Cytoscape.js networks and tables
 * 
 */
public class CytoscapeJsNetworkModule extends SimpleModule {

	private static final long serialVersionUID = -3553426112109820245L;
	
	static final String FORMAT_VERSION_TAG = "format_version";
	static final String FORMAT_VERSION = "1.0";
	static final String GENERATED_BY_TAG = "generated_by";
	static final String TARGET_CYJS_VERSION_TAG = "target_cytoscapejs_version";
	static final String CYTOSCAPEJS_VERSION = "2.0.4";

	public CytoscapeJsNetworkModule(final CyVersion version) {
		super("CytoscapeJsModule", new Version(1, 0, 0, null, null, null));
		addSerializer(new CytoscapeJsViewSerializer(version));
		addSerializer(new CytoscapeJsNetworkSerializer(version));
		addSerializer(new JsRowSerializer());
		addSerializer(new JsNodeViewSerializer());
	}
}