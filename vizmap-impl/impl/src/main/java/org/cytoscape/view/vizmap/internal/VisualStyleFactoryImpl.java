package org.cytoscape.view.vizmap.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;

public class VisualStyleFactoryImpl implements VisualStyleFactory {

	private final VisualLexiconManager lexManager;
	private final CyServiceRegistrar serviceRegistrar;

	public VisualStyleFactoryImpl(final VisualLexiconManager lexManager, final CyServiceRegistrar serviceRegistrar) {
		this.lexManager = lexManager;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public VisualStyle createVisualStyle(final VisualStyle original) {
		final VisualStyle copy = new VisualStyleImpl(original.getTitle(), lexManager, serviceRegistrar);

		copyDefaultValues(original, copy);
		copyMappingFunctions(original, copy);

		return copy;
	}

	@Override
	public VisualStyle createVisualStyle(final String title) {
		return new VisualStyleImpl(title, lexManager, serviceRegistrar);
	}

	private <V, S extends V> void copyDefaultValues(final VisualStyle original, final VisualStyle copy) {
		Set<VisualProperty<?>> visualProps = new HashSet<VisualProperty<?>>();
		visualProps.addAll(lexManager.getNetworkVisualProperties());
		visualProps.addAll(lexManager.getNodeVisualProperties());
		visualProps.addAll(lexManager.getEdgeVisualProperties());

		for (VisualProperty<?> vp : visualProps) {
			S value = (S) original.getDefaultValue(vp);

			// TODO: if the value is not immutable, this can create problems,
			// since it is not setting a copy!
			if (value != null)
				copy.setDefaultValue((VisualProperty<V>) vp, value);
		}
	}

	private void copyMappingFunctions(final VisualStyle original, final VisualStyle copy) {
		Collection<VisualMappingFunction<?, ?>> allMapping = original.getAllVisualMappingFunctions();

		for (VisualMappingFunction<?, ?> mapping : allMapping) {
			String attrName = mapping.getMappingColumnName();
			VisualProperty<?> vp = mapping.getVisualProperty();

			// TODO: clone mappings
			// copy.addVisualMappingFunction(mapping);
		}
	}
}
