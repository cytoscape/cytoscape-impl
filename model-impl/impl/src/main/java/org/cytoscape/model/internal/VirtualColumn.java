/*
 Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.model.internal;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyRow;


final class VirtualColumn {
	private final CyTableImpl sourceTable;
	private final CyColumn sourceColumn;
	private final CyTableImpl targetTable;
	private final CyColumn sourceJoinColumn;
	private final CyColumn targetJoinColumn;

	VirtualColumn(final CyTableImpl sourceTable, final String sourceColumnName,
		      final CyTableImpl targetTable, final String sourceJoinColumnName,
		      final String targetJoinColumnName)
	{
		this.sourceTable      = sourceTable;
		this.sourceColumn     = sourceTable.getColumn(sourceColumnName);
		this.targetTable      = targetTable;
		this.sourceJoinColumn = sourceTable.getColumn(sourceJoinColumnName);
		this.targetJoinColumn = targetTable.getColumn(targetJoinColumnName);
	}

	Object getRawValue(final Object targetKey) {
		final CyRow sourceRow = getSourceRow(targetKey);
		return (sourceRow == null) ? null : sourceRow.getRaw(sourceColumn.getName());
	}

	void setValue(final Object targetKey, final Object value) {
		final CyRow sourceRow = getSourceRow(targetKey);
		if (sourceRow == null)
			throw new IllegalArgumentException("can't set a value for a virtual column.");
		sourceRow.set(sourceColumn.getName(), value);
	}

	Object getValue(final Object targetKey) {
		final CyRow sourceRow = getSourceRow(targetKey);
		if (sourceRow == null)
			return null;

		final Object retValue = sourceRow.get(sourceColumn.getName(),
						      sourceColumn.getType());
		if (retValue == null)
			targetTable.lastInternalError = sourceTable.getLastInternalError();
		return retValue;
	}

	Object getListValue(final Object targetKey) {
		final CyRow sourceRow = getSourceRow(targetKey);
		if (sourceRow == null)
			return null;
		final Object retValue =
			sourceRow.getList(sourceColumn.getName(),
					  sourceColumn.getListElementType());
		if (retValue == null)
			targetTable.lastInternalError = sourceTable.getLastInternalError();
		return retValue;
	}

	private CyRow getSourceRow(final Object targetKey) {
		final Object joinKey = targetTable.getValue(targetKey, targetJoinColumn.getName());
		if (joinKey == null)
			return null;
		return sourceTable.getRowNoCreate(joinKey);
		/*
		final Collection<CyRow> sourceRows =
			sourceTable.getMatchingRows(sourceJoinColumn.getName(),
						    joinKey);
		if (sourceRows.size() != 1)
			return null;

		return sourceRows.iterator().next();
		*/
	}

	Collection<CyRow> getMatchingRows(final Object value) {
		final Collection<CyRow> sourceRows = sourceTable.getMatchingRows(sourceColumn.getName(), value);
		final Set<CyRow> targetRows = new HashSet<CyRow>();
		for (final CyRow sourceRow : sourceRows) {
			final Object targetValue = sourceRow.get(sourceJoinColumn.getName(), sourceJoinColumn.getType());
			if (targetValue != null) {
				final Collection<CyRow> rows = targetTable.getMatchingRows(targetJoinColumn.getName(), targetValue);
				targetRows.addAll(rows);
			}
		}
		return targetRows;
	}

	int countMatchingRows(final Object value) {
		return sourceTable.countMatchingRows(sourceColumn.getName(), value);
	}

	List getColumnValues() {
		final List targetJoinColumnValues =
			targetTable.getColumnValues(targetJoinColumn.getName(),
						    targetJoinColumn.getType());
		List results = new ArrayList();
		for (final Object targetJoinColumnValue : targetJoinColumnValues) {
			final Collection<CyRow> sourceRows =
				sourceTable.getMatchingRows(sourceJoinColumn.getName(),
							    targetJoinColumnValue);
			if (sourceRows.size() == 1) {
				final CyRow sourceRow = sourceRows.iterator().next();
				final Object value =
					sourceRow.get(sourceColumn.getName(),
						      sourceColumn.getType());
				if (value != null)
					results.add(value);
			}
		}

		return results;
	}
}
