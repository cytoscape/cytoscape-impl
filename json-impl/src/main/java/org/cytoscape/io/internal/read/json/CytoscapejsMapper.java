package org.cytoscape.io.internal.read.json;

import static org.cytoscape.io.internal.write.json.serializer.CyJsonToken.*;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyEdge;
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

	public CytoscapejsMapper(final CyNetworkFactory factory) {
		this.factory = factory;
	}

	@Override
	public CyNetwork createNetwork(final JsonNode rootNode) {

		final JsonNode elements = rootNode.get(ELEMENTS.getName());
		final JsonNode nodes = elements.get(NODES.getName());
		final JsonNode edges = elements.get(EDGES.getName());

		final CyNetwork network = factory.createNetwork();
		final Map<String, CyNode> nodeMap = this.addNodes(network, nodes);
		this.addEdges(network, edges, nodeMap);

		return network;
	}

	private final Map<String, CyNode> addNodes(final CyNetwork network,
			final JsonNode nodes) {

		final Map<String, CyNode> nodeMap = new HashMap<String, CyNode>();

		for (final JsonNode node : nodes) {
			final JsonNode data = node.get(DATA.getName());
			System.out.println(data);
			final JsonNode nodeId = data.get(ID.getName());
			CyNode cyNode = nodeMap.get(nodeId.textValue());
			if (cyNode == null) {
				cyNode = network.addNode();
				network.getRow(cyNode).set(CyNetwork.NAME, nodeId.textValue());
				nodeMap.put(nodeId.textValue(), cyNode);
			}
		}
		return nodeMap;
	}

	private final void addEdges(final CyNetwork network, final JsonNode edges,
			final Map<String, CyNode> nodeMap) {

		for (final JsonNode edge : edges) {
			final JsonNode data = edge.get(DATA.getName());
			System.out.println(data);
			final JsonNode source = data.get(SOURCE.getName());
			final JsonNode target = data.get(TARGET.getName());
			
			final CyNode sourceNode = nodeMap.get(source.textValue());
			final CyNode targetNode = nodeMap.get(target.textValue());
			
			final CyEdge newEdge = network.addEdge(sourceNode, targetNode, true);
		}
	}

	private final void readAttributes() {

	}
}
