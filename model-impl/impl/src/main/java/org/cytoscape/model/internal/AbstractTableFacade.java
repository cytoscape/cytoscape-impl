package org.cytoscape.model.internal;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
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


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.SUIDFactory;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.VirtualColumnInfo;
import org.cytoscape.model.events.RowsDeletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;


/**
 * An abstract table facade class. 
 */
public abstract class AbstractTableFacade implements CyTable {
	
	private static final Logger logger = LoggerFactory.getLogger("org.cytoscape.application.userlog");
	
	private final CyTable actual;
	private final CyEventHelper cyEventHelper;
	private final Long suid;
	private final Map<CyRow,CyRow> facadeRows;
	private final Map<CyColumn,CyColumn> facadeColumns;
	private boolean isPublic = true;
	
	private final Object lock = new Object();

	public AbstractTableFacade(final CyTable actual, final CyEventHelper eventHelper) {
		this.actual = actual;
		this.cyEventHelper = eventHelper;
		this.suid = Long.valueOf(SUIDFactory.getNextSUID()); 
		this.facadeRows = new MapMaker().weakKeys().makeMap();
		this.facadeColumns = new MapMaker().weakKeys().makeMap();
	}

	@Override
	public Long getSUID() {
		return suid;	
	}

	@Override
	public String toString() {
		return actual.toString();
	}

	@Override
	public boolean isPublic() {
		return isPublic;
	}

	@Override
	public void setPublic(final boolean isPublic) {
		this.isPublic = isPublic;
	}

	@Override
	public CyTable.Mutability getMutability() {
		return actual.getMutability();
	}

	@Override
	public String getTitle() {
		return actual.getTitle();
	}

	@Override
	public void setTitle(String title) {
		actual.setTitle(title);
	}

	@Override
	public CyColumn getPrimaryKey() {
		return actual.getPrimaryKey();
	}

	@Override
	public CyColumn getColumn(final String columnName) {
		final CyColumn actualColumn = actual.getColumn(columnName);
		if ( actualColumn == null )
			return null;
		
		return getFacadeColumn(actualColumn);
	}

	private final CyColumn getFacadeColumn(final CyColumn actualColumn) {
		synchronized (lock) {
			CyColumn ret = facadeColumns.get(actualColumn);
			
			if (ret == null) {
				ret = new ColumnFacade(actualColumn);
				facadeColumns.put(actualColumn, ret);
			}
			
			return ret;
		}
	}

	@Override
	public Collection<CyColumn> getColumns() {
		return getFacadeColumns(actual.getColumns());
	}

	private final Collection<CyColumn> getFacadeColumns(final Collection<CyColumn> columns) {
		List<CyColumn> facadeColumns = new ArrayList<>(columns.size());
		
		synchronized (lock) {
			columns.forEach(c -> facadeColumns.add(getFacadeColumn(c)));
		}

		return facadeColumns;
	}

	@Override
	public CyRow getRow(final Object primaryKey) {
		CyRow actualRow = actual.getRow(primaryKey);
		
		if (actualRow == null)
			return null;
		
		return getFacadeRow(actualRow);
	}

	private final CyRow getFacadeRow(final CyRow actualRow) {
		synchronized (lock) {
			CyRow ret = facadeRows.get(actualRow);
			
			if (ret == null) {
				ret = new RowFacade(actualRow, this);
				facadeRows.put(actualRow, ret);
			}
	
			return ret;
		}
	}
	
	@Override
	public boolean rowExists(Object primaryKey) {
		return actual.rowExists(primaryKey); 
	}
	
	@Override
	public boolean deleteRows(Collection<?> primaryKeys) {
		// First, remove the rows from the facade table
		for (Object pk : primaryKeys) {
			CyRow row = actual.getRow(pk);

			synchronized (lock) {
				if (row != null && facadeRows.containsKey(row))
					facadeRows.remove(row);
			}
		}

		// Now delete the rows from the actual table
		boolean changed = actual.deleteRows(primaryKeys); 
		
		if (changed)
			cyEventHelper.fireEvent(new RowsDeletedEvent(this, (Collection<Object>) primaryKeys));
		
		return changed;
	}

	@Override
	public List<CyRow> getAllRows() {
		return getFacadeRows(actual.getAllRows());	
	}
	
	@Override
	public String getLastInternalError() {
		return actual.getLastInternalError();
	}

	@Override
	public Collection<CyRow> getMatchingRows(String columnName, Object value) {
		return getFacadeRows(actual.getMatchingRows(columnName,value));
	}

	private final List<CyRow> getFacadeRows(final Collection<CyRow> rows) {
		final List<CyRow> frows = new ArrayList<>(rows.size());

		for (final CyRow r : rows)
			frows.add(getFacadeRow(r));

		return frows;
	}
	
	@Override
	public int countMatchingRows(String columnName, Object value) {
		return actual.countMatchingRows(columnName, value);
	}

