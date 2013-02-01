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

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.mappings.AbstractVisualMappingFunction;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.view.vizmap.mappings.ValueTranslator;

/**
 */
public class PassthroughMappingImpl<K, V> extends AbstractVisualMappingFunction<K, V> implements
		PassthroughMapping<K, V> {

	private final ValueTranslator<K, V> translator;

	/**
	 * dataType is the type of the _attribute_ !! currently we force that to be
	 * the same as the VisualProperty;
	 * FIXME: allow different once? but how to coerce?
	 */
	public PassthroughMappingImpl(final String columnName, final Class<K> columnType, final VisualProperty<V> vp,
			final ValueTranslator<K, V> translator, final CyEventHelper eventHelper) {
		super(columnName, columnType, vp, eventHelper);
		this.translator = translator;
	}

	@Override
	public String toString() {
		return PassthroughMapping.PASSTHROUGH;
	}

	@Override
	@SuppressWarnings("unchecked")
	public V getMappedValue(final CyRow row) {
		if (row == null || !row.isSet(columnName))
			return null;

		K tableValue = null;
		final CyColumn column = row.getTable().getColumn(columnName);
		
		if (column != null) {
			// Always try to find the data type from the current table/column first
			final Class<?> columnClass = column.getType();
	
			try {
				tableValue = (K) row.get(columnName, columnClass);
			} catch (ClassCastException cce) {
				// Invalid
				return null;
			}
			
			Object value = translator.translate(tableValue);
			
			if (value instanceof String)
				value = vp.parseSerializableString((String) value);
			
			if (value != null) {
				try {
					return (V) value;
				} catch (ClassCastException cce) {
				}
			}
		}
		
		return null;
	}
}