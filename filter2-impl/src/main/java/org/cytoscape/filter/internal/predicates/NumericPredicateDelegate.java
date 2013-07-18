package org.cytoscape.filter.internal.predicates;

public interface NumericPredicateDelegate {
	boolean accepts(Number criterion, Number value);
}
