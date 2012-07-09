/*
  File: DiscreteMappingImpl.java

  Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.view.vizmap.internal.mappings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.VisualProperty;
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
	public DiscreteMappingImpl(final String attrName, final Class<K> attrType, final VisualProperty<V> vp) {
		super(attrName, attrType, vp);
		attribute2visualMap = new HashMap<K, V>();
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
			// ViewColumn will automatically substitute the per-VS or global default, as appropriate
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
				Object key = row.get(columnName, columnType);

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
	}

	@Override
	public <T extends V> void putAll(Map<K, T> map) {
		attribute2visualMap.putAll(map);
	}

	@Override
	public Map<K, V> getAll() {
		return attribute2visualMap;
	}
}
