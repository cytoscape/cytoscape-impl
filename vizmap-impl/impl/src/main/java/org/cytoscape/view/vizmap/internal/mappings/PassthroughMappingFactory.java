package org.cytoscape.view.vizmap.internal.mappings;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.view.vizmap.mappings.ValueTranslator;

public class PassthroughMappingFactory implements VisualMappingFunctionFactory {
	
	private static final Map<Class<?>, ValueTranslator<?, ?>> TRANSLATORS = new HashMap<Class<?>, ValueTranslator<?,?>>();
	private static final ValueTranslator<Object, String> DEFAULT_TRANSLATOR = new StringTranslator();
	
	public void addValueTranslator(ValueTranslator<?, ?> translator, Map props) {
		if (translator != null)
			TRANSLATORS.put(translator.getTranslatedValueType(), translator);
	}
	
	public void removeValueTranslator(ValueTranslator<?, ?> translator, Map props) {
	}
	
	@Override
	public <K, V> VisualMappingFunction<K, V> createVisualMappingFunction(final String attributeName,
			final Class<K> attrValueType, final VisualProperty<V> vp) {

		final ValueTranslator<?, ?> translator = TRANSLATORS.get(vp.getRange().getType());

		if (translator != null)
			return new PassthroughMappingImpl<K, V>(attributeName, attrValueType, vp,
					(ValueTranslator<K, V>) translator);
		else
			return new PassthroughMappingImpl<K, V>(attributeName, attrValueType, vp,
					(ValueTranslator<K, V>) DEFAULT_TRANSLATOR);
	}

	@Override
	public String toString() {
		return PassthroughMapping.PASSTHROUGH;
	}

	@Override
	public Class<?> getMappingFunctionType() {
		return PassthroughMapping.class;
	}
}