	@Override
	public int getRowCount() {
		return actual.getRowCount();	
	}
	
	@Override
	public SavePolicy getSavePolicy() {
		return SavePolicy.DO_NOT_SAVE;
	}
	
	@Override
	public void setSavePolicy(SavePolicy policy) {
		if (policy != SavePolicy.DO_NOT_SAVE)
			throw new IllegalArgumentException("This table cannot be saved");
	}
	
	@Override
	public void swap(CyTable otherTable) {
		// TODO do we need to do something here?
		actual.swap(otherTable);	
	}

	protected abstract void updateColumnName(String oldName, String newName);
	
	private final class RowFacade implements CyRow {
		
		private final CyRow actualRow;
		private final CyTable table;

		RowFacade(final CyRow actualRow, final CyTable table) {
			this.actualRow = actualRow;
			this.table = table;
		}

		@Override
		public void set(final String attributeName, final Object value) {
			if ( value != null && attributeName != null && attributeName.equals("edges.SUID") ) {
				System.out.println("facade set (" + Long.toString(actualRow.get("SUID",Long.class)) + " - " + table.getTitle() + ") " + attributeName + " " + value.toString());
				Thread.dumpStack();
			}
			actualRow.set(attributeName,value);
		}

		@Override
		public <T> T get(String attributeName, Class<? extends T> c) {
			if ( attributeName != null && c != null && attributeName.equals("edges.SUID")) {
				System.out.println("facade GET (" + Long.toString(actualRow.get("SUID",Long.class)) + " - " + table.getTitle() + ") " + attributeName + " = " + (actualRow.get(attributeName, c)));
				Thread.dumpStack();
			}
			return actualRow.get(attributeName, c);
		}

		@Override
		public <T> T get(String attributeName, Class<? extends T> c, T defValue) {
			if ( attributeName != null && c != null && attributeName.equals("edges.SUID")) {
				System.out.println("facade GET (" + Long.toString(actualRow.get("SUID",Long.class)) + " - " + table.getTitle() + ") " + attributeName + " = " + (actualRow.get(attributeName, c, defValue)).toString());
				Thread.dumpStack();
			}
			return actualRow.get(attributeName, c, defValue);
		}

		@Override
		public <T> List<T> getList(String attributeName, Class<T> c) {
			if ( attributeName != null && c != null && attributeName.equals("edges.SUID")) {
				System.out.println("facade GET LIST (" + Long.toString(actualRow.get("SUID",Long.class)) + " - " + table.getTitle() + ") " + attributeName + " = " + (actualRow.getList(attributeName, c)).toString());
				Thread.dumpStack();
			}
			return actualRow.getList(attributeName, c);
		}

		@Override
		public <T> List<T> getList(String attributeName, Class<T> c, List<T> defValue) {
			if ( attributeName != null && c != null && attributeName.equals("edges.SUID")) {
				System.out.println("facade GET LIST (" + Long.toString(actualRow.get("SUID",Long.class)) + " - " + table.getTitle() + ") " + attributeName + " = " + (actualRow.getList(attributeName, c, defValue)).toString());
				Thread.dumpStack();
			}
			return actualRow.getList( attributeName, c, defValue);
		}

		@Override
		public Object getRaw(String attributeName) {
			return actualRow.getRaw(attributeName);
		}

		@Override
		public boolean isSet(String attributeName) {
			return actualRow.isSet(attributeName);
		}

		@Override
		public Map<String, Object> getAllValues() {
			return actualRow.getAllValues();
		}

		@Override
		public CyTable getTable() {
			return table;
		}

		@Override
		public String toString() {
			return "FACADE of: " + actualRow.toString();
		}
	}
	
	private final class ColumnFacade implements CyColumn {
		
		private final CyColumn actualColumn;

		public ColumnFacade(final CyColumn actualColumn) {
			this.actualColumn = actualColumn;
		}

		@Override
		public String getName() {
			return actualColumn.getName();
		}

		@Override
		public void setName(String newName) {
			updateColumnName(actualColumn.getName(), newName);
		}

		@Override
		public Class<?> getType() {
			return actualColumn.getType();
		}

		@Override
		public Class<?> getListElementType() {
			return actualColumn.getListElementType();
		}

		@Override
		public boolean isPrimaryKey() {
			return actualColumn.isPrimaryKey();
		}

		@Override
		public boolean isImmutable() {
			return actualColumn.isImmutable();
		}

		@Override
		public CyTable getTable() {
			return AbstractTableFacade.this;
		}

		@Override
		public <T> List<T> getValues(Class<? extends T> type) {
			return actualColumn.getValues(type);
		}

		@Override
		public VirtualColumnInfo getVirtualColumnInfo() {
			return actualColumn.getVirtualColumnInfo();
		}

		@Override
		public Object getDefaultValue() {
			return actualColumn.getDefaultValue();
		}

		@Override
		public String toString() {
			return actualColumn.toString();
		}
	}
}
