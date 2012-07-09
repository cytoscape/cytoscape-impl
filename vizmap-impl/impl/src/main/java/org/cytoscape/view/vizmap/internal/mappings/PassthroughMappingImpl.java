/*
 Copyright (c) 2006, 2007, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
			final ValueTranslator<K, V> translator) {
		super(columnName, columnType, vp);
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