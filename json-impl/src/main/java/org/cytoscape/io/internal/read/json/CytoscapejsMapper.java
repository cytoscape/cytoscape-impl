package org.cytoscape.io.internal.read.json;

import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Parse Cytoscape.js style map.
 * 
 */
public class CytoscapejsMapper implements JSONMapper {

	private final CyNetworkFactory factory;

	private Map<CyNode, Double[]> positionMap;

	public CytoscapejsMapper(final CyNetworkFactory factory) {
		this.factory = factory;
	}

	@Override
	public CyNetwork createNetwork(final JsonNode rootNode) {

		final JsonNode elements = rootNode.get(ELEMENTS.getTag());
		final JsonNode nodes = elements.get(NODES.getTag());
		final JsonNode edges = elements.get(EDGES.getTag());

		final CyNetwork network = factory.createNetwork();
		this.positionMap = new HashMap<CyNode, Double[]>();
		final Map<String, CyNode> nodeMap = this.addNodes(network, nodes);
		this.addEdges(network, edges, nodeMap);

		return network;
	}

	private final Map<String, CyNode> addNodes(final CyNetwork network, final JsonNode nodes) {

		final Map<String, CyNode> nodeMap = new HashMap<String, CyNode>();

		for (final JsonNode node : nodes) {

			// Extract node properties.
			final JsonNode data = node.get(DATA.getTag());
			final JsonNode nodeId = data.get(ID.getTag());
			CyNode cyNode = nodeMap.get(nodeId.textValue());
			if (cyNode == null) {
				cyNode = network.addNode();

				// Use ID as unique name.
				network.getRow(cyNode).set(CyNetwork.NAME, nodeId.textValue());
				nodeMap.put(nodeId.textValue(), cyNode);
				addTableData(data, cyNode, network);
			}

			// Get (x,y) location if available.
			final JsonNode position = node.get(POSITION.getTag());
			if (position != null) {
				final JsonNode x = position.get(POSITION_X.getTag());
				final JsonNode y = position.get(POSITION_Y.getTag());
				final Double[] positionArray = new Double[2];
				positionArray[0] = x.doubleValue();
				positionArray[1] = y.doubleValue();
				positionMap.put(cyNode, positionArray);
			}
		}
		return nodeMap;
	}

	private final void addEdges(final CyNetwork network, final JsonNode edges, final Map<String, CyNode> nodeMap) {

		for (final JsonNode edge : edges) {
			final JsonNode data = edge.get(DATA.getTag());
			final JsonNode source = data.get(SOURCE.getTag());
			final JsonNode target = data.get(TARGET.getTag());

			final CyNode sourceNode = nodeMap.get(source.textValue());
			final CyNode targetNode = nodeMap.get(target.textValue());

			final CyEdge newEdge = network.addEdge(sourceNode, targetNode, true);
		}
	}

	private final void addTableData(JsonNode data, CyIdentifiable graphObject, CyNetwork network) {

		final Iterator<String> fieldNames = data.fieldNames();
		while (fieldNames.hasNext()) {
			final String fieldName = fieldNames.next();
			if (fieldName.equals(ID.getTag()) == false && fieldName.equals(CyIdentifiable.SUID) == false
					&& fieldName.equals(CyNetwork.SELECTED) == false) {
				if (network.getRow(graphObject).getTable().getColumn(fieldName) == null) {
					network.getRow(graphObject).getTable().createColumn(fieldName, String.class, false);
				}
				network.getRow(graphObject).set(fieldName, data.get(fieldName).asText());
			}
		}
	}

	protected Map<CyNode, Double[]> getNodePosition() {
		return this.positionMap;
	}
}
