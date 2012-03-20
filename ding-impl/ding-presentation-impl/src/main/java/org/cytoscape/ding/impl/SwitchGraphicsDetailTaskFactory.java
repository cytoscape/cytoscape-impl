package org.cytoscape.ding.impl;


import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;


public class SwitchGraphicsDetailTaskFactory extends AbstractTaskFactory {
	private final CyApplicationManager appManager;
	private final CyProperty<Properties> defaultProps;

	public SwitchGraphicsDetailTaskFactory(final CyApplicationManager appManager, final CyProperty<Properties> defaultProps) {
		this.appManager   = appManager;
		this.defaultProps = defaultProps;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new SwitchGraphicsDetailTask(defaultProps, appManager));
	}

}
