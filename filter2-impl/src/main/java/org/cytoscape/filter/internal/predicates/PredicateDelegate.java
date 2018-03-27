package org.cytoscape.filter.internal.predicates;

public interface PredicateDelegate {
	
	default boolean unsupported() {
		return false;
	}

}
