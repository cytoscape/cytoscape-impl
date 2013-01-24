package org.cytoscape.view.vizmap.internal;

/*
 * #%L
 * Cytoscape VizMap Impl (vizmap-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.internal.mappings.ContinuousMappingImpl;
import org.cytoscape.view.vizmap.internal.mappings.DiscreteMappingImpl;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.ContinuousMappingPoint;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

public class VisualStyleFactoryImpl implements VisualStyleFactory {

	private final VisualLexiconManager lexManager;
	private final CyServiceRegistrar serviceRegistrar;
	private final CyEventHelper eventHelper;

	private final VisualMappingFunctionFactory passThroughFactory;

	public VisualStyleFactoryImpl(final VisualLexiconManager lexManager, final CyServiceRegistrar serviceRegistrar,
			final VisualMappingFunctionFactory passThroughFactory, final CyEventHelper eventHelper) {
		this.lexManager = lexManager;
		this.serviceRegistrar = serviceRegistrar;
		this.passThroughFactory = passThroughFactory;
		this.eventHelper = eventHelper;
	}

	@Override
	public VisualStyle createVisualStyle(final VisualStyle original) {
		final VisualStyle copy = new VisualStyleImpl(original.getTitle(), lexManager, serviceRegistrar, eventHelper);

		copyDefaultValues(original, copy);
		copyMappingFunctions(original, copy);

		return copy;
	}

	@Override
	public VisualStyle createVisualStyle(final String title) {
		return new VisualStyleImpl(title, lexManager, serviceRegistrar, eventHelper);
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
	private <K, V> VisualMappingFunction<K, V> createPassthrough(final PassthroughMapping<K, V> originalMapping) {

		final String attrName = originalMapping.getMappingColumnName();
		final Class<K> colType = originalMapping.getMappingColumnType();

		final PassthroughMapping<K, V> copyMapping = (PassthroughMapping<K, V>) passThroughFactory
				.createVisualMappingFunction(attrName, colType, originalMapping.getVisualProperty());
		return copyMapping;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <K, V> VisualMappingFunction<K, V> createContinuous(final ContinuousMapping<K, V> originalMapping) {
		final String attrName = originalMapping.getMappingColumnName();
		final Class<?> colType = originalMapping.getMappingColumnType();

		final ContinuousMapping<K, V> copyMapping = new ContinuousMappingImpl(attrName, colType,
				originalMapping.getVisualProperty(), eventHelper);
		List<ContinuousMappingPoint<K, V>> points = originalMapping.getAllPoints();
		for (ContinuousMappingPoint<K, V> point : points)
			copyMapping.addPoint(point.getValue(), point.getRange());

		return copyMapping;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <K, V> VisualMappingFunction<K, V> createDiscrete(final DiscreteMapping<K, V> originalMapping) {
		final String attrName = originalMapping.getMappingColumnName();
		final Class<K> colType = originalMapping.getMappingColumnType();

		final DiscreteMapping<K, V> copyMapping = new DiscreteMappingImpl(attrName, colType,
				originalMapping.getVisualProperty(), eventHelper);

		copyMapping.putAll(originalMapping.getAll());
		return copyMapping;
	}
}
