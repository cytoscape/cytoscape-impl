package org.cytoscape.io.internal.write.json.serializer;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class CytoscapejsModule extends SimpleModule {

	private static final long serialVersionUID = -3553426112109820245L;

	public CytoscapejsModule() {
		super("CytoscapejsModule", new Version(1, 0, 0, null, null, null));
		addSerializer(new JsCyNetworkSerializer());
		addSerializer(new JsRowSerializer());
		addSerializer(new JsCyNetworkViewSerializer());
		addSerializer(new JsNodeViewSerializer());
		
		// For Visual Style
		addSerializer(new JsVisualStyleSerializer());
		addSerializer(new JsVsiaulStyleSetSerializer());
	}
}