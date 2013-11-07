package org.cytoscape.filter.internal.degree;

import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.model.FilterFactory;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public class DegreeFilterFactory implements FilterFactory<CyNetwork, CyIdentifiable> {
	@Override
	public Filter<CyNetwork, CyIdentifiable> createFilter() {
		return new DegreeFilter();
	}

	@Override
	public String getId() {
		return Transformers.DEGREE_FILTER;
	}

}
