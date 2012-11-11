
/*
 Copyright (c) 2008, 2010-2012, The Cytoscape Consortium (www.cytoscape.org)

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.SUIDFactory;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.VirtualColumnInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An abstract table facade class. 
 */
public abstract class AbstractTableFacade implements CyTable {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractTableFacade.class);
	
	private final CyTable actual;
	private final Long suid;
	private final Map<CyRow,CyRow> facadeRows;
	private final Map<CyColumn,CyColumn> facadeColumns;
	private boolean isPublic = true;

	public AbstractTableFacade(final CyTable actual) {
		this.actual = actual;
		this.suid = Long.valueOf(SUIDFactory.getNextSUID()); 
		this.facadeRows = new HashMap<CyRow,CyRow>();
		this.facadeColumns = new HashMap<CyColumn,CyColumn>();
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
		CyColumn ret = facadeColumns.get(actualColumn);
		if ( ret == null ) {
			ret = new ColumnFacade(actualColumn);
			facadeColumns.put(actualColumn,ret);
		}
		
		return ret;
	}

	@Override
	public Collection<CyColumn> getColumns() {
		return getFacadeColumns(actual.getColumns());
	}

	private final Collection<CyColumn> getFacadeColumns(final Collection<CyColumn> columns) {
		List<CyColumn> facadeColumns = new ArrayList<CyColumn>( columns.size() ); 
		for ( CyColumn column : columns )
			facadeColumns.add( getFacadeColumn(column) ); 

		return facadeColumns;
	}

	@Override
	public CyRow getRow(final Object primaryKey) {
		CyRow actualRow = actual.getRow(primaryKey);
		if ( actualRow == null )
			return null;
		
		return getFacadeRow(actualRow);
	}

	private CyRow getFacadeRow(CyRow actualRow) {
		CyRow ret = facadeRows.get(actualRow);
		if ( ret == null ) { 
			ret = new RowFacade(actualRow,this);
			facadeRows.put(actualRow,ret);
		}

		return ret; 
	}
	
	@Override
	public boolean rowExists(Object primaryKey) {
		return actual.rowExists(primaryKey); 
	}
	
	@Override
	public boolean deleteRows(Collection<?> primaryKeys) {
		return actual.deleteRows(primaryKeys); 
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

	private List<CyRow> getFacadeRows(Collection<CyRow> rows) {
		List<CyRow> frows = new ArrayList<CyRow>( rows.size() ); 
		for ( CyRow r : rows )
			frows.add( getFacadeRow(r) ); 

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
		
	}
}