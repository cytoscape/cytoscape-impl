package org.cytoscape.filter.internal.column;

import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.model.FilterFactory;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public class ColumnFilterFactory implements FilterFactory<CyNetwork, CyIdentifiable> {

	@Override
	public Filter<CyNetwork, CyIdentifiable> createFilter() {
		return new ColumnFilter();
	}

	@Override
	public String getId() {
		return Transformers.COLUMN_FILTER;
	}
}
