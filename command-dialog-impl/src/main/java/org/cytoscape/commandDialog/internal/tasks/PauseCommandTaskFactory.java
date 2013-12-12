package org.cytoscape.commandDialog.internal.tasks;

import javax.swing.JFrame;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class PauseCommandTaskFactory extends AbstractTaskFactory {
	JFrame parent;
	
	public PauseCommandTaskFactory(JFrame parentFrame) {
		super();
		this.parent = parentFrame;
	}
	
	public boolean isReady() {
		return true;
	}
	
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new PauseCommandTask(parent));
	}

}
