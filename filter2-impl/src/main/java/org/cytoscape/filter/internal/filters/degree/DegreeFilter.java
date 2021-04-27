package org.cytoscape.filter.internal.filters.degree;

import java.util.LinkedList;
import java.util.List;

import org.cytoscape.filter.internal.predicates.NumericPredicateDelegate;
import org.cytoscape.filter.internal.predicates.PredicateDelegates;
import org.cytoscape.filter.model.AbstractValidatableTransformer;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.model.ValidationWarning;
import org.cytoscape.filter.predicates.Predicate;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.Tunable;

public class DegreeFilter extends AbstractValidatableTransformer<CyNetwork, CyIdentifiable> implements Filter<CyNetwork, CyIdentifiable> {
	private NumericPredicateDelegate delegate;
	private Predicate predicate;
	
	private CyEdge.Type edgeType;
	private Object rawCriterion;
	private Number lowerBound;
	private Number upperBound;
	
	@Tunable
	public CyEdge.Type getEdgeType() {
		return edgeType;
	}
	
	public void setEdgeType(CyEdge.Type type) {
		edgeType = type;
		notifyListeners();
	}
	
	@Tunable
	public Object getCriterion() {
		return rawCriterion;
	}
	
	@SuppressWarnings("unchecked")
	public void setCriterion(Object criterion) {
		rawCriterion = criterion;
		
		if (criterion == null) {
			lowerBound = null;
			upperBound = null;
		} else if (criterion instanceof List) {
			List<Number> list = (List<Number>) criterion;
			lowerBound = list.get(0);
			upperBound = list.get(1);
			rawCriterion = new Number[] { lowerBound, upperBound };
		} else if (criterion instanceof Number[]) {
			Number[] range = (Number[]) criterion;
			lowerBound = range[0];
			upperBound = range[1];
		} else if (criterion instanceof Number) {
			lowerBound = (Number) criterion;
			upperBound = lowerBound;
		}
		notifyListeners();
	}
	
	@Tunable
	public Predicate getPredicate() {
		return predicate;
	}
	
	public void setPredicate(Predicate predicate) {
		this.predicate = predicate;
		delegate = PredicateDelegates.getNumericDelegate(predicate);
		notifyListeners();
	}
	
	@Override
	public String getName() {
		return "Degree Filter";
	}
	
	@Override
	public String getId() {
		return Transformers.DEGREE_FILTER;
	}

	@Override
	public boolean appliesTo(CyNetwork context, CyIdentifiable element) {
		return element instanceof CyNode;
	}
	
	@Override
	@SuppressWarnings("unused")
	public boolean accepts(CyNetwork context, CyIdentifiable element) {
		if (!appliesTo(context, element)) {
			return false;
		}
		
		if (upperBound == null && lowerBound == null) {
			return true;
		}
		
		CyNode node = (CyNode) element;
		int hits = 0;
		for (CyEdge edge : context.getAdjacentEdgeIterable(node, edgeType)) {
			hits++;
		}
		return delegate.accepts(lowerBound, upperBound, hits);
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
	public List<ValidationWarning> validateCreation() {
		List<ValidationWarning> output = new LinkedList<ValidationWarning>();
		if (edgeType != CyEdge.Type.ANY 
			&& edgeType != CyEdge.Type.OUTGOING 
			&& edgeType != CyEdge.Type.INCOMING
		) {
			output.add(new ValidationWarning("Invalid edge type: " + edgeType+ ". Must be one of [DIRECTED, OUTGOING, INCOMING]"));
		}
			
		return output;
	}
}
