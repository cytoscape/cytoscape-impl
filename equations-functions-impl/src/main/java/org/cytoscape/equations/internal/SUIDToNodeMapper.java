package org.cytoscape.equations.internal;


import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.AboutToRemoveNodesEvent;
import org.cytoscape.model.events.AboutToRemoveNodesListener;
import org.cytoscape.model.events.AddedNodesEvent;
import org.cytoscape.model.events.AddedNodesListener;


public class SUIDToNodeMapper implements AddedNodesListener, AboutToRemoveNodesListener {
	private final Map<Long, CyNode> suidToNodeMap = new HashMap<Long, CyNode>();

	public void handleEvent(final AddedNodesEvent event) {
		for (CyNode node : event.getPayloadCollection()) {
			suidToNodeMap.put(node.getSUID(), node);
		}
	}

	public void handleEvent(final AboutToRemoveNodesEvent event) {
		for (CyNode node : event.getNodes()) {
			suidToNodeMap.remove(node.getSUID());
		}
	}

	public CyNode getNode(final Long id) {
		return suidToNodeMap.get(id);
	}
}