package org.cytoscape.ding.customgraphics.vector;

public interface CustomGraphicsProperty<T> {
	
	public T getDefaultValue();
	
	public T getValue();
	public void setValue(Object value);
}
