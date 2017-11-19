package org.cytoscape.task.internal.table;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.util.ListSingleSelection;

public class CreateTableTask extends AbstractTableDataTask implements ObservableTask {
	final CyApplicationManager appMgr;
	final CyTableFactory tableFactory;
	CyTable table = null;

	@Tunable(description="Table name (title)", context="nogui", 
			longDescription=StringToModel.TABLE_TITLE_LONG_DESCRIPTION, exampleStringValue = StringToModel.TABLE_TITLE_EXAMPLE)
	public String title = null;

	@Tunable(description="Key column name", context="nogui", 
			longDescription=StringToModel.COLUMN_LONG_DESCRIPTION, exampleStringValue = StringToModel.COLUMN_EXAMPLE)
	public String keyColumn = null;

	@Tunable (description="Type of key column", context="nogui", 
			longDescription=StringToModel.KEY_TYPE_LONG_DESCRIPTION, exampleStringValue = StringToModel.KEY_TYPE_EXAMPLE)
	public ListSingleSelection<String> keyColumnType =  
		new ListSingleSelection<String>("integer", "long", "double", "string", "boolean");

	public CreateTableTask(CyApplicationManager appMgr, CyTableFactory factory, CyTableManager tableMgr) {
		super(tableMgr);
		this.appMgr = appMgr;
		this.tableFactory = factory;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		if (keyColumn == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR,  "Name of key column must be specified");
			return;
		}

		Class keyType = DataUtils.getType(keyColumnType.getSelectedValue());
		if (keyType == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR,  "Key column type must be specified");
			return;
		}

		if (title == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR,  "Table title must be specified");
			return;
		}

		table = tableFactory.createTable(title, keyColumn, keyType, true, true);
		if (table != null) {
			taskMonitor.showMessage(TaskMonitor.Level.INFO,  "Created table '"+table.toString()+"' (suid:"+table.getSUID()+")");
			cyTableManager.addTable(table);
		} else 
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Unable to create table'"+title+"'");
		

	}

	@Override
	public List<Class<?>> getResultClasses() {	return Arrays.asList(CyTable.class, String.class, JSONResult.class);	}

	@Override
	public Object getResults(Class requestedType) {
		if (requestedType.equals(CyTable.class)) 				return table;
		if (requestedType.equals(String.class)) 			return "" + table.getSUID();
		if (requestedType.equals(JSONResult.class)) {
			JSONResult res = () -> {		return "{\"table\":" + table.getSUID() + "}";	};
			return res;
}
		return null;
	}
}
