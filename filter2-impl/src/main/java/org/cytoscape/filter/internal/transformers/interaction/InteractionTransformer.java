package org.cytoscape.filter.internal.transformers.interaction;

import org.cytoscape.filter.internal.AbstractMemoizedTransformer;
import org.cytoscape.filter.internal.filters.composite.CompositeFilterImpl;
import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.filter.model.ElementTransformer;
import org.cytoscape.filter.model.SubFilterTransformer;
import org.cytoscape.filter.model.TransformerSink;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.Tunable;

public class InteractionTransformer extends AbstractMemoizedTransformer<CyNetwork, CyIdentifiable> 
									implements ElementTransformer<CyNetwork, CyIdentifiable>,
											   SubFilterTransformer<CyNetwork,CyIdentifiable> {
	
	public static enum Action {
		ADD, REPLACE
	}

	// Internally use a compositeFilter to store child filters
	private final CompositeFilter<CyNetwork,CyIdentifiable> nodeFilter;
	
	@Tunable
	public boolean selectSource;
	
	@Tunable
	public boolean selectTarget;
	
	private Action action = Action.ADD;
		
	
	public InteractionTransformer() {
		nodeFilter = new CompositeFilterImpl<>(CyNetwork.class,CyIdentifiable.class);
		nodeFilter.setType(CompositeFilter.Type.ALL); // ALL accepts if empty
		nodeFilter.addListener(this::notifyListeners);
		selectSource = true;
		selectTarget = true;
	}
	
	@Tunable
	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
		notifyListeners();
	}
	
	
	@Override
	public String getName() {
		return "Edge Interaction Transformer";
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
	public void apply(CyNetwork network, CyIdentifiable element, TransformerSink<CyIdentifiable> sink) {
		if(action == Action.ADD)
			sink.collect(element);
		
		if(element instanceof CyEdge) {
			CyEdge edge = (CyEdge) element;

			if(selectSource) {
				CyNode source = edge.getSource();
				if(memoizedFilter.accepts(network, source)) {
					sink.collect(source);
				}
			}
			
			if(selectTarget) {
				CyNode target = edge.getTarget();
				if(memoizedFilter.accepts(network, target)) {
					sink.collect(target);
				}
			}
		}
	}

	@Override
	public CompositeFilter<CyNetwork, CyIdentifiable> getCompositeFilter() {
		return nodeFilter;
	}
	
	public boolean hasSubfilters() {
		return nodeFilter.getLength() > 0;
	}
}
