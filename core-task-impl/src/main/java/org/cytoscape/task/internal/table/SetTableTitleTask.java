package org.cytoscape.task.internal.table;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.TableTunable;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

public class SetTableTitleTask extends AbstractTableDataTask implements ObservableTask {
	final CyApplicationManager appMgr;
	private final CyServiceRegistrar serviceRegistrar;
	private CyTable table = null;

	@ContainsTunables
	public TableTunable tableTunable = null;

	@Tunable(description="New table title", context="nogui",
			longDescription=StringToModel.TABLE_TITLE_LONG_DESCRIPTION, exampleStringValue = "Filtered Edges")
	public String title = null;

	public SetTableTitleTask(CyApplicationManager appMgr, CyTableManager tableMgr, CyServiceRegistrar reg) {
		super(tableMgr);
		this.appMgr = appMgr;
		serviceRegistrar = reg;
		tableTunable = new TableTunable(tableMgr);
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		table = tableTunable.getTable();
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

	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class, JSONResult.class);	
	}

	@Override
	public Object getResults(Class requestedType) {
		if (requestedType.equals(String.class)) 		return title;
		if (requestedType.equals(JSONResult.class)) {
			JSONResult res = () -> {
				if (table == null) return "{}";
				return "{\"table\":"+table.getSUID()+", \"title\":\""+title+"\"}";	
			};
		return res;
	}
	return null;
}

}
