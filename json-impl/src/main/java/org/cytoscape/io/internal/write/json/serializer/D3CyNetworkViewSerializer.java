package org.cytoscape.io.internal.write.json.serializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class D3CyNetworkViewSerializer extends JsonSerializer<CyNetworkView> {

	@Override
	public void serialize(final CyNetworkView networkView, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {

		final CyNetwork network = networkView.getModel();

		// Write array
		final List<CyNode> nodes = network.getNodeList();
		final List<CyEdge> edges = network.getEdgeList();

		final Map<CyNode, Long> node2Index = new HashMap<CyNode, Long>();

		jgen.useDefaultPrettyPrinter();

		jgen.writeStartObject();

		long index = 0;
		jgen.writeArrayFieldStart(CyJsonToken.NODES.getName());
		for (final CyNode node : nodes) {
			jgen.writeStartObject();

			jgen.writeStringField(CyJsonToken.ID.getName(), node.getSUID().toString());

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

			jgen.writeStringField(CyJsonToken.ID.getName(), edge.getSUID().toString());
			jgen.writeNumberField(CyJsonToken.SOURCE.getName(), node2Index.get(edge.getSource()));
			jgen.writeNumberField(CyJsonToken.TARGET.getName(), node2Index.get(edge.getTarget()));

			// Write CyRow in "data" field
			jgen.writeObject(network.getRow(edge));

			jgen.writeEndObject();

		}
		jgen.writeEndArray();

		jgen.writeEndObject();
	}

	@Override
	public Class<CyNetworkView> handledType() {
		return CyNetworkView.class;
	}
}
