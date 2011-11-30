package org.cytoscape.editor.internal;


import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.work.TaskIterator;


public class SIFInterpreterTaskFactory extends AbstractNetworkViewTaskFactory {
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new SIFInterpreterTask(view));
	}
}

