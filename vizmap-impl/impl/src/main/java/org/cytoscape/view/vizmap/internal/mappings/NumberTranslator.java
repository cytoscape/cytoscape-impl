package org.cytoscape.view.vizmap.internal.mappings;

import org.cytoscape.view.vizmap.mappings.ValueTranslator;

//TODO: DELETE?
public class NumberTranslator<T extends Number> implements ValueTranslator<Object, T> {

	private final Class<T> translatedValueType;
	
	public NumberTranslator(final Class<T> translatedValueType) {
		this.translatedValueType = translatedValueType;
	}
	
	@Override
	public T translate(Object inputValue) {
		if(inputValue instanceof Number) {
			return (T) inputValue;
		} else {
			return null;
		}
	}

	@Override
	public Class<T> getTranslatedValueType() {
		return translatedValueType;
	}

}
