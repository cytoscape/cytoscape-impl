package org.cytoscape.task.internal.table;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.task.internal.utils.TableTunable;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

public class DeleteRowTask extends AbstractTableDataTask {
	final CyApplicationManager appMgr;
	CyRow row = null;
	CyTable table = null;
	private final CyServiceRegistrar serviceRegistrar;

	@ContainsTunables
	public TableTunable tableTunable = null;

	@Tunable(description="Key value for row to delete", context="nogui", 
			longDescription=StringToModel.VALUE_LONG_DESCRIPTION, exampleStringValue = StringToModel.VALUE_EXAMPLE)

	public String keyValue = null;

	public DeleteRowTask(CyApplicationManager appMgr, CyTableManager tableMgr, CyServiceRegistrar reg) {
		super(tableMgr);
		this.appMgr = appMgr;
		tableTunable = new TableTunable(tableMgr);
		serviceRegistrar = reg;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		table = tableTunable.getTable();
		if (table == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR,  "Unable to find table '"+tableTunable.getTableString()+"'");
			return;
		}
		if (keyValue == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR,  "Key of row to delete must be specified");
			return;
		}

		// Get the primary key column
		CyColumn primaryKColumn = table.getPrimaryKey();
		Class keyType = primaryKColumn.getType();
		Object key = null;
		try {
			key = DataUtils.convertString(keyValue, keyType);
		} catch (NumberFormatException nfe) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Unable to convert "+keyValue+" to a "+keyType.getName()+": "+nfe.getMessage());
			return;
		}
		if (key == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Unable to convert "+keyValue+" to a "+keyType.getName());
			return;
		}
		if (!table.rowExists(key)) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Row "+keyValue+" doesn't exist");
			return;
		}
		
		table.deleteRows(Collections.singletonList(key));
		taskMonitor.showMessage(TaskMonitor.Level.INFO,  "Deleted row '"+keyValue+"'");
	}

	public List<Class<?>> getResultClasses() {	return Arrays.asList(String.class, JSONResult.class);	}
	public Object getResults(Class requestedType) {
		if (requestedType.equals(String.class))			return keyValue;
		if (requestedType.equals(JSONResult.class)) {
			JSONResult res = () -> {		
				if (table == null || keyValue == null) return "{}";
				return "{\"table\":"+table.getSUID()+",\"key\":\"" + keyValue + "\"}";	
			};	
			return res;
			}
		return null;
	}

}
