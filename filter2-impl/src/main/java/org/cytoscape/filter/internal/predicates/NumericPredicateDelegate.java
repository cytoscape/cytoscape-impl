package org.cytoscape.filter.internal.predicates;

public interface NumericPredicateDelegate extends PredicateDelegate {
	boolean accepts(Number lowerBound, Number upperBound, Number value);
}
