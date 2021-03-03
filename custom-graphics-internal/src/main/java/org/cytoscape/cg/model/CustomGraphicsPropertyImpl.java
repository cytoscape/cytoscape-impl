package org.cytoscape.cg.model;

import org.cytoscape.cg.internal.vector.CustomGraphicsProperty;

public class CustomGraphicsPropertyImpl<T> implements CustomGraphicsProperty<T> {
	
	private T value;
	private final T defaultValue;
	
	public CustomGraphicsPropertyImpl(final T defaultValue) {
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}
	
	@Override
	public T getDefaultValue() {
		return defaultValue;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setValue(Object value) {
		if(this.value.getClass().isAssignableFrom(value.getClass()) == false)
			throw new IllegalArgumentException("The value type is not compatible.");
		else
			this.value = (T) value;
	}
}
