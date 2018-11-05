package org.cytoscape.task.internal.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.task.internal.utils.RowTunable;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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

public class SetValuesTask extends AbstractTableDataTask implements ObservableTask {
	
	private CyTable table;
	private List<String> rowKeys;

	@ContainsTunables
	public RowTunable rowTunable;

	@Tunable(description="Column to set", context="nogui", longDescription=StringToModel.COLUMN_LONG_DESCRIPTION, exampleStringValue = StringToModel.COLUMN_EXAMPLE)
	public String columnName;

	@Tunable(description="Value to set", context="nogui", 
	         longDescription="The value to set the columns in the selected rows to.  "+
	                         "This should be a string value, which will be converted to the appropriate column type.", 
	         exampleStringValue = StringToModel.VALUE_EXAMPLE)
	public String value;

	public SetValuesTask(CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
		rowTunable = new RowTunable(serviceRegistrar);
	}

	@Override
	public void run(final TaskMonitor tm) {
		table = rowTunable.getTable();
		if (table == null) {
			tm.showMessage(TaskMonitor.Level.ERROR,  "Unable to find table '"+rowTunable.getTableString()+"'");
			return;
		}

		List<CyRow> rowList = rowTunable.getRowList();
		if (rowList == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "No rows returned");
			return;
		}

		if (columnName == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "No column specified");
			return;
		}

		if (value == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "No values specified");
			return;
		}
		
		CyColumn column = table.getColumn(columnName);
		if (column == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Column '"+columnName+"' doesn't exist in this table");
			return;
		}

		Class columnType = column.getType();
		Class listType = null;
		if (columnType.equals(List.class))
			listType = column.getListElementType();

		String primaryKey = table.getPrimaryKey().getName();
		CyColumn nameColumn = table.getColumn(CyNetwork.NAME);
		String nameKey = null;
		if (nameColumn != null) nameKey = nameColumn.getName();

		tm.showMessage(TaskMonitor.Level.INFO, "Retreived "+rowList.size()+" rows:");

		rowKeys = new ArrayList<>();
		for (CyRow row: rowList) {
			String key = row.getRaw(primaryKey).toString();
			String message = "  Row (key:"+key;
			if (nameKey != null)
				message += ", name: "+row.get(nameKey, String.class)+") ";
			else
				message += ") ";
			if (listType == null) {
				try {
					row.set(column.getName(), DataUtils.convertString(value, columnType));
				} catch (NumberFormatException nfe) {
					tm.showMessage(TaskMonitor.Level.ERROR, 
					                        "Unable to convert "+value+" to a "+DataUtils.getType(columnType));
					return;
				}
				message += "column "+column.getName()+" set to "+DataUtils.convertString(value, columnType).toString();
			} else {
				try {
					row.set(column.getName(), DataUtils.convertStringList(value, listType));
				} catch (NumberFormatException nfe) {
					tm.showMessage(TaskMonitor.Level.ERROR, 
					                        "Unable to convert "+value+" to a list of "+
					                        DataUtils.getType(listType)+"s");
					return;
				}
				message += "list column "+column.getName()+" set to "+DataUtils.convertStringList(value, listType).toString();
			}

			// If we got here, we successfully set the value
			rowKeys.add(key);
			tm.showMessage(TaskMonitor.Level.INFO, message);
		}
	}

	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(List.class, String.class, JSONResult.class);	
	}

	@Override
	public Object getResults(Class requestedType) {
		if (requestedType.equals(String.class)) {
			String ret = "";
			for (String key: rowKeys)
				ret += key+",";
			return ret.substring(0, ret.length()-1);
		}
		if (requestedType.equals(JSONResult.class)) {
			JSONResult res = () -> {
				if (table == null || rowTunable.getRowList() == null) return "{}";
				String xstring = "{\"table\":"+table.getSUID()+",\"rows\": ";
				StringJoiner joiner = new StringJoiner(", ", "[", "]");
				for (String key: rowKeys)
					joiner.add("\""+key+"\"");
				// System.out.println("JSON output: "+xstring.substring(0, xstring.length()-1)+"]}");
				return xstring.substring(0, xstring.length()-1) + joiner.toString() + "}";
			};
			return res;
		}
		return rowKeys;
	}
}
