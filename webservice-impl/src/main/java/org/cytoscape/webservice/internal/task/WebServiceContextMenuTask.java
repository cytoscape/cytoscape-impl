package org.cytoscape.webservice.internal.task;

import org.cytoscape.model.CyTableEntry;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class WebServiceContextMenuTask<T extends CyTableEntry> extends AbstractTask {
	
	private final View<T> view;
	
	public WebServiceContextMenuTask(final View<T> view) {
		this.view = view;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
