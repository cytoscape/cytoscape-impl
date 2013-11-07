package org.cytoscape.filter.internal.attribute;

import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.model.FilterFactory;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public class AttributeFilterFactory implements FilterFactory<CyNetwork, CyIdentifiable> {

	@Override
	public Filter<CyNetwork, CyIdentifiable> createFilter() {
		return new AttributeFilter();
	}

	@Override
	public String getId() {
		return Transformers.ATTRIBUTE_FILTER;
	}
}
