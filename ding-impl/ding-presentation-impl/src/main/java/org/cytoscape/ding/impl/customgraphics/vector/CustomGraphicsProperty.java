package org.cytoscape.ding.impl.customgraphics.vector;

public interface CustomGraphicsProperty<T> {
	
	public T getDefaultValue();
	
	public T getValue();
	public void setValue(Object value);
}
