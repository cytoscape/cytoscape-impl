package org.cytoscape.task.internal.table;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.TableTunable;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

public class DeleteColumnCommandTask extends AbstractTableDataTask implements ObservableTask {
	final CyApplicationManager appMgr;
	CyServiceRegistrar serviceRegistrar;
	private CyTable table = null;

	@ContainsTunables
	public TableTunable tableTunable = null;

	@Tunable(description="Name of column to delete", 
	                      longDescription=StringToModel.COLUMN_LONG_DESCRIPTION, 
	                      exampleStringValue = StringToModel.COLUMN_EXAMPLE, context="nogui")
	public String column = null;

	public DeleteColumnCommandTask(CyApplicationManager appMgr, CyTableManager tableMgr, CyServiceRegistrar reg) {
		super(tableMgr);
		this.appMgr = appMgr;
		serviceRegistrar = reg;
		tableTunable = new TableTunable(tableMgr);
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		table = tableTunable.getTable();
		if (table == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, 
			                        "Unable to find table '"+tableTunable.getTableString()+"'");
			return;
		}

		if (column == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, 
			                        "Column name must be specified");
			return;
		}

		CyColumn col = table.getColumn(column);
		if (col == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, 
			                        "Can't find a '"+column+"' column in table: "+table.toString());
			return;
		}

		if (col.isPrimaryKey()) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Can't delete primary key column");
			return;
		}

		table.deleteColumn(column);

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Deleted column: "+column);

	}

	@Override
	public List<Class<?>> getResultClasses() {	
		return Arrays.asList(CyColumn.class, String.class, JSONResult.class);	
	}

	@Override
	public Object getResults(Class requestedType) {
		if (requestedType.equals(String.class)) 		return (column == null) ? "" : column;
		if (requestedType.equals(JSONResult.class)) {
			JSONResult res = () -> {  
				if (table == null || column == null) return "{}";
			return   "{\"table\":"+table.getSUID()+",\"column\":\"" + column + "\"}";
		};
			return res;
			}
		return null;
	}

}
