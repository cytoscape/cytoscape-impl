package org.cytoscape.internal.command;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class PauseCommandTaskFactory extends AbstractTaskFactory {
	
	private final CySwingApplication swingApplication;
	
	public PauseCommandTaskFactory(CySwingApplication swingApplication) {
		this.swingApplication = swingApplication;
	}
	
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new PauseCommandTask(swingApplication));
	}

}
