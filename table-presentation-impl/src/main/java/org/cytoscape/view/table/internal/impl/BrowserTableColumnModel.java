package org.cytoscape.view.table.internal.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

/*
 * #%L
 * Cytoscape Table Presentation Impl (table-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
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

@SuppressWarnings("serial")
public class BrowserTableColumnModel extends DefaultTableColumnModel {
	
	private final Map<TableColumn,Double> gravities = new HashMap<>();
	private final Set<TableColumn> visibleColumns = new HashSet<>();
	
	private final List<BrowserTableColumnModelListener> listeners = new CopyOnWriteArrayList<>();
	
	
	public void addColumn(TableColumn col, Long viewSuid, boolean isVisible, double gravity) {
		col.setIdentifier(viewSuid);
		gravities.put(col, gravity);
		if(isVisible) {
			visibleColumns.add(col);
			super.addColumn(col);
		}
	}
	
	/**
	 * Columns are removed from the model when they are made invisible, but we want to remember
	 * the gravity of invisible columns, so we need this new method for when columns are actually deleted.
	 */
	public void deleteColumn(TableColumn column) {
		gravities.remove(column);
		super.removeColumn(column);
	}
	
	public void addBrowserTableColumnModelListener(BrowserTableColumnModelListener listener) {
		listeners.add(listener);
	}
	
	public void removeBrowserTableColumnModelListener(BrowserTableColumnModelListener listener) {
		listeners.remove(listener);
	}
	
	@Override
	public void addColumn(TableColumn aColumn) {
		super.addColumn(aColumn);
	}
	
 	public boolean isVisible(TableColumn col) {
		return visibleColumns.contains(col);
	}
	
	public TableColumn getTableColumn(Long suid) {
		for(TableColumn col : gravities.keySet()) {
			if(suid.equals(col.getIdentifier())) {
				return col;
			}
		}
		return null;
	}
	
	private int getSetVisibleIndex(TableColumn col) {
		var list = new ArrayList<>(gravities.entrySet());
        list.sort(Entry.comparingByValue());

        int i = 0;
        for(var entry : list) {
        	TableColumn curCol = entry.getKey();
        	if(curCol == col) {
        		return i;
        	}
        	if(isVisible(curCol)) {
        		i++;
        	}
        }
       	throw new IllegalArgumentException("Table Column not found");
	}
	
	
	
	public void setColumnVisible(TableColumn col, boolean visible) {
		if(visible == isVisible(col))
			return;
		
		if(visible) {
			int visibleIndex = getSetVisibleIndex(col);
			visibleColumns.add(col);
			// column gets added at the end
			int index = getColumnCount();
			addColumn(col);
			super.moveColumn(index, visibleIndex);
		} else {
			visibleColumns.remove(col);
			removeColumn(col);
		}
	}
	
	
	public void setColumnGravity(TableColumn col, double gravity) {
		gravities.put(col, gravity);
	}
	

	public void reorderColumnsToRespectGravity() {
		var list = new ArrayList<>(gravities.entrySet());
        list.sort(Entry.comparingByValue());
        
        int i = 0;
        for(var entry : list) {
        	TableColumn curCol = entry.getKey();
        	if(isVisible(curCol)) {
	        	int index = super.getColumnIndex(curCol.getIdentifier());
	        	if(index != i) {
	        		super.moveColumn(index, i);
	        	}
	        	i++;
        	}
        }
	}
	
	public int getColumnCount(boolean onlyVisible) {
		return onlyVisible ? super.getColumnCount() : gravities.size();
	}
	
	@Override
	public void moveColumn(int columnIndex, int newIndex) {
		BrowserTableColumnModelGravityEvent event = null;
		if(columnIndex != newIndex) {
			final var c1 = super.getColumn(columnIndex);
			final var c2 = super.getColumn(newIndex);
			final var g1 = gravities.get(c1);
			final var g2 = gravities.get(c2);
			gravities.put(c1, g2);
			gravities.put(c2, g1);
			
			Long c1Suid = (Long) c1.getIdentifier();
			Long c2Suid = (Long) c2.getIdentifier();
			event = new BrowserTableColumnModelGravityEvent(c1Suid, g2, c2Suid, g1);
		}
		
		super.moveColumn(columnIndex, newIndex);
		
		if(event != null) {
			for(var listener : listeners) {
				listener.columnGravityChanged(event);
			}
		}
	}
	
	public TableColumn getColumnByModelIndex(int modelColumnIndex) {
		for (TableColumn column : gravities.keySet()) {
			if (column.getModelIndex() == modelColumnIndex) {
				return column;
			}
		}
		return null;
	}

}
