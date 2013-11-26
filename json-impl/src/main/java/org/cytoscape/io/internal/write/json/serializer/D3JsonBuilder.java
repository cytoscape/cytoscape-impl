package org.cytoscape.io.internal.write.json.serializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;

import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.*;


public class D3JsonBuilder {
	
	protected final void serializeNetwork(final CyNetwork network, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {

		// Write array
		final List<CyNode> nodes = network.getNodeList();
		final List<CyEdge> edges = network.getEdgeList();

		final Map<CyNode, Long> node2Index = new HashMap<CyNode, Long>();

		jgen.useDefaultPrettyPrinter();

		jgen.writeStartObject();

		long index = 0;
		jgen.writeArrayFieldStart(NODES.getTag());
		for (final CyNode node : nodes) {
			jgen.writeStartObject();

			jgen.writeStringField(ID.getTag(), node.getSUID().toString());

			// Write CyRow in "data" field
			jgen.writeObject(network.getRow(node));

			jgen.writeEndObject();
			node2Index.put(node, index);
			index++;
		}
		jgen.writeEndArray();

		jgen.writeArrayFieldStart("links");
		for (final CyEdge edge : edges) {
			jgen.writeStartObject();

			jgen.writeStringField(ID.getTag(), edge.getSUID().toString());
			jgen.writeNumberField(SOURCE.getTag(), node2Index.get(edge.getSource()));
			jgen.writeNumberField(TARGET.getTag(), node2Index.get(edge.getTarget()));

			// Write CyRow in "data" field
			jgen.writeObject(network.getRow(edge));

			jgen.writeEndObject();

		}
		jgen.writeEndArray();

		jgen.writeEndObject();
	}
}