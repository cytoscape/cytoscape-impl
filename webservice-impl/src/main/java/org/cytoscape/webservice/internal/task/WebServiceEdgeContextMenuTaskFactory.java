package org.cytoscape.webservice.internal.task;

import org.cytoscape.model.CyEdge;
import org.cytoscape.task.AbstractEdgeViewTaskFactory;
import org.cytoscape.work.TaskIterator;

public class WebServiceEdgeContextMenuTaskFactory extends AbstractEdgeViewTaskFactory {
	
	WebServiceEdgeContextMenuTaskFactory() {
		
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new WebServiceContextMenuTask<CyEdge>(edgeView));
	}

}
