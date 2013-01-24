package org.cytoscape.network.merge.internal.conflict;

/*
 * #%L
 * Cytoscape Merge Impl (network-merge-impl)
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

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.network.merge.internal.util.ColumnType;

public class DefaultAttributeConflictHandler implements AttributeConflictHandler {

	@Override
	public boolean handleIt(final CyIdentifiable to, final CyColumn toColumn,
			final Map<CyIdentifiable, CyColumn> mapFromGOFromAttr) {
		
		if (to == null || toColumn == null || mapFromGOFromAttr == null) {
			throw new java.lang.NullPointerException("All parameters should not be null.");
		}

		final CyTable table = toColumn.getTable();
		final CyRow row = table.getRow(to.getSUID());
		final ColumnType type = ColumnType.getType(toColumn);

		if (type == ColumnType.STRING) {
			final String toValue = row.get(toColumn.getName(), String.class);
			final Set<String> values = new TreeSet<String>();
			values.add(toValue);

			for (Map.Entry<CyIdentifiable, CyColumn> entry : mapFromGOFromAttr.entrySet()) {
				final CyIdentifiable from = entry.getKey();
				final CyColumn fromColumn = entry.getValue();
				final CyRow fromRow = fromColumn.getTable().getRow(from.getSUID());
				
				// TODO figure out which network to be using
				String fromValue = fromRow.get(fromColumn.getName(), String.class);
				if (fromValue != null) {
					values.add(fromValue.toString());
				}
			}

			StringBuilder str = new StringBuilder();
			for (String v : values) {
				str.append(v + ";");
			}

			str.deleteCharAt(str.length() - 1);
			row.set(toColumn.getName(), str.toString());

			return true;
		}

		// FIXME: how about Integer, Double, Boolean?
		return false;
	}
}
