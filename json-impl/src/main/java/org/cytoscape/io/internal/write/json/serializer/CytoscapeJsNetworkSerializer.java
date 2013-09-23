package org.cytoscape.io.internal.write.json.serializer;

import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.*;
import java.io.IOException;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Implementation to convert
 * 
 * 
 */
public class CytoscapeJsNetworkSerializer extends JsonSerializer<CyNetwork> {

	@Override
	public void serialize(CyNetwork network, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			JsonProcessingException {

		jgen.useDefaultPrettyPrinter();

		jgen.writeStartObject();
		jgen.writeObjectFieldStart(ELEMENTS.getTag());

		// Write array
		final List<CyNode> nodes = network.getNodeList();
		final List<CyEdge> edges = network.getEdgeList();

		jgen.writeArrayFieldStart(NODES.getTag());
		for (final CyNode node : nodes) {
			jgen.writeStartObject();

			// Data field
			jgen.writeObjectFieldStart(DATA.getTag());
			jgen.writeStringField(ID.getTag(), node.getSUID().toString());

			// Write CyRow in "data" field
			jgen.writeObject(network.getRow(node));
			jgen.writeEndObject();

			jgen.writeEndObject();
		}
		jgen.writeEndArray();

		jgen.writeArrayFieldStart(EDGES.getTag());
		for (final CyEdge edge : edges) {
			jgen.writeStartObject();

			jgen.writeObjectFieldStart(DATA.getTag());
			jgen.writeStringField(ID.getTag(), edge.getSUID().toString());
			jgen.writeStringField(SOURCE.getTag(), edge.getSource().getSUID().toString());
			jgen.writeStringField(TARGET.getTag(), edge.getTarget().getSUID().toString());

			// Write CyRow in "data" field
			jgen.writeObject(network.getRow(edge));

			jgen.writeEndObject();

			jgen.writeEndObject();

		}
		jgen.writeEndArray();

		jgen.writeEndObject();
	}

	@Override
	public Class<CyNetwork> handledType() {
		return CyNetwork.class;
	}
}
