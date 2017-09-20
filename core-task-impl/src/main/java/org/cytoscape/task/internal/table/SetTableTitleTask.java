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
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

public class SetTableTitleTask extends AbstractTableDataTask {
	final CyApplicationManager appMgr;
	private final CyServiceRegistrar serviceRegistrar;

	@ContainsTunables
	public TableTunable tableTunable = null;

	@Tunable(description="New table title", context="nogui")
	public String title = null;

	public SetTableTitleTask(CyApplicationManager appMgr, CyTableManager tableMgr, CyServiceRegistrar reg) {
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

		if (title == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR,  "New title must be specified");
			return;
		}

		String oldTitle = table.getTitle();
		table.setTitle(title);
		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Changed title of table '"+oldTitle+"' to '"+title+"'");
	}
	public List<Class<?>> getResultClasses() {	return Arrays.asList(CyColumn.class, String.class, JSONResult.class);	}
	public Object getResults(Class requestedType) {
		if (requestedType.equals(String.class)) 		return "";
		if (requestedType.equals(JSONResult.class)) {
			JSONResult res = () -> {		return "{}";	};	}
		return null;
	}

}
