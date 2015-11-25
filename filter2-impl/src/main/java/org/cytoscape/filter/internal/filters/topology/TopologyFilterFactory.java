package org.cytoscape.filter.internal.filters.topology;

import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.model.FilterFactory;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public class TopologyFilterFactory implements FilterFactory<CyNetwork, CyIdentifiable> {

	@Override
	public Filter<CyNetwork, CyIdentifiable> createFilter() {
		return new TopologyFilter();
	}

	@Override
	public String getId() {
		return Transformers.TOPOLOGY_FILTER;
	}

}
