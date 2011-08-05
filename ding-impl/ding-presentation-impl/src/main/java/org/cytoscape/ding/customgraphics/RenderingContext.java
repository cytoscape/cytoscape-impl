package org.cytoscape.ding.customgraphics;

public interface RenderingContext<T> {

	public void setContext(T context);
	public T getContext();
}
