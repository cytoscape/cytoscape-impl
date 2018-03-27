package org.cytoscape.filter.internal.predicates;

public interface StringPredicateDelegate extends PredicateDelegate {
	boolean accepts(String criterion, String lowerCaseCriterion, String value, boolean caseSensitive);
}
