package org.cytoscape.view.table.internal.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

@SuppressWarnings("serial")
public class BrowserTableColumnModel extends DefaultTableColumnModel {
	
	private final Map<TableColumn,Double> gravities = new HashMap<>();
	private final Set<TableColumn> visibleColumns = new HashSet<>();
	
	
	public void addColumn(TableColumn col, Long viewSuid, boolean isVisible, double gravity) {
		col.setIdentifier(viewSuid);
		gravities.put(col, gravity);
		if(isVisible) {
			visibleColumns.add(col);
			super.addColumn(col);
		}
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
		throw new IllegalArgumentException("CyColumnView suid not found: " + suid);
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
		if(columnIndex != newIndex) {
			var c1 = super.getColumn(columnIndex);
			var c2 = super.getColumn(newIndex);
			var g1 = gravities.get(c1);
			var g2 = gravities.get(c2);
			gravities.put(c1, g2);
			gravities.put(c2, g1);
		}
		super.moveColumn(columnIndex, newIndex);
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
