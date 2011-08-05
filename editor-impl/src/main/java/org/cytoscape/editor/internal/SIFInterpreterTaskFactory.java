package org.cytoscape.editor.internal;


import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.work.TaskIterator;


public class SIFInterpreterTaskFactory extends AbstractNetworkViewTaskFactory {
	public TaskIterator getTaskIterator() {
		return new TaskIterator(new SIFInterpreterTask(view));
	}
}

