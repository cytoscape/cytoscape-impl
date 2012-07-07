/* File: DefaultAttributeMerger.java

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

package org.cytoscape.network.merge.internal.util;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.network.merge.internal.conflict.AttributeConflictCollector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * 
 */
public class DefaultAttributeMerger implements AttributeMerger {

	protected final AttributeConflictCollector conflictCollector;

	public DefaultAttributeMerger(final AttributeConflictCollector conflictCollector) {
		this.conflictCollector = conflictCollector;
	}

	@Override
	public <T extends CyIdentifiable> void mergeAttribute(final Map<T, CyColumn> mapGOAttr, final T graphObject, final CyColumn column,
			final CyNetwork network) {
		if ((mapGOAttr == null) || (graphObject == null) || (column == null))
			throw new java.lang.IllegalArgumentException("Required parameters cannot be null.");

		final CyRow cyRow = network.getRow(graphObject);
		final ColumnType colType = ColumnType.getType(column);

		for (Map.Entry<T, CyColumn> entryGOAttr : mapGOAttr.entrySet()) {
			final T from = entryGOAttr.getKey();
			final CyColumn fromColumn = entryGOAttr.getValue();
			final CyTable fromTable = fromColumn.getTable();
			final CyRow fromCyRow = fromTable.getRow(from.getSUID());
			final ColumnType fromColType = ColumnType.getType(fromColumn);

			if (colType == ColumnType.STRING) {
				final String fromValue = fromCyRow.get(fromColumn.getName(), String.class);
				final String o2 = cyRow.get(column.getName(), String.class);
				if (o2 == null || o2.length() == 0) { // null or empty attribute
					if(!column.getVirtualColumnInfo().isVirtual())
						cyRow.set(column.getName(), fromValue);
				} else if (fromValue.equals(o2)) { // TODO: neccessary?
					// the same, do nothing
				} else { // attribute conflict
					// add to conflict collector
					conflictCollector.addConflict(from, fromColumn, graphObject, column);
				}
			} else if (!colType.isList()) { // simple type (Integer, Long,
											// Double, Boolean)
				Object o1 = fromCyRow.get(fromColumn.getName(), fromColType.getType());
				if (fromColType != colType) {
					o1 = colType.castService(o1);
				}

				Object o2 = cyRow.get(column.getName(), colType.getType());
				if (o2 == null) {
					cyRow.set(column.getName(), o1);
					// continue;
				} else if (o1.equals(o2)) {
					// continue; // the same, do nothing
				} else { // attribute conflict

					// add to conflict collector
					conflictCollector.addConflict(from, fromColumn, graphObject, column);
					// continue;
				}
			} else { // toattr is list type
				// TODO: use a conflict handler to handle this part?
				ColumnType plainType = colType.toPlain();

				List l2 = cyRow.getList(column.getName(), plainType.getType());
				if (l2 == null) {
					l2 = new ArrayList<Object>();
				}

				if (!fromColType.isList()) { // from plain
					Object o1 = fromCyRow.get(fromColumn.getName(), fromColType.getType());
					if (plainType != fromColType) {
						o1 = plainType.castService(o1);
					}

					if (!l2.contains(o1)) {
						l2.add(o1);
					}

					cyRow.set(column.getName(), l2);
				} else { // from list
					final ColumnType fromPlain = fromColType.toPlain();
					final List<?> list = fromCyRow.getList(fromColumn.getName(), fromPlain.getType());

					for (final Object listValue:list) {
						if(listValue == null)
							continue;
						
						final Object validValue;
						if (plainType != fromColType) {
							validValue = plainType.castService(listValue);
						} else {
							validValue = listValue;
						}
						if (!l2.contains(validValue)) {
							l2.add(validValue);
						}
					}
				}

				cyRow.set(column.getName(), l2);
			}
		}

	}

}
