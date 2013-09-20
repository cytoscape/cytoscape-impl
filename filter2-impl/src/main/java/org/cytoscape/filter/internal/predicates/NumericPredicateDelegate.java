package org.cytoscape.filter.internal.predicates;

public interface NumericPredicateDelegate {
	boolean accepts(Number lowerBound, Number upperBound, Number value);
}
