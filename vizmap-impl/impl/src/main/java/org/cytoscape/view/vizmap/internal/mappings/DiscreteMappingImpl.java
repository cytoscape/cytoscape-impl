package org.cytoscape.view.vizmap.internal.mappings;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.events.VisualMappingFunctionChangeRecord;
import org.cytoscape.view.vizmap.events.VisualMappingFunctionChangedEvent;
import org.cytoscape.view.vizmap.mappings.AbstractVisualMappingFunction;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;

/*
 * #%L
 * Cytoscape VizMap Impl (vizmap-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

/**
 * Implements a lookup table mapping data to values of a particular class. The
 * data value is extracted from a bundle of attributes by using a specified data
 * attribute name.
 */
public class DiscreteMappingImpl<K, V> extends AbstractVisualMappingFunction<K, V> implements DiscreteMapping<K, V> {

	/** Contains the actual map elements (sorted) */
	private final Map<K, V> attribute2visualMap;
	
	private final Object lock = new Object();

	public DiscreteMappingImpl(final String attrName, final Class<K> attrType, final VisualProperty<V> vp,
			final CyEventHelper eventHelper) {
		super(attrName, attrType, vp, eventHelper);
		attribute2visualMap = new HashMap<>();
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
						
						synchronized (lock) {
							value = attribute2visualMap.get(key);
						}

						if (value != null)
							break;
					}
				}
			} else {
				K key = row.get(columnName, columnType);

				if (key != null) {
					synchronized (lock) {
						value = attribute2visualMap.get(key);
					}
				}
			}
		}

		return value;
	}

	@Override
	public V getMapValue(K key) {
		synchronized (lock) {
			return attribute2visualMap.get(key);
		}
	}

	@Override
	public <T extends V> void putMapValue(final K key, final T value) {
		boolean changed = false;

		synchronized (lock) {
			boolean containsKey = attribute2visualMap.containsKey(key);
			V oldValue = attribute2visualMap.put(key, value);
			changed = !containsKey || (value == null && oldValue != null) || (value != null && !value.equals(oldValue));
		}

		if (changed)
			eventHelper.addEventPayload(this, new VisualMappingFunctionChangeRecord(),
					VisualMappingFunctionChangedEvent.class);
	}

	@Override
	public <T extends V> void putAll(Map<K, T> map) {
		if (map != null) {
			// Quick check to make sure it's not setting the same entries again
			boolean changed = map.size() > attribute2visualMap.size();
			
			if (!changed) {
				// The new map is not bigger, but may have different keys or values, of course
				synchronized (lock) {
					changed = map.entrySet().stream()
							.anyMatch(me -> {
								V v1 = attribute2visualMap.get(me.getKey());
								T v2 = me.getValue();
								return !attribute2visualMap.containsKey(me.getKey()) ||
										(v1 == null && v2 != null) || (v1 != null && !v1.equals(v2));
							});
				}
			}
			
			if (changed) {
				synchronized (lock) {
					attribute2visualMap.putAll(map);
				}
				
				eventHelper.addEventPayload(this, new VisualMappingFunctionChangeRecord(),
						VisualMappingFunctionChangedEvent.class);
			}
		}
	}

	@Override
	public Map<K, V> getAll() {
		return Collections.unmodifiableMap(attribute2visualMap);
	}

	@Override
	public String toString() {
		return DiscreteMapping.DISCRETE;
	}
}
