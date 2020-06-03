package org.cytoscape.view.vizmap.gui.internal.view.table;

import org.cytoscape.model.CyColumn;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperTableMediator;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class TableStyleDialogTask extends AbstractTask {

	private final CyColumn column;
	private final VizMapperTableMediator mediator;
	
	public TableStyleDialogTask(CyColumn column, VizMapperTableMediator mediator) {
		this.column = column;
		this.mediator = mediator;
	}

	@Override
	public void run(TaskMonitor tm) {
		mediator.showDialogFor(column);
	}

}
