package org.cytoscape.ding.customgraphics;

import org.cytoscape.ding.impl.customgraphics.vector.CustomGraphicsProperty;


public class CustomGraphicsPropertyImpl<T> implements
		CustomGraphicsProperty<T> {
	
	private T value;
	private final T defaultValue;
	
	public CustomGraphicsPropertyImpl(final T defaultValue) {
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}
	
	

	public T getDefaultValue() {
		return defaultValue;
	}

	

	public T getValue() {
		return value;
	}

	

	public void setValue(Object value) {
		if(this.value.getClass().isAssignableFrom(value.getClass()) == false)
			throw new IllegalArgumentException("The value type is not compatible.");
		else
			this.value = (T) value;
	}

}
