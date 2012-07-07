/* File: DefaultAttributeConflictHandler.java

 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

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

package org.cytoscape.network.merge.internal.conflict;

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
