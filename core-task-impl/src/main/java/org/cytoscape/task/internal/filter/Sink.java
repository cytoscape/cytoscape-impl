package org.cytoscape.task.internal.filter;

import java.util.ArrayList;
import java.util.Collection;

import org.cytoscape.filter.model.TransformerSink;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;

class Sink implements TransformerSink<CyIdentifiable> {

	private Collection<CyNode> nodes = new ArrayList<>();
	private Collection<CyEdge> edges = new ArrayList<>();
	
	@Override
	public void collect(CyIdentifiable element) {
		if(element instanceof CyNode)
			nodes.add((CyNode)element);
		else if(element instanceof CyEdge)
			edges.add((CyEdge)element);
	}
	
	public Collection<CyNode> getNodes() {
		return nodes;
	}
	
	public Collection<CyEdge> getEdges() {
		return edges;
	}

}
