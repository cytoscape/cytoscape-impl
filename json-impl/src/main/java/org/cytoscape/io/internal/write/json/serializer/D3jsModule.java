package org.cytoscape.io.internal.write.json.serializer;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class D3jsModule extends SimpleModule {
	
	private static final long serialVersionUID = 7684074670405628381L;

	public D3jsModule() {
		super("D3jsModule", new Version(1, 0, 0, null, null, null));
		addSerializer(new D3CyNetworkViewSerializer());
		addSerializer(new JsRowSerializer());
	}
}
