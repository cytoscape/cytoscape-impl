package org.cytoscape.internal.prefs.lib;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class MasterListTableModel implements TableModel
{

	private List<String> recordList = new ArrayList<String>();

	public void addRecord(String lay)	{ 	recordList.add(lay); }
	@Override	public int getRowCount() {		return recordList.size();	}
	@Override	public int getColumnCount() {		return 1;	}
	@Override	public String getColumnName(int columnIndex) {		return "";	}
	@Override	public Class<?> getColumnClass(int columnIndex) {		return String.class;	}
	@Override	public boolean isCellEditable(int rowIndex, int columnIndex) {		return false;	}

				public String getLayout(int rowIndex) {		return recordList.get(rowIndex);	}
	@Override	public Object getValueAt(int rowIndex, int columnIndex) {		return recordList.get(rowIndex);	}
	@Override	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {	}
	@Override	public void addTableModelListener(TableModelListener l) {	}

	@Override	public void removeTableModelListener(TableModelListener l) {	}

}
