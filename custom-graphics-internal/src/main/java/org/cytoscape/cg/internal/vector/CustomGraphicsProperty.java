package org.cytoscape.cg.internal.vector;

public interface CustomGraphicsProperty<T> {
	
	T getDefaultValue();
	
	T getValue();
	void setValue(Object value);
}
