package org.cytoscape.cg.model;

public interface RenderingContext<T> {

	void setContext(T context);
	
	T getContext();
}
