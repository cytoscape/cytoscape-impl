package org.cytoscape.view.vizmap.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.internal.mappings.ContinuousMappingImpl;
import org.cytoscape.view.vizmap.internal.mappings.DiscreteMappingImpl;
import org.cytoscape.view.vizmap.internal.mappings.PassthroughMappingImpl;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.ContinuousMappingPoint;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

public class VisualStyleFactoryImpl implements VisualStyleFactory {

	private final VisualLexiconManager lexManager;
	private final CyServiceRegistrar serviceRegistrar;
	private final CyNetworkManager networkManager;

	public VisualStyleFactoryImpl(final VisualLexiconManager lexManager, final CyServiceRegistrar serviceRegistrar,
			final CyNetworkManager networkManager) {
		this.lexManager = lexManager;
		this.serviceRegistrar = serviceRegistrar;
		this.networkManager = networkManager;
	}

	@Override
	public VisualStyle createVisualStyle(final VisualStyle original) {
		final VisualStyle copy = new VisualStyleImpl(original.getTitle(), lexManager, serviceRegistrar,networkManager);

		copyDefaultValues(original, copy);
		copyMappingFunctions(original, copy);

		return copy;
	}

	@Override
	public VisualStyle createVisualStyle(final String title) {
		return new VisualStyleImpl(title, lexManager, serviceRegistrar, networkManager);
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

	/**
	 * Copy Mapping functions
	 * 
	 * @param original
	 * @param copy
	 */
	private void copyMappingFunctions(final VisualStyle original, final VisualStyle copy) {
		final Collection<VisualMappingFunction<?, ?>> allMapping = original.getAllVisualMappingFunctions();

		for (VisualMappingFunction<?, ?> mapping : allMapping) {
			VisualMappingFunction<?, ?> copyMapping = null;
			if (mapping instanceof PassthroughMapping) {
				copyMapping = createPassthrough((PassthroughMapping<?, ?>) mapping);
			} else if (mapping instanceof ContinuousMapping) {
				copyMapping = createContinuous((ContinuousMapping<?, ?>) mapping);
			} else if (mapping instanceof DiscreteMapping) {
				copyMapping = createDiscrete((DiscreteMapping<?, ?>) mapping);
			}

			if (copyMapping != null)
				copy.addVisualMappingFunction(mapping);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <K,V> VisualMappingFunction<K, V> createPassthrough(final PassthroughMapping<K, V> originalMapping) {

		final String attrName = originalMapping.getMappingColumnName();
		final Class<K> colType = originalMapping.getMappingColumnType();

		final PassthroughMapping<K, V> copyMapping = new PassthroughMappingImpl(attrName, colType,
				originalMapping.getMappingTable(), originalMapping.getVisualProperty());
		return copyMapping;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <K,V> VisualMappingFunction<K, V> createContinuous(final ContinuousMapping<K, V> originalMapping) {
		final String attrName = originalMapping.getMappingColumnName();
		final Class<?> colType = originalMapping.getMappingColumnType();
		
		final ContinuousMapping<K,V> copyMapping = new ContinuousMappingImpl(attrName, colType,
				originalMapping.getMappingTable(), originalMapping.getVisualProperty());
		List<ContinuousMappingPoint<K, V>> points = originalMapping.getAllPoints();
		for(ContinuousMappingPoint<K, V> point: points)
			 copyMapping.addPoint(point.getValue(), point.getRange());
		
		return copyMapping;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <K,V> VisualMappingFunction<K,V> createDiscrete(final DiscreteMapping<K, V> originalMapping) {
		final String attrName = originalMapping.getMappingColumnName();
		final Class<K> colType = originalMapping.getMappingColumnType();

		final DiscreteMapping<K, V> copyMapping = new DiscreteMappingImpl(attrName, colType,
				originalMapping.getMappingTable(), originalMapping.getVisualProperty());
		
		copyMapping.putAll(originalMapping.getAll());
		return copyMapping;
	}
}
