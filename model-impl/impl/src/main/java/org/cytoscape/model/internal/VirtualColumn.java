package org.cytoscape.model.internal;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2013 The Cytoscape Consortium
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


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.VirtualColumnInfo;


final class VirtualColumn implements VirtualColumnInfo {
	private final CyTableImpl sourceTable;
	private final CyColumn sourceColumn;
	private final CyTableImpl targetTable;
	private final CyColumn sourceJoinColumn;
	private final CyColumn targetJoinColumn;
	private final boolean isImmutable;

	VirtualColumn(final CyTableImpl sourceTable, final String sourceColumnName,
		      final CyTableImpl targetTable, final String sourceJoinColumnName,
		      final String targetJoinColumnName, boolean isImmutable)
	{
		this.sourceTable      = sourceTable;
		this.sourceColumn     = sourceTable.getColumn(sourceColumnName);
		this.targetTable      = targetTable;
		this.sourceJoinColumn = sourceTable.getColumn(sourceJoinColumnName);
		this.targetJoinColumn = targetTable.getColumn(targetJoinColumnName);
		this.isImmutable = isImmutable;
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
		return sourceTable.getRow(joinKey);
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
		final Set<CyRow> targetRows = new HashSet<>();
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

	
	@Override
	public boolean isImmutable() {
		return isImmutable;
	}
	
	@Override
	public String getSourceColumn() {
		return sourceColumn.getName();
	}
	
	@Override
	public String getSourceJoinKey() {
		return sourceJoinColumn.getName();
	}
	
	@Override
	public CyTable getSourceTable() {
		return sourceTable;
	}
	
	@Override
	public String getTargetJoinKey() {
		return targetJoinColumn.getName();
	}
	
	@Override
	public boolean isVirtual() {
		return true;
	}
}
