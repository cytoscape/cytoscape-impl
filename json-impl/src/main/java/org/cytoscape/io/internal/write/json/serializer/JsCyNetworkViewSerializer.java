package org.cytoscape.io.internal.write.json.serializer;

import java.io.IOException;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class JsCyNetworkViewSerializer extends JsonSerializer<CyNetworkView> {

	@Override
	public void serialize(CyNetworkView networkView, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		jgen.useDefaultPrettyPrinter();

		jgen.writeStartObject();
		jgen.writeObjectFieldStart(CyJsonToken.ELEMENTS.getName());

		// Write array
		final List<CyNode> nodes = networkView.getModel().getNodeList();
		final List<CyEdge> edges = networkView.getModel().getEdgeList();

		final CyNetwork network = networkView.getModel();

		jgen.writeArrayFieldStart(CyJsonToken.NODES.getName());
		for (final CyNode node : nodes) {
			jgen.writeStartObject();

			// Data field
			jgen.writeObjectFieldStart(CyJsonToken.DATA.getName());
			jgen.writeStringField(CyJsonToken.ID.getName(), node.getSUID().toString());
			// Write CyRow in "data" field
			jgen.writeObject(networkView.getModel().getRow(node));
			jgen.writeEndObject();

			// Position and other visual props
			jgen.writeObject(networkView.getNodeView(node));

			// Special case for cytoscape.js format:
			// - Selected
			jgen.writeBooleanField(CyNetwork.SELECTED, network.getRow(node).get(CyNetwork.SELECTED, Boolean.class));

			jgen.writeEndObject();
		}
		jgen.writeEndArray();

		jgen.writeArrayFieldStart(CyJsonToken.EDGES.getName());
		for (final CyEdge edge : edges) {
			jgen.writeStartObject();

			jgen.writeObjectFieldStart(CyJsonToken.DATA.getName());
			jgen.writeStringField(CyJsonToken.ID.getName(), edge.getSUID().toString());
			jgen.writeStringField(CyJsonToken.SOURCE.getName(), edge.getSource().getSUID().toString());
			jgen.writeStringField(CyJsonToken.TARGET.getName(), edge.getTarget().getSUID().toString());

			// Write CyRow in "data" field
			jgen.writeObject(networkView.getModel().getRow(edge));

			jgen.writeEndObject();

			// Special case for cytoscape.js format:
			// - Selected
			jgen.writeBooleanField(CyNetwork.SELECTED, network.getRow(edge).get(CyNetwork.SELECTED, Boolean.class));

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
