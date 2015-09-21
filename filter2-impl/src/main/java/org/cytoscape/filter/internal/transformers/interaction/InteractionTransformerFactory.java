package org.cytoscape.filter.internal.transformers.interaction;

import org.cytoscape.filter.model.ElementTransformer;
import org.cytoscape.filter.model.ElementTransformerFactory;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public class InteractionTransformerFactory implements ElementTransformerFactory<CyNetwork, CyIdentifiable> {
	@Override
	public String getId() {
		return Transformers.INTERACTION_TRANSFORMER;
	}

	@Override
	public ElementTransformer<CyNetwork, CyIdentifiable> createElementTransformer() {
		return new InteractionTransformer();
	}
}
