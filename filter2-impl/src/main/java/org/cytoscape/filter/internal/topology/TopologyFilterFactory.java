package org.cytoscape.filter.internal.topology;

import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.model.TransformerFactory;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public class TopologyFilterFactory implements TransformerFactory<CyNetwork, CyIdentifiable> {

	@Override
	public Transformer<CyNetwork, CyIdentifiable> createTransformer() {
		return new TopologyFilter();
	}

	@Override
	public String getId() {
		return Transformers.TOPOLOGY_FILTER;
	}

}
