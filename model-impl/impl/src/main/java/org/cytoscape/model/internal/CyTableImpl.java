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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.cytoscape.equations.Equation;
import org.cytoscape.equations.Interpreter;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.SUIDFactory;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.VirtualColumnInfo;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnNameChangedEvent;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsCreatedEvent;
import org.cytoscape.model.events.RowsDeletedEvent;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.TableAddedEvent;
import org.cytoscape.model.events.TableAddedListener;
import org.cytoscape.model.events.TablePrivacyChangedEvent;
import org.cytoscape.model.events.TableTitleChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class CyTableImpl implements CyTable, TableAddedListener {
	
	private static final Logger logger = LoggerFactory.getLogger(CyTableImpl.class);

	private Set<String> currentlyActiveAttributes;
	private Map<String, Map<Object, Object>> attributes; // Maps column names to (key,value) pairs, where "key" is the primary key.
	private Map<Object, CyRow> rows; // Maps the primary key to CyRow.
	private Map<String, CyColumn> types;
	private ArrayList<CyColumn> colList; //Stores the list of columns in the table
	private ArrayList<CyRow> rowList;    //Stores the list of rows in the table
	private Map<String, Set<CyColumn>> dependents;
	// Caches the normalized names, in order to prevent creating new strings (e.g. name.toLowerCase())
	// every time a column or value is retrieved.
	private Map<String/*name*/, String/*normalized name*/> normalizedColumnNames; 

	// This is not unique and might be changed by user.
	private String title;

	// Visibility value is immutable.
	private boolean pub;

	private boolean isImmutable;

	// Unique ID.
	private final Long suid;

	// name of the primary key column
	private String primaryKey;

	private final CyEventHelper eventHelper;
	private final Interpreter interpreter;
	private final int defaultInitSize;

	String lastInternalError;

	private Map<String, VirtualColumn> virtualColumnMap;

	private SavePolicy savePolicy;
	private boolean fireEvents;
	
	private final Object lock = new Object();
	
	/**
	 * Creates a new CyTableImpl object.
	 */
	public CyTableImpl(	final String title,
						final String primaryKey,
						Class<?> primaryKeyType,
						final boolean pub,
						final boolean isMutable,
						SavePolicy savePolicy,
						final CyEventHelper eventHelper,
						final Interpreter interpreter,
						final int defaultInitSize) {
		this.title = title;
		this.primaryKey = primaryKey;
		this.pub = pub;
		this.isImmutable = !isMutable;
		this.suid = Long.valueOf(SUIDFactory.getNextSUID());
		this.eventHelper = eventHelper;
		this.interpreter = interpreter;
		this.savePolicy = savePolicy;
		this.fireEvents = false;
		this.defaultInitSize = defaultInitSize;

		currentlyActiveAttributes = new HashSet<String>();
		attributes = new HashMap<String, Map<Object, Object>>();
		
		rows = new ConcurrentHashMap<Object, CyRow>(defaultInitSize, 0.5f, 2);
		types = new ConcurrentHashMap<String, CyColumn>(16, 0.75f, 2);
		colList = new ArrayList<CyColumn>();
		rowList = new ArrayList<CyRow>();
		
		dependents = new HashMap<String, Set<CyColumn>>();
		normalizedColumnNames = new HashMap<String, String>();
		
		VirtualColumnInfo virtualInfo = NonVirtualColumnInfo.create(true);
		final String normalizedPKName = normalizeColumnName(primaryKey);
		
		// Create the primary key column.  Do this explicitly so that we don't fire an event.
		types.put(normalizedPKName, new CyColumnImpl(this, primaryKey, primaryKeyType,
						                             /* listElementType = */ null,
						                             virtualInfo,
						                             /* isPrimaryKey = */ true,
						                             /* isImmutable = */ true,
						                             null));
		colList.add(getColumn(normalizedPKName));
		// Using a ConcurrentHashMap for attributes because this speeds up initial access times
		// for getMatchingRows()
		attributes.put(normalizedPKName, new HashMap<Object, Object>(defaultInitSize));

		virtualColumnMap = new HashMap<String, VirtualColumn>();
	}


	@Override
	public void swap(final CyTable otherTable) {
		synchronized (lock) {
			final CyTableImpl other = (CyTableImpl)otherTable;
	
			final Set<String> tempCurrentlyActiveAttributes = currentlyActiveAttributes;
			currentlyActiveAttributes = other.currentlyActiveAttributes;
			other.currentlyActiveAttributes = tempCurrentlyActiveAttributes;
	
			final Map<String, Map<Object, Object>> tempAttributes = attributes;
			attributes = other.attributes;
			other.attributes = tempAttributes;
	
			final Map<Object, CyRow> tempRows = rows;
			rows = other.rows;
			other.rows = tempRows;
	
			final Map<String, CyColumn> tempTypes = types;
			types = other.types;
			other.types = tempTypes;
			
			final ArrayList<CyColumn> tempListCol = colList;
			colList = other.colList;
			other.colList = tempListCol;
			
			final ArrayList<CyRow> tempListRow = rowList;
			rowList = other.rowList;
			other.rowList = tempListRow;
			
			final Map<String, String> tempNormalizedColNames = normalizedColumnNames;
			normalizedColumnNames = other.normalizedColumnNames;
			other.normalizedColumnNames = tempNormalizedColNames;
	
			final String tempTitle = title;
			title = other.title;
			other.title = tempTitle;
	
			final boolean tempPub = pub;
			pub = other.pub;
			other.pub = tempPub;
	
			final boolean tempIsImmutable = isImmutable;
			isImmutable = other.isImmutable;
			other.isImmutable = tempIsImmutable;
	
			final String tempPrimaryKey = primaryKey;
			primaryKey = other.primaryKey;
			other.primaryKey = tempPrimaryKey;
	
			final String tempLastInternalError= lastInternalError;
			lastInternalError = other.lastInternalError;
			other.lastInternalError = tempLastInternalError;
	
			final Map<String, VirtualColumn> tempVirtualColumnMap = virtualColumnMap;
			virtualColumnMap = other.virtualColumnMap;
			other.virtualColumnMap = tempVirtualColumnMap;
	
			final Map<String, Set<CyColumn>> tempDependents = dependents;
			dependents = other.dependents;
			other.dependents = tempDependents;
			
			final SavePolicy tempSavePolicy = savePolicy;
			savePolicy = other.savePolicy;
			other.savePolicy = tempSavePolicy;
		}
	}

	void updateColumnName(final String oldColumnName, final String newColumnName) {
		
		if (oldColumnName.equalsIgnoreCase(newColumnName))
			return;

		for(final String curColumnName : types.keySet())
			if (curColumnName.equalsIgnoreCase(newColumnName))
				throw new IllegalArgumentException("column already exists with name: '"
					   + curColumnName + "' with type: "
					   + types.get(curColumnName).getType());
		
		synchronized(lock) {
			if (currentlyActiveAttributes.remove(oldColumnName)) {
				currentlyActiveAttributes.add(newColumnName);
			}

			String normalizedOldColName = normalizeColumnName(oldColumnName);
			String normalizedNewColName = normalizeColumnName(newColumnName);
			final Map<Object, Object> keyValuePairs = attributes.get(normalizedOldColName);
			if (keyValuePairs != null) {
				attributes.remove(normalizedOldColName);
				attributes.put(normalizedNewColName, keyValuePairs);
			}

			final CyColumn column = types.get(normalizedOldColName);
			types.put(normalizedNewColName, column);
			types.remove( normalizedOldColName);
			
			final VirtualColumn virtualColumn = virtualColumnMap.get(normalizedOldColName);
			if (virtualColumn != null) {
				virtualColumnMap.remove(normalizedOldColName);
				virtualColumnMap.put(normalizedNewColName, virtualColumn);
			}
			
			final Set<CyColumn> columnDependents = dependents.get(normalizedOldColName);
			if (columnDependents != null) {
				dependents.remove(normalizedOldColName);
				dependents.put(normalizedNewColName, columnDependents);
			}
		}

		eventHelper.fireEvent(new ColumnNameChangedEvent(this, oldColumnName, newColumnName));
	}
	
	boolean eventsEnabled() {
		return fireEvents;
	}
	
	@Override
	public Long getSUID() {
		return suid;
	}

	@Override
	public boolean isPublic() {
		return pub;
	}

	@Override
	public void setPublic(boolean isPublic) {

		if(pub != isPublic){
			pub = isPublic;
			if( fireEvents)
				eventHelper.fireEvent(new TablePrivacyChangedEvent(this));
		}
	}
	
	@Override
	public CyTable.Mutability getMutability() {
		synchronized (lock) {
			if (isImmutable)
				return Mutability.PERMANENTLY_IMMUTABLE;
			else if (getDependentCount() == 0)
				return Mutability.MUTABLE;
			else
				return Mutability.IMMUTABLE_DUE_TO_VIRT_COLUMN_REFERENCES;
		}
	}

	private final int getDependentCount() {
		int count = 0;
		for (final Set<CyColumn> columnDependents : dependents.values()) {
			count += columnDependents.size();
		}
		return count;
	}


	@Override
	public String getTitle() {
		return title;
	}
	
	@Override
	public String toString() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		boolean valueChanged;
		String oldTitle = null;
		synchronized (lock) {
			valueChanged = !this.title.equals(title);
			if (valueChanged) {
				oldTitle = this.title;
				this.title = title;
			}
		}
		if (valueChanged && fireEvents) {
			if(fireEvents)
				eventHelper.fireEvent(new TableTitleChangedEvent(this, oldTitle));
		}
	}

	@Override
	public Collection<CyColumn> getColumns() {
		return colList;
	}

	@Override
	public CyColumn getColumn(final String columnName) {
		return types.get(normalizeColumnName(columnName));
	}

	@Override
	public CyColumn getPrimaryKey() {
		return types.get(normalizeColumnName(primaryKey));
	}

	@Override
	public int getRowCount() {
		return rows.size();
	}

	@Override
	public void deleteColumn(final String columnName) {
		synchronized(lock) {
			if (columnName == null)
				throw new NullPointerException("\"columnName\" must not be null.");

			final String normalizedColName = normalizeColumnName(columnName);
			final CyColumn column = types.get(normalizedColName);
			if (column == null)
				return;

			if (column.isImmutable())
				throw new IllegalArgumentException("cannot delete immutable column \"" + columnName + "\".");
				
			final VirtualColumn virtColumn = virtualColumnMap.get(normalizedColName);
			if (attributes.containsKey(normalizedColName) || virtColumn != null) {
				if (virtColumn != null) {
					final CyColumn cyColumn = types.get(normalizedColName);
					virtualColumnMap.remove(normalizedColName);
					attributes.remove(normalizedColName);
					types.remove(normalizedColName);
					colList.remove(cyColumn);
					VirtualColumnInfo info = cyColumn.getVirtualColumnInfo();
					((CyTableImpl) info.getSourceTable()).removeDependent(info.getSourceColumn(), cyColumn);
				} else {
					attributes.remove(normalizedColName);
					colList.remove(types.get(normalizedColName));
					types.remove(normalizedColName);
				}
			}
		}

		// This event must be synchronous!
		eventHelper.fireEvent(new ColumnDeletedEvent(this, columnName));
	}

	private void addDependent(String columnName, CyColumn joinedColumn) {
		String normalizedName = normalizeColumnName(columnName);
		Set<CyColumn> set = dependents.get(normalizedName);
		if (set == null) {
			set = new HashSet<CyColumn>();
			dependents.put(normalizedName, set);
		}
		set.add(joinedColumn);
	}
	
	private void removeDependent(String columnName, CyColumn joinedColumn) {
		String normalizedName = normalizeColumnName(columnName);
		Set<CyColumn> set = dependents.get(normalizedName);
		if (set == null) {
			return;
		}
		set.remove(joinedColumn);
	}
	
	@Override
	public <T> void createColumn(final String columnName, final Class<? extends T> type, final boolean isImmutable) {
		createColumn(columnName,type,isImmutable,null);
	}

	@Override
	public <T> void createColumn(final String columnName, final Class<? extends T> type,
				     final boolean isImmutable, final T defaultValue) {
		synchronized(lock) {
			if (columnName == null)
				throw new NullPointerException("column name is null");
			
			final String normalizedColName = normalizeColumnName(columnName);
			
			if (types.containsKey(normalizedColName))
				throw new IllegalArgumentException("column already exists with name: '" + columnName
						+ "' with type: " + types.get(normalizedColName).getType());
			
			if (type == null)
				throw new NullPointerException("type is null");

			if (type == List.class)
				throw new IllegalArgumentException(
						"use createListColumn() to create List columns instead of createColumn for column '"
						+ columnName + "'.");

			checkClass(type);

			VirtualColumnInfo virtualInfo = NonVirtualColumnInfo.create(isImmutable);
			types.put(normalizedColName, new CyColumnImpl(this, columnName, type,
							                              /* listElementType = */ null,
							                              virtualInfo ,
							                              /* isPrimaryKey = */ false,
							                              isImmutable,
							                              defaultValue));
			attributes.put(normalizedColName, new HashMap<Object, Object>(defaultInitSize));
			colList.add(types.get(normalizedColName));
		}
		
		eventHelper.fireEvent(new ColumnCreatedEvent(this, columnName));
	}
	

	@Override
	public <T> void createListColumn(final String columnName, final Class<T> listElementType,
					 final boolean isImmutable) {
		createListColumn(columnName,listElementType,isImmutable,null);
	}

	@Override
	public <T> void createListColumn(final String columnName, final Class<T> listElementType,
					 final boolean isImmutable, final List<T> defaultValue) {
		synchronized(lock) {
			if (columnName == null)
				throw new NullPointerException("column name is null");

			final String normalizedColName = normalizeColumnName(columnName);
			
			if (types.containsKey(normalizedColName))
				throw new IllegalArgumentException("column already exists with name: '"
					   + columnName + "' with type: "
					   + types.get(normalizedColName).getType());
			
			if (listElementType == null)
				throw new NullPointerException("listElementType is null");

			checkClass(listElementType);

			VirtualColumnInfo virtualInfo = NonVirtualColumnInfo.create(isImmutable);
			types.put(normalizedColName, new CyColumnImpl(this, columnName, List.class,
							       listElementType,
							       virtualInfo,
							       /* isPrimaryKey = */ false,
							       isImmutable,
								   defaultValue));
			attributes.put(normalizedColName, new HashMap<Object, Object>(defaultInitSize));
			colList.add(types.get(normalizedColName));
		}

		eventHelper.fireEvent(new ColumnCreatedEvent(this, columnName));
	}

	<T> List<T> getColumnValues(final String columnName, final Class<? extends T> type) {
		if (columnName == null)
			throw new NullPointerException("column name is null.");

		if (type == null)
			throw new NullPointerException("column type is null.");

		synchronized (lock) {
			List<T> l = new ArrayList<>();
			for(Object key : rows.keySet()) {
				l.add((T)getValue(key, columnName, type));
			}
			return l;
		}
	}

	// Used in virtual columns so that we don't create new rows in tables
	// that are only being referenced. We expect it to return null.
	CyRow getRowNoCreate(final Object key) {
		checkKey(key);
		return rows.get(key);
	}

	@Override
	public CyRow getRow(final Object key) {
		checkKey(key);

		CyRow row = rows.get(key);
		if (row != null)
			return row;

		row = new InternalRow(key);
		rows.put(key, row);
		rowList.add(row);

		if (fireEvents)
			eventHelper.addEventPayload((CyTable) this, (Object) key, RowsCreatedEvent.class);
		return row;
	}

	@Override
	public boolean rowExists(final Object primaryKey) {
		return (primaryKey != null && rows.containsKey(primaryKey));	
	}


	@Override
	public String getLastInternalError() {
		return lastInternalError;
	}

	private final void checkKey(final Object suid) {
		if (suid == null)
			throw new NullPointerException("key is null");

		if (!types.get(normalizeColumnName(primaryKey)).getType().isAssignableFrom(suid.getClass()))
			throw new IllegalArgumentException("key of type " + suid.getClass()
							   + " and not the expected: "
							   + types.get(normalizeColumnName(primaryKey)).getType().getName());
	}

	@Override
	public List<CyRow> getAllRows() {
		return rowList;
	}

	@Override
	public Collection<CyRow> getMatchingRows(final String columnName, final Object value) {
		synchronized (lock) {
			final String normalizedColName = normalizeColumnName(columnName);
			final VirtualColumn virtColumn = virtualColumnMap.get(normalizedColName);
			
			if (virtColumn != null)
				return virtColumn.getMatchingRows(value);
	
			if (normalizedColName.equals(normalizeColumnName(primaryKey))) {
				final ArrayList<CyRow> matchingRows = new ArrayList<CyRow>(1);
				final CyRow matchingRow = rows.get(value);
				if (matchingRow != null)
					matchingRows.add(matchingRow);
				return matchingRows;
			}
			
			final Map<Object, Object> keyToValueMap = attributes.get(normalizedColName);
			final ArrayList<CyRow> matchingRows = new ArrayList<CyRow>(rows.size());
			
			for(Entry<Object, Object> entry: keyToValueMap.entrySet()) {
				if(entry.getValue().equals(value))
					matchingRows.add(rows.get(entry.getKey()));
			}
	
			return matchingRows;
		}
	}

	@Override
	public int countMatchingRows(final String columnName, final Object value) {
		synchronized (lock) {
			final String normalizedColName = normalizeColumnName(columnName);
			final VirtualColumn virtColumn = virtualColumnMap.get(normalizedColName);
			
			if (virtColumn != null)
				return virtColumn.countMatchingRows(value);
			
			final Map<Object, Object> keyToValueMap = attributes.get(normalizedColName);
			return Collections.frequency(keyToValueMap.values(), value);
		}
	}

	private final void setX(final Object key, final String columnName, final Object value) {
		if (columnName == null)
			throw new NullPointerException("columnName must not be null.");
		if (value == null)
			throw new NullPointerException("value must not be null.");
		
		final Object newValue;
		final Object newRawValue;
		final VirtualColumn virtColumn;
		
		synchronized(lock) {
			final String normalizedColName = normalizeColumnName(columnName);
			
			if (types.get(normalizedColName) == null)
				throw new IllegalArgumentException("column: '" + columnName + "' does not yet exist.");

			final Class<?> columnType = types.get(normalizedColName).getType();
			if (columnType == List.class) {
				setListX(key, columnName, value);
				return;
			}

			if (!(value instanceof Equation))
				checkType(value);

			virtColumn = virtualColumnMap.get(normalizedColName);
			if (virtColumn != null) {
				virtColumn.setValue(key, value);
				newValue = virtColumn.getValue(key);
				newRawValue = virtColumn.getRawValue(key);
			} else {
				Map<Object, Object> keyToValueMap = attributes.get(normalizedColName);

				if (!columnType.isAssignableFrom(value.getClass())
				    && !EqnSupport.scalarEquationIsCompatible(value, columnType))
					throw new IllegalArgumentException("value of \"" + columnName + "\" is not of type " + columnType);

				if (value instanceof Equation) {
					newRawValue = value;
					final Equation equation = (Equation)value;
					// TODO this is an implicit addRow - not sure if we want to refactor this or not
					keyToValueMap.put(key, equation);

					final StringBuilder errorMsg = new StringBuilder();
					newValue = EqnSupport.evalEquation(equation, key, interpreter,
									   currentlyActiveAttributes, columnName,
									   errorMsg, this);
					lastInternalError = errorMsg.toString();
					if (newValue == null)
						logger.warn("attempted premature evaluation evaluation for " + equation);
				} else {
					// TODO this is an implicit addRow - not sure if we want to refactor this or not
					newRawValue = newValue = columnType.cast(value);
					keyToValueMap.put(key, newValue);
				}
			}
		}

		if (fireEvents && virtColumn == null) {
			// Fire an event for each table in the virtual column chain
			fireVirtualColumnRowSetEvent(this, key, columnName, newValue, newRawValue, Collections.newSetFromMap(new IdentityHashMap<VirtualColumnInfo, Boolean>()));
		}
	}

	private void fireVirtualColumnRowSetEvent(CyTableImpl table, Object key, String columnName, Object newValue, Object newRawValue, Set<VirtualColumnInfo> seen) {
		// Fire an event for this table
		CyRow row = table.getRowNoCreate(key);
		if (row == null) {
			return;
		}
		eventHelper.addEventPayload((CyTable) table, new RowSetRecord(row, columnName, newValue, newRawValue), RowsSetEvent.class);
		
		// ...then fire events for all dependents
		Set<CyColumn> columnDependents;
		synchronized (lock) {
			String normalizedColumnName = normalizeColumnName(columnName);
			columnDependents = dependents.get(normalizedColumnName);
			if (columnDependents == null) {
				return;
			}
		}
		
		for (CyColumn dependent : columnDependents) {
			VirtualColumnInfo info = dependent.getVirtualColumnInfo();
			if (seen.contains(info)) {
				continue;
			}
			seen.add(info);
			CyTableImpl table2 = (CyTableImpl) dependent.getTable();
			String targetJoinKey = info.getTargetJoinKey();
			if (targetJoinKey.equals(table2.getPrimaryKey().getName())) {
				fireVirtualColumnRowSetEvent(table2, key, dependent.getName(), newValue, newRawValue, seen);
			} else {
				String normalizedTargetJoinKey = table2.normalizeColumnName(targetJoinKey);		
				Map<Object, Object> keyToValueMap = table2.attributes.get(normalizedTargetJoinKey);
				if(keyToValueMap != null) {
					for (Object key2: keyToValueMap.keySet()) {
						if (keyToValueMap.get(key2).equals(key)) {
							fireVirtualColumnRowSetEvent(table2, key2, dependent.getName(), newValue, newRawValue, seen);
						}
					}
				}
			}
		}
	}

	private final void setListX(final Object key, final String columnName, final Object value) {
		Object newValue;
		final Object rawValue;
		
		synchronized(lock) {
			final String normalizedColName = normalizeColumnName(columnName);
			final CyColumn column = types.get(normalizedColName);
			CyRow row = rows.get(key);
			final Class<?> type = column.getListElementType();
			
			if (value instanceof CyListImpl) {
				rawValue = value;
			} else if (value instanceof List) {
				final List list = (List)value;
				if (!list.isEmpty())
					checkType(list.get(0));
				rawValue = new CyListImpl(type, new ArrayList(list), eventHelper, row, column, this);
			} else if (!(value instanceof Equation)) {
				throw new IllegalArgumentException("value is a " + value.getClass().getName()
								   + " and not a List for column '"
								   + columnName + "'.");
			} else if (!EqnSupport.listEquationIsCompatible((Equation)value, type)) {
				throw new IllegalArgumentException(
								   "value is not a List equation of a compatible type for column '"
								   + columnName + "'.");
			} else {
				rawValue = value;
			}

			final VirtualColumn virtColumn = virtualColumnMap.get(normalizedColName);
			
			if (virtColumn != null) {
				virtColumn.setValue(key, rawValue);
				newValue = virtColumn.getListValue(key);
			} else {
				Map<Object, Object> keyToValueMap = attributes.get(normalizedColName);

				// TODO this is an implicit addRow - not sure if we want to refactor this or not
				keyToValueMap.put(key, rawValue);
				if (rawValue instanceof Equation) {
					final StringBuilder errorMsg = new StringBuilder();
					newValue = EqnSupport.evalEquation((Equation)rawValue, suid, interpreter,
									   currentlyActiveAttributes, columnName,
									   errorMsg, this);
					lastInternalError = errorMsg.toString();
				} else {
					newValue = rawValue;
				}
			}
		}

		if (fireEvents)
			eventHelper.addEventPayload((CyTable)this,
			                            new RowSetRecord(getRow(key),columnName,newValue, rawValue),
			                            RowsSetEvent.class);
	}

	private void unSetX(final Object key, final String columnName) {
		synchronized(lock) {
			final String normalizedColName = normalizeColumnName(columnName);
			final VirtualColumn virtColumn = virtualColumnMap.get(normalizedColName);
			
			if (virtColumn != null)
				virtColumn.setValue(key, null);
			else {
				final Map<Object, Object> keyToValueMap = attributes.get(normalizedColName);
				if (!types.containsKey(normalizedColName) || keyToValueMap == null)
					throw new IllegalArgumentException("column: '" + columnName + "' does not yet exist.");

				final Object value = keyToValueMap.get(key);
				if (value == null)
					return;

				keyToValueMap.remove(key);
			}
		}

		if (fireEvents)
			eventHelper.addEventPayload((CyTable)this,
			                            new RowSetRecord(getRow(key), columnName, null, null),
			                            RowsSetEvent.class);
	}

	Object getValueOrEquation(final Object key, final String columnName) {
		synchronized (lock) {
			final String normalizedColName = normalizeColumnName(columnName);
			return getValueOrEquation(key, columnName, virtualColumnMap.get(normalizedColName));
		}
	}

	private final Object getValueOrEquation(final Object key, final String columnName, final VirtualColumn virtColumn) {
		synchronized (lock) {
			final String normalizedColName = normalizeColumnName(columnName);
			
			if (primaryKey.equalsIgnoreCase(normalizedColName))
				return key;
	
			if (virtColumn != null)
				return virtColumn.getRawValue(key);
			
			final Map<Object, Object> keyToValueMap = attributes.get(normalizedColName);
			if (keyToValueMap == null)
				return null;
			
			return keyToValueMap.get(key);
		}
	}

	private final <T> T getX(final Object key, final String columnName, final Class<? extends T> type, final T defaultValue) {
		if (type.isAssignableFrom(List.class))
			logger.debug("risky use of get() instead of getList() for retrieving list");
		lastInternalError = null;

		Object value;
		synchronized (lock) {
			value = getValue(key, columnName, type);
			if (value == null)
				return getDefaultValue(columnName, defaultValue);
		}
		return type.cast(value);
	}

	Object getValue(Object key, String columnName) {
		CyColumn column = getColumn(columnName);
		Class<?> type = column == null ? null : column.getType();
		return getValue(key, columnName, type);
	}

	private final Object getValue(final Object key, final String columnName, final Class<?> type) {
		final String normalizedColName = normalizeColumnName(columnName);
		final VirtualColumn virtColumn = virtualColumnMap.get(normalizedColName);
		final Object vl = getValueOrEquation(key, columnName, virtColumn);
		if (virtColumn != null && vl == null)
			return virtColumn.getValue(key);

		if (vl == null)
			return null;

		if (vl instanceof Equation) {
			final StringBuilder errorMsg = new StringBuilder();
			final Object value =
				EqnSupport.evalEquation((Equation)vl, key, interpreter,
							currentlyActiveAttributes, columnName,
							errorMsg, this);
			lastInternalError = errorMsg.toString();
			if ( type == null )
				return value;
			else if(value != null)
				return EqnSupport.convertEqnResultToColumnType(type, value);
			else 
				return null;
		} else
			return vl;
	}

	private final <T> T getDefaultValue(final String columnName, final T defaultValue) {
		if ( defaultValue == null ) {
			final CyColumn column = this.getColumn(columnName);
			if ( column == null ) {
				return null;
			} else
				return (T)(column.getDefaultValue());
		} else {
			return defaultValue;
		}
	}


	private <T> List<T> getListX(final Object key, final String columnName,
							   final Class<? extends T> listElementType, final List<T> defaultValue) {
		synchronized (lock) {
			final String normalizedColName = normalizeColumnName(columnName);
			CyColumn type = types.get(normalizedColName);
			
			if (type == null) {
				logger.warn("'" + columnName + "' does not yet exist.");
				return defaultValue;
			}
	
			final Class<?> expectedListElementType = type.getListElementType();
			if (expectedListElementType == null) {
				throw new IllegalArgumentException("'" + columnName + "' is not a List.");
			}
	
			if (expectedListElementType != listElementType) {
				throw new IllegalArgumentException("invalid list element type for column '"
				             + columnName + ", found: " + listElementType.getName()
				             + ", expected: " + expectedListElementType.getName()
				             + ".");
			}
	
			lastInternalError = null;
	
			final VirtualColumn virtColumn = virtualColumnMap.get(normalizedColName);
			final Object vl = getValueOrEquation(key, columnName, virtColumn);
			if (virtColumn != null && vl == null)
				return (List<T>)virtColumn.getListValue(key);
	
			if (vl == null)
				return getDefaultValue(columnName,defaultValue);
	
			if (vl instanceof Equation) {
				final StringBuilder errorMsg = new StringBuilder();
				final Object result =
					EqnSupport.evalEquation((Equation)vl, key, interpreter,
								currentlyActiveAttributes, columnName,
								errorMsg, this);
				lastInternalError = errorMsg.toString();
				return (List)result;
			} else
				return (List)vl;
		}
	}

	private <T> boolean isSetX(final Object key, final String columnName) {
		synchronized (lock) {
			final String normalizedColName = normalizeColumnName(columnName);
			
			if (primaryKey.equalsIgnoreCase(normalizedColName))
				return true;
			
			final VirtualColumn virtColumn = virtualColumnMap.get(normalizedColName);
			
			if (virtColumn != null) {
				return virtColumn.getRawValue(key) != null;
			} else {
				final Map<Object, Object> keyToValueMap = attributes.get(normalizedColName);
				
				return keyToValueMap != null && keyToValueMap.get(key) != null;
			}
		}
	}

	private void checkClass(Class<?> c) {
		if ( c == Integer.class || 
		     c == Long.class || 
			 c == Double.class || 
			 c == String.class || 
			 c == Boolean.class )
			return;
		else
			throw new IllegalArgumentException("invalid class: " + c.getName());
	}

	private final void checkType(final Object o) {
		if (o instanceof String)
			return;
		else if (o instanceof Integer)
			return;
		else if (o instanceof Boolean)
			return;
		else if (o instanceof Double)
			return;
		else if (o instanceof Long)
			return;
		else
			throw new IllegalArgumentException("invalid type: " + o.getClass().toString());
	}

	@Override
	public final String addVirtualColumn(final String virtualColumnName, final String sourceColumnName,
					     final CyTable sourceTable, final String targetJoinKeyName, final boolean isImmutable) {
		if (virtualColumnName == null)
			throw new NullPointerException("\"virtualColumn\" argument must never be null.");
		if (sourceColumnName == null)
			throw new NullPointerException("\"sourceColumn\" argument must never be null.");
		if (sourceTable == null)
			throw new NullPointerException("\"sourceTable\" argument must never be null.");
		if (targetJoinKeyName == null)
			throw new NullPointerException("\"targetJoinKey\" argument must never be null.");

		String targetName = "failed to create column"; 

		synchronized(lock) {
			final String normalizedColName = normalizeColumnName(virtualColumnName);
			if (types.containsKey(normalizedColName))
				throw new IllegalArgumentException("column already exists with name: '" + virtualColumnName
						+ "' with type: " + types.get(normalizedColName).getType());
			
			final CyColumn sourceColumn = sourceTable.getColumn(normalizeColumnName(sourceColumnName));
			if (sourceColumn == null)
				throw new IllegalArgumentException("\""+sourceColumnName+"\" is not a column in source table.");

			final CyColumn targetJoinKey = this.getColumn(targetJoinKeyName);
			if (targetJoinKey == null)
				throw new IllegalArgumentException("\""+ targetJoinKeyName +"\" is not a known column in this table.");

			final CyColumn sourceJoinKey = sourceTable.getPrimaryKey();
			if (sourceJoinKey.getType() != targetJoinKey.getType())
				throw new IllegalArgumentException("\""+sourceJoinKey.getName()+"\" has a different type from \""+targetJoinKeyName+"\".");

			VirtualColumn virtualColumn = new VirtualColumn((CyTableImpl)sourceTable, sourceColumnName, this,
                    sourceTable.getPrimaryKey().getName(), 
                    targetJoinKeyName, isImmutable);
			targetName = virtualColumnName;

			final CyColumn targetColumn = new CyColumnImpl(this, targetName, sourceColumn.getType(),
			                                               sourceColumn.getListElementType(), virtualColumn,
			                                               /* isPrimaryKey = */ false, isImmutable, sourceColumn.getDefaultValue());
			
			((CyTableImpl) sourceTable).addDependent(sourceColumnName, targetColumn);

			final String normalizedTargetName = normalizeColumnName(targetName);
			types.put(normalizedTargetName, targetColumn);
			attributes.put(normalizedTargetName, new HashMap<Object, Object>(defaultInitSize));
			virtualColumnMap.put(normalizedTargetName, virtualColumn);
			colList.add(types.get(normalizedTargetName));
		}

		eventHelper.fireEvent(new ColumnCreatedEvent(this, targetName));
		return targetName;
	}

	// Warning: This method is only to be used by CyTableManagerImpl!!!  That's also the reason
	//          why no ColumnDeletedEvent events are being fired by it!  Also this deletes
	//          (intentionally!) immutable columns!
	void removeAllVirtColumns() {
		synchronized (lock) {
			if (getDependentCount() > 0)
				return;
	
			for (final String columnName : virtualColumnMap.keySet()) {
				final CyColumn column = types.get(columnName);
				types.remove(columnName);
				colList.remove(column);
				VirtualColumnInfo info = column.getVirtualColumnInfo();
				((CyTableImpl) info.getSourceTable()).removeDependent(info.getSourceColumn(), column);
			}
			virtualColumnMap.clear();
		}
	}

	@Override
	public final void addVirtualColumns(final CyTable sourceTable, final String targetJoinKeyName,
			final boolean isImmutable) {
		synchronized (lock) {
			if (sourceTable == null)
				throw new NullPointerException("\"sourceTable\" argument must never be null.");
			if (targetJoinKeyName == null)
				throw new NullPointerException("\"targetJoinKeyName\" argument must never be null.");
	
			final CyColumn targetJoinKey = this.getColumn(targetJoinKeyName);
			if (targetJoinKey == null)
				throw new IllegalArgumentException("\"" + targetJoinKeyName
								   + "\" is not a known column in this table (" + getTitle() +").");
	
			final CyColumn sourceJoinKey = sourceTable.getPrimaryKey();
			if (sourceJoinKey.getType() != targetJoinKey.getType())
				throw new IllegalArgumentException("\"" + sourceJoinKey.getName()
								   + "\" has a different type from \""
								   + targetJoinKeyName + "\".");
	
			final Collection<CyColumn> columns = sourceTable.getColumns();
			for(final CyColumn column: columns) {
				// skip the primary key
				if (column == sourceTable.getPrimaryKey())
					continue;
				
				final String normalizedColName = normalizeColumnName(column.getName());
				if (types.containsKey(normalizedColName))
					throw new IllegalArgumentException("column already exists with name: '" + column.getName()
							+ "' with type: " + types.get(normalizedColName).getType());
			}
			for (final CyColumn column : columns) {
				// skip the primary key
				if (column == sourceTable.getPrimaryKey())
					continue;
				final String columnName = column.getName();
	
				addVirtualColumn(columnName, columnName, sourceTable, targetJoinKeyName, isImmutable);
			}
		}
	}

	@Override
	public SavePolicy getSavePolicy() {
		return savePolicy;
	}
	
	@Override
	public void setSavePolicy(SavePolicy policy) {
		savePolicy = policy;
	}

	@Override
	public void handleEvent(final TableAddedEvent e) {
		if (e.getTable() == this)
			fireEvents = true;
	}
	
	/**
	 * Normalizes the column names to be case insensitive.
	 */
	private final String normalizeColumnName(final String initialName) {
		String name = normalizedColumnNames.get(initialName);
		if (name == null) {
			name = initialName.toLowerCase();
			// cache the normalized name, to avoid creating new strings in future accesses.
			normalizedColumnNames.put(initialName, name);
		}
		return name;
	}
	
	@Override
	public boolean deleteRows(final Collection<?> primaryKeys) {
		boolean changed = false;
		synchronized(lock) {
			// collect the attribute maps for the columns, faster to normalize column names outside the main loop
			Collection<CyColumn> columns = getColumns();
			List<Map<?,?>> attributeMaps = new ArrayList<>(columns.size());
			for(CyColumn col : columns) {
				final String normalizedColName = normalizeColumnName(col.getName());
				final Map<?,?> keyToValueMap = attributes.get(normalizedColName);
				if(keyToValueMap != null) {
					attributeMaps.add(keyToValueMap);
				}
			}
			
			// batch remove from rowList for performance
			Set<CyRow> rowsToRemoveFromList = new HashSet<>();
			
			// main loop
			for (Object key : primaryKeys) {
				checkKey(key);
				CyRow row = rows.remove(key);
				if (row != null) {
					rowsToRemoveFromList.add(row);
					for(Map<?,?> keyToValueMap : attributeMaps) {
						keyToValueMap.remove(key);
					}
					changed = true;
				}
			}
			rowList.removeAll(rowsToRemoveFromList);
		}
		if(changed)
			eventHelper.fireEvent(new RowsDeletedEvent( this,  (Collection<Object>) primaryKeys));
		
		return changed;
	}
	
	private final class InternalRow implements CyRow {
		private final Object key;

		InternalRow(final Object key) {
			this.key = key;
		}

		@Override
		public void set(String attributeName, Object value) {
			if (value == null)
				unSetX(key, attributeName);
			else
				setX(key, attributeName, value);
		}

		@Override
		public <T> T get(String attributeName, Class<? extends T> c) {
			return getX(key, attributeName, c, null);
		}

		@Override
		public <T> T get(String attributeName, Class<? extends T> c, T defValue) {
			return getX(key, attributeName, c, defValue);
		}

		@Override
		public <T> List<T> getList(String attributeName, Class<T> c) {
			return getListX(key, attributeName, c, null);
		}

		@Override
		public <T> List<T> getList(String attributeName, Class<T> c, List<T> defValue) {
			return getListX(key, attributeName, c, defValue);
		}

		@Override
		public Object getRaw(String attributeName) {
			return getValueOrEquation(key, attributeName);
		}

		@Override
		public boolean isSet(String attributeName) {
			return isSetX(key, attributeName);
		}

		@Override
		public Map<String, Object> getAllValues() {
			final Map<String, Object> nameToValueMap = new HashMap<String, Object>(types.size());
			for (final CyColumn column : types.values()) {
				final String columnName = column.getName();
				final Class<?> type = column.getType();
				if (type == List.class) {
					final Class<?> elementType = column.getListElementType();
					nameToValueMap.put(columnName, getListX(key, columnName, elementType, null));
				} else
					nameToValueMap.put(columnName, getX(key, columnName, type, null));
			}

			return nameToValueMap;
		}

		@Override
		public CyTable getTable() {
			return CyTableImpl.this;
		}

		@Override
		public String toString() {
			return 	"Table: " + title + " SUID: " + suid + " isImmutable: " + isImmutable + " public: " + pub;
		/*
			final StringBuilder builder = new StringBuilder();
			final Map<String, Object> allVal = getAllValues();
			for(String key: getAllValues().keySet())
				builder.append(key + " = " + allVal.get(key) + ", ");
			return builder.toString();
			*/
		}
	}
}
