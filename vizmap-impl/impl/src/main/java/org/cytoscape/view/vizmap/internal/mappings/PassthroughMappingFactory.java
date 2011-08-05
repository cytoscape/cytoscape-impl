package org.cytoscape.view.vizmap.internal.mappings;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

public class PassthroughMappingFactory implements VisualMappingFunctionFactory{

	@Override
	public <K, V> VisualMappingFunction<K, V> createVisualMappingFunction(final String attributeName,
			Class<K> attrValueType, VisualProperty<V> vp) {
		return new PassthroughMappingImpl<K, V>(attributeName, attrValueType, vp);
	}
	
	@Override public String toString() {
		return PassthroughMapping.PASSTHROUGH;
	}

	@Override
	public Class<?> getMappingFunctionType() {
		return PassthroughMapping.class;
	}

}
