package org.cytoscape.io.internal.write.json.serializer;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class D3TreeModule extends SimpleModule {
	
	public D3TreeModule() {
		super("D3TreeModule", new Version(1, 0, 0, null, null, null));
		addSerializer(new D3CyNetworkViewTreeSerializer());
		addSerializer(new JsRowSerializer());
	}

}
