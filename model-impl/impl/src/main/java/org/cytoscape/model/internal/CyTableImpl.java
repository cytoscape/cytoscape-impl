/*
 Copyright (c) 2008, 2010-2011, The Cytoscape Consortium (www.cytoscape.org)

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.di.util.DIUtil;
import org.cytoscape.equations.Equation;
import org.cytoscape.equations.Interpreter;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.SUIDFactory;
import org.cytoscape.model.VirtualColumnInfo;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnNameChangedEvent;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsCreatedEvent;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.TableAddedEvent;
import org.cytoscape.model.events.TableAddedListener;

import com.google.common.collect.SetMultimap;
import com.google.common.collect.HashMultimap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class CyTableImpl implements CyTable, TableAddedListener {
	private static final Logger logger = LoggerFactory.getLogger(CyTableImpl.class);

	private Set<String> currentlyActiveAttributes;
	private Map<String, Map<Object, Object>> attributes; // Maps column names to (key,value) pairs, where "key" is the primary key.

	private Map<String, SetMultimap<Object,Object>> reverse;
	private Map<Object, CyRow> rows; // Maps the primary key to CyRow.

	private Map<String, CyColumn> types;

	// This is not unique and might be changed by user.
	private String title;

	// Visibility value is immutable.
	private boolean pub;

	private boolean isImmutable;

	// Unique ID.
	private final long suid;

	// name of the primary key column
	private String primaryKey;

	private final CyEventHelper eventHelper;
	private final Interpreter interpreter;

	String lastInternalError = null;

	private Map<String, VirtualColumn> virtualColumnMap;
	private int virtualColumnReferenceCount;

	private SavePolicy savePolicy;
	private boolean fireEvents;
	
	/**
	 * Creates a new CyTableImpl object.
	 */
	public CyTableImpl(final String title, final String primaryKey, Class<?> primaryKeyType,
			   final boolean pub, final boolean isMutable, SavePolicy savePolicy,
	                   final CyEventHelper eventHelper, final Interpreter interpreter)
	{
		this.title = title;
		this.primaryKey = primaryKey;
		this.pub = pub;
		this.isImmutable = !isMutable;
		this.suid = SUIDFactory.getNextSUID();
		this.eventHelper = DIUtil.stripProxy(eventHelper);
		this.interpreter = interpreter;
		this.savePolicy = savePolicy;
		this.fireEvents = !pub;

		currentlyActiveAttributes = new HashSet<String>();
		attributes = new HashMap<String, Map<Object, Object>>();
		reverse =  new HashMap<String, SetMultimap<Object,Object>>();
		rows = new HashMap<Object, CyRow>(10000, 0.5f);
		types = new HashMap<String, CyColumn>();

		VirtualColumnInfo virtualInfo = new VirtualColumnInfoImpl(false, null, null, null, null, true);
		// Create the primary key column.  Do this explicitly
		// so that we don't fire an event.
		types.put(primaryKey, new CyColumnImpl(this, primaryKey, primaryKeyType,
						       /* listElementType = */ null,
						       virtualInfo,
						       /* isPrimaryKey = */ true,
						       /* isImmutable = */ true, 
							   null));
		attributes.put(primaryKey, new HashMap<Object, Object>());
		reverse.put(primaryKey, HashMultimap.create());

		virtualColumnMap = new HashMap<String, VirtualColumn>();
		virtualColumnReferenceCount = 0;
	}


	@Override
	public synchronized void swap(final CyTable otherTable) {
		final CyTableImpl other = (CyTableImpl)otherTable;

		final Set<String> tempCurrentlyActiveAttributes = currentlyActiveAttributes;
		currentlyActiveAttributes = other.currentlyActiveAttributes;
		other.currentlyActiveAttributes = tempCurrentlyActiveAttributes;

		final Map<String, Map<Object, Object>> tempAttributes = attributes;
		attributes = other.attributes;
		other.attributes = tempAttributes;

		final Map<String, SetMultimap<Object,Object>> tempReverse = reverse;
		reverse = other.reverse;
		other.reverse = tempReverse;

		final Map<Object, CyRow> tempRows = rows;
		rows = other.rows;
		other.rows = tempRows;

		final Map<String, CyColumn> tempTypes = types;
		types = other.types;
		other.types = tempTypes;

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

		final int tempVirtualColumnReferenceCount = virtualColumnReferenceCount;
		virtualColumnReferenceCount = other.virtualColumnReferenceCount;
		other.virtualColumnReferenceCount = tempVirtualColumnReferenceCount;
		
		final SavePolicy tempSavePolicy = savePolicy;
		savePolicy = other.savePolicy;
		other.savePolicy = tempSavePolicy;
	}

	void updateColumnName(final String oldColumnName, final String newColumnName) {
		if (oldColumnName.equals(newColumnName))
			return;

		synchronized(this) {
			if (currentlyActiveAttributes.contains(oldColumnName)) {
				currentlyActiveAttributes.remove(oldColumnName);
				currentlyActiveAttributes.add(newColumnName);
			}

			final Map<Object, Object> keyValuePairs = attributes.get(oldColumnName);
			if (keyValuePairs != null) {
				attributes.remove(oldColumnName);
				attributes.put(newColumnName, keyValuePairs);
			}

			final SetMultimap<Object,Object> valueKeysPairs = reverse.get(oldColumnName);
			if (valueKeysPairs != null) {
				reverse.remove(oldColumnName);
				reverse.put(newColumnName, valueKeysPairs);
			}

			final CyColumn column = types.get(oldColumnName);
			types.put(newColumnName, column);
			types.remove(oldColumnName);
		}

		eventHelper.fireEvent(new ColumnNameChangedEvent(this, oldColumnName,
									    newColumnName));
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	@Override
	public long getSUID() {
		return suid;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	@Override
	public boolean isPublic() {
		return pub;
	}

	@Override
	public synchronized CyTable.Mutability getMutability() {
		if (isImmutable)
			return Mutability.PERMANENTLY_IMMUTABLE;
		else if (virtualColumnReferenceCount == 0)
			return Mutability.MUTABLE;
		else
			return Mutability.IMMUTABLE_DUE_TO_VIRT_COLUMN_REFERENCES;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	@Override
	synchronized public String getTitle() {
		return title;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param title
	 *            DOCUMENT ME!
	 */
	@Override
	synchronized public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	@Override
	synchronized public Collection<CyColumn> getColumns() {
		return types.values();
	}

	@Override
	synchronized public CyColumn getColumn(final String columnName) {
		return types.get(columnName);
	}

	@Override
	public CyColumn getPrimaryKey() {
		return types.get(primaryKey);
	}

	@Override
	synchronized public int getRowCount() {
		return rows.size();
	}

	@Override
	public void deleteColumn(final String columnName) {
		synchronized(this) {
			if (columnName == null)
				throw new NullPointerException("\"columnName\" must not be null!");

			final CyColumn column = types.get(columnName);
			if (column == null)
				return;

			if (column.isImmutable())
				throw new IllegalArgumentException("cannot delete immutable column \""
								   + columnName + "\"!");
				
			final VirtualColumn virtColumn = virtualColumnMap.get(columnName);
			if (attributes.containsKey(columnName) || virtColumn != null) {
				if (virtColumn != null) {
					final CyColumn cyColumn = types.get(columnName);
					virtualColumnMap.remove(columnName);
					types.remove(columnName);
					--((CyTableImpl)cyColumn.getVirtualColumnInfo().getSourceTable()).virtualColumnReferenceCount;
				} else {
					attributes.remove(columnName);
					reverse.remove(columnName);
					types.remove(columnName);
				}
			}
		}

		// This event must be synchronous!
		eventHelper.fireEvent(new ColumnDeletedEvent(this, columnName));
	}

	@Override
	public <T> void createColumn(final String columnName, final Class<? extends T> type,
				     final boolean isImmutable) 
	{
		createColumn(columnName,type,isImmutable,null);
	}

	@Override
	public <T> void createColumn(final String columnName, final Class<? extends T> type,
				     final boolean isImmutable, final T defaultValue)
	{
		synchronized(this) {
			if (columnName == null)
				throw new NullPointerException("attribute name is null");

			if (type == null)
				throw new NullPointerException("type is null");

			if (types.get(columnName) != null)
				throw new IllegalArgumentException("attribute already exists for name: '"
								   + columnName + "' with type: "
								   + types.get(columnName).getType());

			if (type == List.class)
				throw new IllegalArgumentException(
						"use createListColumn() to create List columns instead of createColumn for attribute '"
						+ columnName + "'!");

			checkClass(type);

			VirtualColumnInfo virtualInfo = new VirtualColumnInfoImpl(false, null, null, null, null, isImmutable);
			types.put(columnName, new CyColumnImpl(this, columnName, type,
							       /* listElementType = */ null,
							       virtualInfo ,
							       /* isPrimaryKey = */ false,
							       isImmutable,
								   defaultValue));
			attributes.put(columnName, new HashMap<Object, Object>(10000));
			reverse.put(columnName, HashMultimap.create());
		}

		eventHelper.fireEvent(new ColumnCreatedEvent(this, columnName));
	}

	@Override
	public <T> void createListColumn(final String columnName, final Class<T> listElementType,
					 final boolean isImmutable)
	{
		createListColumn(columnName,listElementType,isImmutable,null);
	}


	@Override
	public <T> void createListColumn(final String columnName, final Class<T> listElementType,
					 final boolean isImmutable, final List<T> defaultValue)
	{
		synchronized(this) {
			if (columnName == null)
				throw new NullPointerException("attribute name is null");

			if (listElementType == null)
				throw new NullPointerException("listElementType is null");

			if (types.get(columnName) != null)
				throw new IllegalArgumentException("attribute already exists for name: '"
								   + columnName + "' with type: "
								   + types.get(columnName).getType());

			checkClass(listElementType);

			VirtualColumnInfo virtualInfo = new VirtualColumnInfoImpl(false, null, null, null, null, isImmutable);
			types.put(columnName, new CyColumnImpl(this, columnName, List.class,
							       listElementType,
							       virtualInfo,
							       /* isPrimaryKey = */ false,
							       isImmutable,
								   defaultValue));
			attributes.put(columnName, new HashMap<Object, Object>(10000));
			reverse.put(columnName, HashMultimap.create());
		}

		eventHelper.fireEvent(new ColumnCreatedEvent(this, columnName));
	}

	synchronized <T> List<T> getColumnValues(final String columnName, final Class<? extends T> type) {
		if (columnName == null)
			throw new NullPointerException("column name is null!");

		if (type == null)
			throw new NullPointerException("column type is null!");

		if (columnName.equals(primaryKey)) {
			final List primaryKeys = new ArrayList(rows.size());
			for (final Object primaryKey : rows.keySet())
				primaryKeys.add(primaryKey);
			return primaryKeys;
		}

		final VirtualColumn virtColumn = virtualColumnMap.get(columnName);
		if (virtColumn != null)
			return virtColumn.getColumnValues();

		Map<Object, Object> vals = attributes.get(columnName);
		if (vals == null)
			throw new IllegalArgumentException("attribute \"" + columnName
							   + "\" does not exist!");

		List l = new ArrayList(vals.size());
		for (final Object suid : vals.keySet()) {
			final Object value = vals.get(suid);
			if (value instanceof Equation) {
				final StringBuilder errorMsg = new StringBuilder();
				final Object eqnValue =
					EqnSupport.evalEquation((Equation)value, suid, interpreter,
								currentlyActiveAttributes,
								columnName, errorMsg, this);
				lastInternalError = errorMsg.toString();
				if (eqnValue == null)
					throw new IllegalStateException("can't convert an equation to a value!");
				l.add(eqnValue);
			} else
				l.add(value);
		}

		return l;
	}

	@Override
	public CyRow getRow(final Object key) {
		checkKey(key);

		CyRow row;
		synchronized(this) {
			row = rows.get(key);
			if (row != null)
				return row;

			row = new InternalRow(key);
			rows.put(key, row);
		}

		if (fireEvents)
			eventHelper.addEventPayload((CyTable)this, (Object)key, RowsCreatedEvent.class);
		return row;
	}

	@Override
	synchronized public boolean rowExists(final Object primaryKey) {
		return (primaryKey != null && rows.containsKey(primaryKey));	
	}

	private boolean rowIsEmpty(final Object key) {
		for (final String columnName : attributes.keySet()) {
			if (!attributes.get(columnName).isEmpty())
				return false;
		}

		return true;
	}

	@Override
	synchronized public String getLastInternalError() {
		return lastInternalError;
	}

	private void checkKey(final Object suid) {
		if (suid == null)
			throw new NullPointerException("key is null");

		if (!types.get(primaryKey).getType().isAssignableFrom(suid.getClass()))
			throw new IllegalArgumentException("key of type " + suid.getClass()
							   + " and not the expected: "
							   + types.get(primaryKey).getType().getName());
	}

	@Override
	synchronized public List<CyRow> getAllRows() {
		return new ArrayList<CyRow>(rows.values());
	}

	@Override
	synchronized public Collection<CyRow> getMatchingRows(final String columnName, final Object value) {
		final VirtualColumn virtColumn = virtualColumnMap.get(columnName);
		if (virtColumn != null)
			return virtColumn.getMatchingRows(value);

		if (columnName.equals(primaryKey)) {
			final ArrayList<CyRow> matchingRows = new ArrayList<CyRow>(1);
			final CyRow matchingRow = rows.get(value);
			if (matchingRow != null)
				matchingRows.add(matchingRow);
			return matchingRows;
		}

		final SetMultimap<Object,Object> valueToKeysMap = reverse.get(columnName);

		final Set<Object> keys = valueToKeysMap.get(value);
		if (keys == null)
			return new ArrayList<CyRow>();

		final ArrayList<CyRow> matchingRows = new ArrayList<CyRow>(rows.size());
		for (final Object key : keys)
			matchingRows.add(rows.get(key));

		return matchingRows;
	}

	private void setX(final Object key, final String columnName, final Object value) {
		if (columnName == null)
			throw new NullPointerException("columnName must not be null!");
		if (value == null)
			throw new NullPointerException("value must not be null!");

		Object newValue;
		Object newRawValue;

		synchronized(this) {
			if (types.get(columnName) == null)
				throw new IllegalArgumentException("attribute: '" + columnName
								   + "' does not yet exist!");

			final Class<?> columnType = types.get(columnName).getType();
			if (columnType == List.class) {
				setListX(key, columnName, value);
				return;
			}

			if (!(value instanceof Equation))
				checkType(value);

			final VirtualColumn virtColumn = virtualColumnMap.get(columnName);
			if (virtColumn != null) {
				virtColumn.setValue(key, value);
				newValue = virtColumn.getValue(key);
				newRawValue = virtColumn.getRawValue(key);
			} else {
				Map<Object, Object> keyToValueMap = attributes.get(columnName);

				if (!columnType.isAssignableFrom(value.getClass())
				    && !EqnSupport.scalarEquationIsCompatible(value, columnType))
					throw new IllegalArgumentException("value is not of type: "
									   + columnType);

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
						logger.warn("attempted premature evaluation evaluation for "
							    + equation);
				} else {
					// TODO this is an implicit addRow - not sure if we want to refactor this or not
					newRawValue = newValue = columnType.cast(value);
					final Object oldValue = keyToValueMap.get(key);
					keyToValueMap.put(key, newValue);
					addToReverseMap(columnName, key, oldValue, newValue);
				}
			}
		}

		if (fireEvents)
			eventHelper.addEventPayload((CyTable)this,
			                            new RowSetRecord(getRow(key), columnName, newValue, newRawValue),
			                            RowsSetEvent.class);

	}

	private void addToReverseMap(final String columnName, final Object key,
				     final Object oldValue, final Object newValue)
	{
		final SetMultimap<Object,Object> valueTokeysMap = reverse.get(columnName);
		valueTokeysMap.remove(oldValue,key);
		valueTokeysMap.put(newValue,key);
	}

	private void setListX(final Object key, final String columnName, final Object value) {
		Object newValue;

		synchronized(this) {
			if (value instanceof List) {
				final List list = (List)value;
				if (!list.isEmpty())
					checkType(list.get(0));
			} else if (!(value instanceof Equation))
				throw new IllegalArgumentException("value is a " + value.getClass().getName()
								   + " and not a List for column '"
								   + columnName + "'!");
			else if (!EqnSupport.listEquationIsCompatible((Equation)value,
								      types.get(columnName).getListElementType()))
				throw new IllegalArgumentException(
								   "value is not a List equation of a compatible type for column '"
								   + columnName + "'!");

			final VirtualColumn virtColumn = virtualColumnMap.get(columnName);
			if (virtColumn != null) {
				virtColumn.setValue(key, value);
				newValue = virtColumn.getListValue(key);
			} else {
				Map<Object, Object> keyToValueMap = attributes.get(columnName);

				// TODO this is an implicit addRow - not sure if we want to refactor this or not
				final Object oldValue = keyToValueMap.get(key);
				keyToValueMap.put(key, value);
				if (value instanceof Equation) {
					final StringBuilder errorMsg = new StringBuilder();
					newValue = EqnSupport.evalEquation((Equation)value, suid, interpreter,
									   currentlyActiveAttributes, columnName,
									   errorMsg, this);
					lastInternalError = errorMsg.toString();
				} else {
					newValue = value;
					addToReverseMap(columnName, key, oldValue, value);
				}
			}
		}

		if (fireEvents)
			eventHelper.addEventPayload((CyTable)this,
			                            new RowSetRecord(getRow(key),columnName,newValue, value),
			                            RowsSetEvent.class);
	}

	synchronized private void unSetX(final Object key, final String columnName) {
		synchronized(this) {
			final VirtualColumn virtColumn = virtualColumnMap.get(columnName);
			if (virtColumn != null)
				virtColumn.setValue(key, null);
			else {
				if (!types.containsKey(columnName) || !attributes.containsKey(columnName))
					throw new IllegalArgumentException("attribute: '" + columnName
									   + "' does not yet exist!");

				final Map<Object, Object> keyToValueMap = attributes.get(columnName);
				if (!keyToValueMap.containsKey(key))
					return;

				final Object value = keyToValueMap.get(key);
				if (!(value instanceof Equation))
					removeFromReverseMap(columnName, key, value);
				keyToValueMap.remove(key);
			}
		}

		if (fireEvents)
			eventHelper.addEventPayload((CyTable)this,
			                            new RowSetRecord(getRow(key), columnName, null, null),
			                            RowsSetEvent.class);
	}

	private void removeFromReverseMap(final String columnName, final Object key, final Object value) {
		final SetMultimap<Object,Object> valueTokeysMap = reverse.get(columnName);
		valueTokeysMap.remove(key,value);
	}

	synchronized Object getValueOrEquation(final Object key, final String columnName) {
		if (columnName.equals(primaryKey))
			return key;

		final VirtualColumn virtColumn = virtualColumnMap.get(columnName);
		if (virtColumn != null)
			return virtColumn.getRawValue(key);

		Map<Object, Object> keyToValueMap = attributes.get(columnName);
		if (keyToValueMap == null)
			return null;

		return keyToValueMap.get(key);
	}

	synchronized private <T> T getX(final Object key, final String columnName, final Class<? extends T> type, final T defaultValue) {
		if (type.isAssignableFrom(List.class))
			logger.debug("risky use of get() instead of getList() for retrieving list");
		lastInternalError = null;

		final VirtualColumn virtColumn = virtualColumnMap.get(columnName);
		if (virtColumn != null)
			return (T)virtColumn.getValue(key);

		final Object vl = getValueOrEquation(key, columnName);
		if (vl == null)
			return getDefaultValue(columnName,defaultValue);

		if (vl instanceof Equation) {
			final StringBuilder errorMsg = new StringBuilder();
			final Object result =
				EqnSupport.evalEquation((Equation)vl, key, interpreter,
							currentlyActiveAttributes, columnName,
							errorMsg, this);
			lastInternalError = errorMsg.toString();
			return result == null ? getDefaultValue(columnName,defaultValue) : (T)EqnSupport.convertEqnResultToColumnType(type, result);
		} else
			return type.cast(vl);
	}

	private <T> T getDefaultValue(final String columnName, final T defaultValue) {
		if ( defaultValue == null ) {
			final CyColumn column = types.get(columnName);
			if ( column == null )
				return null;
			else
				return (T)(column.getDefaultValue());
		} else {
			return defaultValue;
		}
	}

	Object getValue(Object key, String columnName) {
		final VirtualColumn virtColumn = virtualColumnMap.get(columnName);
		if (virtColumn != null)
			return virtColumn.getValue(key);

		final Object vl = getValueOrEquation(key, columnName);
		if (vl == null)
			return null;

		if (vl instanceof Equation) {
			final StringBuilder errorMsg = new StringBuilder();
			final Object value =
				EqnSupport.evalEquation((Equation)vl, key, interpreter,
							currentlyActiveAttributes, columnName,
							errorMsg, this);
			lastInternalError = errorMsg.toString();
			return value;
		} else
			return vl;
	}

	synchronized private <T> List<T> getListX(final Object key, final String columnName,
							   final Class<? extends T> listElementType, final List<T> defaultValue)
	{
		if (!types.containsKey(columnName))
			throw new IllegalArgumentException("'" + columnName + "' does not yet exist!");
		final Class<?> expectedListElementType = types.get(columnName).getListElementType();
		if (expectedListElementType == null)
			throw new IllegalArgumentException("'" + columnName + "' is not a List!");
		if (expectedListElementType != listElementType)
			throw new IllegalArgumentException("invalid list element type for column '"
							   + columnName + ", found: " + listElementType.getName()
							   + ", expected: " + expectedListElementType.getName()
							   + "!");

		lastInternalError = null;

		final VirtualColumn virtColumn = virtualColumnMap.get(columnName);
		if (virtColumn != null)
			return (List<T>)virtColumn.getListValue(key);

		final Object vl = getValueOrEquation(key, columnName);
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

	synchronized private <T> boolean isSetX(final Object key, final String columnName) {
		final VirtualColumn virtColumn = virtualColumnMap.get(columnName);
		if (virtColumn != null)
			return virtColumn.getRawValue(key) != null;
		else {
			if (!attributes.containsKey(columnName))
				return false;

			final Map<Object, Object> keyToValueMap = attributes.get(columnName);
			return keyToValueMap.get(key) != null;
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

	private void checkType(Object o) {
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
					     final CyTable sourceTable, final String sourceJoinKeyName,
					     final String targetJoinKeyName, final boolean isImmutable)
	{
		if (virtualColumnName == null)
			throw new NullPointerException("\"virtualColumn\" argument must never be null!");
		final String targetName = getUniqueColumnName(virtualColumnName);

		synchronized(this) {
			if (sourceColumnName == null)
				throw new NullPointerException("\"sourceColumn\" argument must never be null!");
			if (sourceTable == null)
				throw new NullPointerException("\"sourceTable\" argument must never be null!");
			if (sourceJoinKeyName == null)
				throw new NullPointerException("\"sourceJoinKey\" argument must never be null!");
			if (targetJoinKeyName == null)
				throw new NullPointerException("\"targetJoinKey\" argument must never be null!");

			final CyColumn sourceColumn = sourceTable.getColumn(sourceColumnName);
			if (sourceColumn == null)
				throw new IllegalArgumentException("\"sourceColumn\" is not a column in \"sourceColumn\"!");

			final CyColumn sourceJoinKeyType = sourceTable.getColumn(sourceJoinKeyName);
			if (sourceJoinKeyType == null)
				throw new IllegalArgumentException("\"sourceJoinKey\" is not a known column in \"sourceTable\"!");

			final CyColumn targetJoinKeyType = this.getColumn(targetJoinKeyName);
			if (targetJoinKeyType == null)
				throw new IllegalArgumentException("\"targetJoinKey\" is not a known column in this table!");

			if (sourceJoinKeyType.getType() != targetJoinKeyType.getType())
				throw new IllegalArgumentException("\"sourceJoinKey\" has a different type from \"targetJoinKey\"!");

			++((CyTableImpl)sourceTable).virtualColumnReferenceCount;
			VirtualColumnInfo virtualInfo = new VirtualColumnInfoImpl(true, sourceTable, sourceColumnName, sourceJoinKeyName, targetJoinKeyName, isImmutable);
			final CyColumn targetColumn =
				new CyColumnImpl(this, virtualColumnName, sourceColumn.getType(),
						 sourceColumn.getListElementType(),
						 virtualInfo,
						 /* isPrimaryKey = */ false,
						 isImmutable,
						 null);
			types.put(targetName, targetColumn);
			virtualColumnMap.put(targetName,
					     new VirtualColumn(sourceTable, sourceColumnName, this,
							       sourceJoinKeyName, targetJoinKeyName));
		}

		eventHelper.fireEvent(new ColumnCreatedEvent(this, targetName));
		return targetName;
	}

	private String getUniqueColumnName(final String preferredName) {
		if (!types.containsKey(preferredName))
			return preferredName;

		String newUniqueName;
		int i = 0;
		do {
			++i;
			newUniqueName = preferredName + "-" + i;
		} while (types.containsKey(newUniqueName));

		return newUniqueName;
	}

	// Warning: This method is only to be used by CyTableManagerImpl!!!  That's also the reason
	//          why no ColumnDeletedEvent events are being fired by it!  Also this deletes
	//          (intentionally!) immutable columns!
	synchronized void removeAllVirtColumns() {
		if (virtualColumnReferenceCount > 0)
			return;

		for (final String columnName : virtualColumnMap.keySet()) {
			final CyColumn column = types.get(columnName);
			types.remove(columnName);
			--((CyTableImpl)column.getVirtualColumnInfo().getSourceTable()).virtualColumnReferenceCount;
		}
		virtualColumnMap.clear();
	}

	@Override
	synchronized public final void addVirtualColumns(final CyTable sourceTable,
							 final String sourceJoinKeyName,
							 final String targetJoinKeyName,
							 final boolean isImmutable)
	{
		if (sourceTable == null)
			throw new NullPointerException("\"sourceTable\" argument must never be null!");
		if (sourceJoinKeyName == null)
			throw new NullPointerException("\"sourceJoinKeyName\" argument must never be null!");
		if (targetJoinKeyName == null)
			throw new NullPointerException("\"targetJoinKeyName\" argument must never be null!");

		final CyColumn sourceJoinKey = sourceTable.getColumn(sourceJoinKeyName);
		if (sourceJoinKey == null)
			throw new IllegalArgumentException("\"sourceJoinKey\" is not a known column in \"sourceTable\"!");

		final CyColumn targetJoinKey = this.getColumn(targetJoinKeyName);
		if (targetJoinKey == null)
			throw new IllegalArgumentException("\"" + targetJoinKeyName
							   + "\" is not a known column in this table!");

		if (sourceJoinKey.getType() != targetJoinKey.getType())
			throw new IllegalArgumentException("\"" + sourceJoinKeyName
							   + "\" has a different type from \""
							   + targetJoinKeyName + "\"!");

		final Collection<CyColumn> columns = sourceTable.getColumns();
		for (final CyColumn column : columns) {
			final String columnName = column.getName();
			if (columnName.equals(sourceJoinKeyName))
				continue;

			addVirtualColumn(columnName, columnName, sourceTable, sourceJoinKeyName,
					 targetJoinKeyName, isImmutable);
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

//		@Override
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
