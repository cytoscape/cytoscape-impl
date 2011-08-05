package org.cytoscape.equations.internal;


import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.events.AboutToRemoveEdgesEvent;
import org.cytoscape.model.events.AboutToRemoveEdgesListener;
import org.cytoscape.model.events.AddedEdgesEvent;
import org.cytoscape.model.events.AddedEdgesListener;


public class SUIDToEdgeMapper implements AddedEdgesListener, AboutToRemoveEdgesListener {
	private final Map<Long, CyEdge> suidToEdgeMap = new HashMap<Long, CyEdge>();

	public void handleEvent(final AddedEdgesEvent event) {
		for (CyEdge edge : event.getPayloadCollection()) {
			suidToEdgeMap.put(edge.getSUID(), edge);
		}
	}

	public void handleEvent(final AboutToRemoveEdgesEvent event) {
		for (CyEdge edge : event.getEdges()) {
			suidToEdgeMap.remove(edge.getSUID());
		}
	}

	public CyEdge getEdge(final Long id) {
		return suidToEdgeMap.get(id);
	}
}