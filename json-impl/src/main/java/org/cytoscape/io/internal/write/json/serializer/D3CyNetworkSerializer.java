package org.cytoscape.io.internal.write.json.serializer;

import java.io.IOException;

import org.cytoscape.model.CyNetwork;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class D3CyNetworkSerializer extends JsonSerializer<CyNetwork> {

	@Override
	public void serialize(final CyNetwork network, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		final D3JsonBuilder builder = new D3JsonBuilder();
		builder.serializeNetwork(network, jgen, provider);
	}

	@Override
	public Class<CyNetwork> handledType() {
		return CyNetwork.class;
	}

}