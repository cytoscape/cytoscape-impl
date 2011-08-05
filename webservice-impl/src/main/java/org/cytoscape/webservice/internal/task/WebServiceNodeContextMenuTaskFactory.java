package org.cytoscape.webservice.internal.task;

import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.work.TaskIterator;

public class WebServiceNodeContextMenuTaskFactory extends AbstractNodeViewTaskFactory {
	
	WebServiceNodeContextMenuTaskFactory() {
		
	}

	@Override
	public TaskIterator getTaskIterator() {
		return new TaskIterator(new WebServiceContextMenuTask<CyNode>(nodeView));
	}

}
