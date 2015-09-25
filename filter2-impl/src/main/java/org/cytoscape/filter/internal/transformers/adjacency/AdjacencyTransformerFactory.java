package org.cytoscape.filter.internal.transformers.adjacency;

import org.cytoscape.filter.model.ElementTransformerFactory;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public class AdjacencyTransformerFactory implements ElementTransformerFactory<CyNetwork, CyIdentifiable> {

	@Override
	public String getId() {
		return Transformers.ADJACENCY_TRANSFORMER;
	}

	@Override
	public AdjacencyTransformer createElementTransformer() {
		return new AdjacencyTransformer();
	}

}
