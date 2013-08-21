package org.cytoscape.io.internal.write.json.serializer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Module for Jackson to hold the custom serializers for CyNetworks, CyNodes,
 * CyEdges, and CyTables.
 */
public class CyJacksonModule extends SimpleModule {

	private static final long serialVersionUID = -2618300407121070693L;

	/**
	 * Construct this CyJacksonModule and add all of the defined serializers.
	 */
	public CyJacksonModule() {
		// TODO: provide correct artifact information.
		super("CyJacksonModule", new Version(1, 0, 0, null, null, null));
		addSerializer(new CyNodeSerializer());
		addSerializer(new CyEdgeSerializer());
		addSerializer(new CyRowSerializer());
		addSerializer(new CyTableSerializer());
		addSerializer(new CyNetworkSerializer());
	}

	private static final void writeNodeInfo(CyNode node, JsonGenerator jgen) throws JsonGenerationException,
			IOException {
		if (node.getNetworkPointer() != null)
			jgen.writeNumberField("nestedNetwork", node.getNetworkPointer().getSUID());
	}

	private static final void writeEdgeInfo(CyEdge edge, JsonGenerator jgen) throws JsonGenerationException,
			IOException {
		jgen.writeNumberField("source", edge.getSource().getSUID());
		jgen.writeNumberField("target", edge.getTarget().getSUID());
		jgen.writeBooleanField("isDirected", edge.isDirected());
	}


	private final class CyNetworkSerializer extends JsonSerializer<CyNetwork> {

		@Override
		public void serialize(CyNetwork network, JsonGenerator jgen, SerializerProvider provider) throws IOException,
				JsonProcessingException {
			


			jgen.writeStartObject();
			jgen.writeObjectFieldStart(CyJsonToken.NETWORK.getName());

			// Network data table
			jgen.writeObject(network.getRow(network));

			// Write array
			final List<CyNode> nodes = network.getNodeList();
			final List<CyEdge> edges = network.getEdgeList();

			jgen.writeArrayFieldStart(CyJsonToken.NODES.getName());
			for (final CyNode node : nodes) {
				jgen.writeStartObject();
				writeNodeInfo(node, jgen);
				jgen.writeObject(network.getRow(node));
				jgen.writeEndObject();
			}
			jgen.writeEndArray();

			jgen.writeArrayFieldStart(CyJsonToken.EDGES.getName());
			for (final CyEdge edge : edges) {
				jgen.writeStartObject();
				writeEdgeInfo(edge, jgen);
				jgen.writeObject(network.getRow(edge));
				jgen.writeEndObject();
			}
			jgen.writeEndArray();

			jgen.writeEndObject();
		}

		public Class<CyNetwork> handledType() {
			return CyNetwork.class;
		}
	}

	/** Serializer for CyNodes. */
	private class CyNodeSerializer extends JsonSerializer<CyNode> {

		@Override
		public void serialize(CyNode node, JsonGenerator jgen, SerializerProvider provider) throws IOException,
				JsonProcessingException {

			jgen.writeStartObject();
			writeNodeInfo(node, jgen);
			jgen.writeEndObject();
		}

		public Class<CyNode> handledType() {
			return CyNode.class;
		}
	}

	/** Serializer for CyEdges. */
	private class CyEdgeSerializer extends JsonSerializer<CyEdge> {

		@Override
		public void serialize(CyEdge edge, JsonGenerator jgen, SerializerProvider provider) throws IOException,
				JsonProcessingException {

			jgen.writeStartObject();
			writeEdgeInfo(edge, jgen);
			jgen.writeEndObject();
		}

		@Override
		public Class<CyEdge> handledType() {
			return CyEdge.class;
		}
	}

	/** Serializer for CyTables. */
	private class CyTableSerializer extends JsonSerializer<CyTable> {

		@Override
		public void serialize(CyTable table, JsonGenerator jgen, SerializerProvider provider) throws IOException,
				JsonProcessingException {
			jgen.writeStartObject();
			jgen.writeEndObject();
		}

		public Class<CyTable> handledType() {
			return CyTable.class;
		}
	}

	private class CyRowSerializer extends JsonSerializer<CyRow> {

		@Override
		public void serialize(CyRow row, JsonGenerator jgen, SerializerProvider provider) throws IOException,
				JsonProcessingException {

			final CyTable table = row.getTable();
			final Map<String, Object> values = row.getAllValues();

			for (final String key : values.keySet()) {
				final Object value = values.get(key);
				if (value == null)
					continue;

				Class<?> type = table.getColumn(key).getType();
				if (type == List.class) {
					type = table.getColumn(key).getListElementType();
					writeList(type, key, (List<?>) value, jgen);
				} else {
					write(type, key, value, jgen);
				}
			}
		}

		private void writeList(final Class<?> type, String fieldName, List<?> values, JsonGenerator jgen)
				throws JsonGenerationException, IOException {

			jgen.writeFieldName(fieldName);
			jgen.writeStartArray();

			for (Object value : values)
				writeValue(type, value, jgen);
			
			jgen.writeEndArray();
		}

		private void write(final Class<?> type, String fieldName, Object value, JsonGenerator jgen)
				throws JsonGenerationException, IOException {
			jgen.writeFieldName(fieldName);
			writeValue(type, value, jgen);
		}

		private final void writeValue(final Class<?> type, Object value, JsonGenerator jgen)
				throws JsonGenerationException, IOException {
			jgen.writeStartObject();
			if (type == String.class) {
				jgen.writeStringField("type", "string");
				jgen.writeStringField("value", (String) value);
			} else if (type == Boolean.class) {
				jgen.writeStringField("type", "boolean");
				jgen.writeBooleanField("value", (Boolean) value);
			} else if (type == Double.class) {
				jgen.writeStringField("type", "double");
				jgen.writeNumberField("value", (Double) value);
			} else if (type == Integer.class) {
				jgen.writeStringField("type", "integer");
				jgen.writeNumberField("value", (Integer) value);
			} else if (type == Long.class) {
				jgen.writeStringField("type", "long");
				jgen.writeNumberField("value", (Long) value);
			} else if (type == Float.class) {
				// Handle float as double.
				jgen.writeStringField("type", "double");
				jgen.writeNumberField("value", (Float) value);
			}
			jgen.writeEndObject();
		}

		public Class<CyRow> handledType() {
			return CyRow.class;
		}
	}
	
	

}
