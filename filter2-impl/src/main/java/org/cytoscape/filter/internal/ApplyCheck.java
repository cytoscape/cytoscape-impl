package org.cytoscape.filter.internal;

public interface ApplyCheck<C,E> {

	public boolean appliesTo(C context, E element);
	
}
