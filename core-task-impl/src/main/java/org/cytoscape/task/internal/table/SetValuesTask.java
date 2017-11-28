package org.cytoscape.task.internal.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.task.internal.utils.RowTunable;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

public class SetValuesTask extends AbstractTableDataTask implements ObservableTask {
	final CyApplicationManager appMgr;
	private final CyServiceRegistrar registrar;
	private CyTable table = null;
	private List<String> rowKeys = null;

	@ContainsTunables
	public RowTunable rowTunable = null;

	@Tunable(description="Column to set", context="nogui", longDescription=StringToModel.COLUMN_LONG_DESCRIPTION, exampleStringValue = StringToModel.COLUMN_EXAMPLE)
	public String columnName = null;

	@Tunable(description="Value to set", context="nogui", 
	         longDescription="The value to set the columns in the selected rows to.  "+
	                         "This should be a string value, which will be converted to the appropriate column type.", 
	         exampleStringValue = StringToModel.VALUE_EXAMPLE)
	public String value = null;

	public SetValuesTask(CyApplicationManager appMgr, CyTableManager tableMgr, CyServiceRegistrar reg) {
		super(tableMgr);
		this.appMgr = appMgr;
		registrar = reg;
		rowTunable = new RowTunable(tableMgr);
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		table = rowTunable.getTable();
		if (table == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR,  "Unable to find table '"+rowTunable.getTableString()+"'");
			return;
		}

		List<CyRow> rowList = rowTunable.getRowList();
		if (rowList == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "No rows returned");
			return;
		}

		if (columnName == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "No column specified");
			return;
		}

		if (value == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "No values specified");
			return;
		}
		
		CyColumn column = table.getColumn(columnName);
		if (column == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Column '"+columnName+"' doesn't exist in this table");
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

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Retreived "+rowList.size()+" rows:");

		rowKeys = new ArrayList<String>();
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
					taskMonitor.showMessage(TaskMonitor.Level.ERROR, 
					                        "Unable to convert "+value+" to a "+DataUtils.getType(columnType));
					return;
				}
				message += "column "+column.getName()+" set to "+DataUtils.convertString(value, columnType).toString();
			} else {
				try {
					row.set(column.getName(), DataUtils.convertStringList(value, listType));
				} catch (NumberFormatException nfe) {
					taskMonitor.showMessage(TaskMonitor.Level.ERROR, 
					                        "Unable to convert "+value+" to a list of "+
					                        DataUtils.getType(listType)+"s");
					return;
				}
				message += "list column "+column.getName()+" set to "+DataUtils.convertStringList(value, listType).toString();
			}

			// If we got here, we successfully set the value
			rowKeys.add(key);
			taskMonitor.showMessage(TaskMonitor.Level.INFO, message);
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
