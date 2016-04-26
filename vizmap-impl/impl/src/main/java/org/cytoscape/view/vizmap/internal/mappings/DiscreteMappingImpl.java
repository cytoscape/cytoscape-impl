package org.cytoscape.view.vizmap.internal.mappings;

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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.events.VisualMappingFunctionChangeRecord;
import org.cytoscape.view.vizmap.events.VisualMappingFunctionChangedEvent;
import org.cytoscape.view.vizmap.mappings.AbstractVisualMappingFunction;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;

/**
 * Implements a lookup table mapping data to values of a particular class. The
 * data value is extracted from a bundle of attributes by using a specified data
 * attribute name.
 */
public class DiscreteMappingImpl<K, V> extends AbstractVisualMappingFunction<K, V> implements DiscreteMapping<K, V> {

	// contains the actual map elements (sorted)
	private final Map<K, V> attribute2visualMap;

	/**
	 * Constructor.
	 * 
	 * @param attrName
	 * @param attrType
	 * @param vp
	 */
	public DiscreteMappingImpl(final String attrName, final Class<K> attrType, final VisualProperty<V> vp,
			final CyEventHelper eventHelper) {
		super(attrName, attrType, vp, eventHelper);
		attribute2visualMap = new HashMap<>();
	}

	@Override
	public String toString() {
		return DiscreteMapping.DISCRETE;
	}

	@Override
	public V getMappedValue(final CyRow row) {
		V value = null;

		if (row != null && row.isSet(columnName)) {
			// Skip if source attribute is not defined.
			// ViewColumn will automatically substitute the per-VS or global
			// default, as appropriate
			final CyColumn column = row.getTable().getColumn(columnName);
			final Class<?> attrClass = column.getType();

			if (attrClass.isAssignableFrom(List.class)) {
				List<?> list = row.getList(columnName, column.getListElementType());

				if (list != null) {
					for (Object item : list) {
						// TODO: should we convert other types to String?
						String key = item.toString();
						value = attribute2visualMap.get(key);

						if (value != null)
							break;
					}
				}
			} else {
				K key = row.get(columnName, columnType);

				if (key != null)
					value = attribute2visualMap.get(key);
			}
		}

		return value;
	}

	@Override
	public V getMapValue(K key) {
		return attribute2visualMap.get(key);
	}

	@Override
	public <T extends V> void putMapValue(final K key, final T value) {
		attribute2visualMap.put(key, value);
		eventHelper.addEventPayload((VisualMappingFunction) this, new VisualMappingFunctionChangeRecord(),
				VisualMappingFunctionChangedEvent.class);
	}

	@Override
	public <T extends V> void putAll(Map<K, T> map) {
		attribute2visualMap.putAll(map);
		VisualMappingFunction function = this;
		eventHelper.addEventPayload(function, new VisualMappingFunctionChangeRecord(),
				VisualMappingFunctionChangedEvent.class);
	}

	@Override
	public Map<K, V> getAll() {
		return Collections.unmodifiableMap(attribute2visualMap);
	}
}
