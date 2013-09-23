package org.cytoscape.io.internal.write.json.serializer;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Use selected node as the root.
 * 
 * 
 */
public class D3CyNetworkViewTreeSerializer extends JsonSerializer<CyNetworkView> {

	private Set<CyNode> expanded;
	
	@Override
	public void serialize(final CyNetworkView networkView, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {

		final CyNetwork network = networkView.getModel();
		Collection<CyRow> root = network.getDefaultNodeTable().getMatchingRows(CyNetwork.SELECTED, true);

		if (root.size() != 1) {
			throw new IllegalStateException("You need to select one node as the root for this tree.");
		}

		CyRow rootNodeRow = root.iterator().next();
		final CyNode rootNode = network.getNode(rootNodeRow.get(CyIdentifiable.SUID, Long.class));
		if (rootNode == null) {
			throw new IllegalStateException("Could not find the root for this tree.");
		}

		jgen.useDefaultPrettyPrinter();

		expanded = new HashSet<CyNode>();
		getChildren(network, rootNode, jgen);
	}

	private final void getChildren(CyNetwork network, CyNode node, JsonGenerator jgen) throws IOException {
		if(network == null || node == null || jgen == null)
			throw new NullPointerException();
		
		expanded.add(node);
		
		final List<CyNode> children = network.getNeighborList(node, org.cytoscape.model.CyEdge.Type.ANY);
		System.out.println("Children Count = " + children.size());
		
		jgen.writeStartObject();
		jgen.writeStringField(CyNetwork.NAME, network.getRow(node).get(CyNetwork.NAME, String.class));
		// Write labels
		CyRow row = network.getRow(node);
		jgen.writeObject(row);
		
		
		if(children.isEmpty()) {
			// Leaf
			jgen.writeEndObject();
			return;
		} else {
			// Remove if already visited.
			Set<CyNode> notVisited = new HashSet<CyNode>();
			for(CyNode child: children) {
				if(expanded.contains(child) == false)
					notVisited.add(child);
			}
			
			if(notVisited.isEmpty()) {
				jgen.writeEndObject();
				return;
			}
				
			// Need to add children section
			jgen.writeArrayFieldStart("children");
			for(CyNode child: notVisited) {
				getChildren(network, child, jgen);
			}
			jgen.writeEndArray();
			jgen.writeEndObject();
		}
		
	}

	@Override
	public Class<CyNetworkView> handledType() {
		return CyNetworkView.class;
	}
}
