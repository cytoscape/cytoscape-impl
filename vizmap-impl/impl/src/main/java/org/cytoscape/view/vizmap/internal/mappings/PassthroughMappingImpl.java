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

import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.mappings.AbstractVisualMappingFunction;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

/**
 */
public class PassthroughMappingImpl<K, V> extends AbstractVisualMappingFunction<K, V> implements
		PassthroughMapping<K, V> {

	/**
	 * dataType is the type of the _attribute_ !! currently we force that to be
	 * the same as the VisualProperty; FIXME: allow different once? but how to
	 * coerce?
	 */
	public PassthroughMappingImpl(final String columnName, final Class<K> columnType, final CyTable table,
			final VisualProperty<V> vp) {
		super(columnName, columnType, table, vp);
	}

	@Override
	public String toString() {
		return PassthroughMapping.PASSTHROUGH;
	}

	@Override
	public void apply(final CyRow row, final View<? extends CyIdentifiable> view) {
		if (row == null || view == null)
			return;

		V value = null;

		if (columnName.equals(CyIdentifiable.SUID)) {
			// Special case: SUID. Value is type Long. This always exists.
			value = (V) view.getModel().getSUID();
		} else if (row.isSet(columnName)) {
			final CyColumn column = row.getTable().getColumn(columnName);
			final Class<?> columnClass = column.getType();

			Object tempValue = null;
			if (columnClass.isAssignableFrom(List.class)) {
				// Special handler for List column. String is only supported
				// one.
				final List<?> list = row.getList(columnName, column.getListElementType());
				final StringBuffer sb = new StringBuffer();

				if (list != null && !list.isEmpty()) {
					for (Object item : list)
						sb.append(item.toString() + "\n");

					sb.deleteCharAt(sb.length() - 1);
				}

				tempValue = sb.toString();
			} else {
				// Regular column.
				// Error check
				final Class<?> actualType = row.getTable().getColumn(columnName).getType();
				if (actualType.equals(columnType))
					tempValue = row.get(columnName, columnType);
				else
					tempValue = row.get(columnName, actualType);
			}

			try {
				value = vp.getRange().getType().cast(tempValue);
			} catch (ClassCastException ex) {
				// Invalid. Try if it's a String
				if (vp.getRange().getType() == String.class)
					value = (V) tempValue.toString();
				else
					value = null;
			}
		}

		if (value != null)
			view.setVisualProperty(vp, value);
	}
}
