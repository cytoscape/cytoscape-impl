package org.cytoscape.filter.internal.attribute;

import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.model.TransformerFactory;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public class AttributeFilterFactory implements TransformerFactory<CyNetwork, CyIdentifiable> {

	@Override
	public Transformer<CyNetwork, CyIdentifiable> createTransformer() {
		return new AttributeFilter();
	}

	@Override
	public String getId() {
		return Transformers.ATTRIBUTE_FILTER;
	}

}
