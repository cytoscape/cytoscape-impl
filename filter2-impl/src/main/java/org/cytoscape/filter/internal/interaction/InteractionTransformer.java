package org.cytoscape.filter.internal.interaction;

import org.cytoscape.filter.model.AbstractTransformer;
import org.cytoscape.filter.model.ElementTransformer;
import org.cytoscape.filter.model.TransformerSink;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.Tunable;

public class InteractionTransformer extends AbstractTransformer<CyNetwork, CyIdentifiable> implements ElementTransformer<CyNetwork, CyIdentifiable> {
	@Tunable()
	public boolean selectSource;
	
	@Tunable()
	public boolean selectTarget;

	@Override
	public String getName() {
		return "Interaction Transformer";
	}

	@Override
	public String getId() {
		return Transformers.INTERACTION_TRANSFORMER;
	}

	@Override
	public Class<CyNetwork> getContextType() {
		return CyNetwork.class;
	}

	@Override
	public Class<CyIdentifiable> getElementType() {
		return CyIdentifiable.class;
	}

	@Override
	public void apply(CyNetwork context, CyIdentifiable element, TransformerSink<CyIdentifiable> sink) {
		sink.collect(element);
		
		if (!(element instanceof CyEdge)) {
			return;
		}
		
		CyEdge edge = (CyEdge) element;
		if (selectSource) {
			sink.collect(edge.getSource());
		}
		if (selectTarget) {
			sink.collect(edge.getTarget());
		}
	}
}
