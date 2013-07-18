package org.cytoscape.filter.internal.transformers;

import org.cytoscape.filter.internal.predicates.NumericPredicateDelegate;
import org.cytoscape.filter.internal.predicates.NumericPredicateDelegates;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.predicates.NumericPredicate;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.Tunable;

public class DegreeFilter implements Filter<CyNetwork, CyIdentifiable> {
	private NumericPredicateDelegate delegate;
	private NumericPredicate predicate;
	
	@Tunable
	public CyEdge.Type edgeType;

	@Tunable
	public Number criterion;
	
	@Tunable
	public NumericPredicate getPredicate() {
		return predicate;
	}
	
	@Tunable
	public void setPredicate(NumericPredicate predicate) {
		this.predicate = predicate;
		delegate = NumericPredicateDelegates.get(predicate);
	}
	
	@Override
	public String getName() {
		return "Degree";
	}
	
	@Override
	public String getId() {
		return Transformers.DEGREE_FILTER;
	}

	@SuppressWarnings("unused")
	@Override
	public boolean accepts(CyNetwork context, CyIdentifiable element) {
		if (!(element instanceof CyNode)) {
			return false;
		}
		
		CyNode node = (CyNode) element;
		int hits = 0;
		for (CyEdge edge : context.getAdjacentEdgeIterable(node, edgeType)) {
			hits++;
		}
		return delegate.accepts(criterion, hits);
	}
	
	@Override
	public Class<CyNetwork> getContextType() {
		return CyNetwork.class;
	}
	
	@Override
	public Class<CyIdentifiable> getElementType() {
		return CyIdentifiable.class;
	}
}
