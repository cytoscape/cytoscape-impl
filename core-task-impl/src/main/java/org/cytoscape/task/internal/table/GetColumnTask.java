package org.cytoscape.task.internal.table;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.TableTunable;
import org.cytoscape.util.json.CyJSONUtil;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

public class GetColumnTask extends AbstractTableDataTask implements ObservableTask {
	final CyApplicationManager appMgr;
	private final CyServiceRegistrar serviceRegistrar;
	CyColumn returnValue;

	@ContainsTunables
	public TableTunable tableTunable = null;

	@Tunable(description="Name of column", context="nogui")
	public String column = null;

	public GetColumnTask(CyApplicationManager appMgr, CyTableManager tableMgr, CyServiceRegistrar reg) {
		super(tableMgr);
		this.appMgr = appMgr;
		serviceRegistrar = reg;
		tableTunable = new TableTunable(tableMgr);
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		CyTable table = tableTunable.getTable();
		if (table == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR,  "Unable to find table '"+tableTunable.getTableString()+"'");
			return;
		}

		if (column == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR,  "Column name must be specified");
			return;
		}

		returnValue = table.getColumn(column);
		if (returnValue == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR,  "Column '"+column+"' doesn't exist in table: "+table.toString());
			return;
		}

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Retrieved column: "+returnValue.toString());
	}
	public List<Class<?>> getResultClasses() {	return Arrays.asList(CyColumn.class, String.class, JSONResult.class);	}
	
	public Object getResults(Class requestedType) {
		if (returnValue == null) return null;
		if (requestedType.equals(CyColumn.class)) 		return returnValue;
		if (requestedType.equals(String.class)) 		return returnValue.getName();
		if (requestedType.equals(JSONResult.class)) {
			JSONResult res = () -> {	if (returnValue == null) 		return "{}";
			CyJSONUtil cyJSONUtil = serviceRegistrar.getService(CyJSONUtil.class);
			return cyJSONUtil.toJson(returnValue, true, true);
		};
		return res;
		}
		return returnValue;
	}
}
