package org.cytoscape.internal.task;

import java.awt.event.ActionEvent;
import java.util.Map;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

@SuppressWarnings("serial")
public class TaskFactoryTunableAction extends AbstractCyAction {

	private final static Logger logger = LoggerFactory.getLogger(TaskFactoryTunableAction.class);

	private final TaskFactory factory;
	private final CyServiceRegistrar serviceRegistrar;

	/**
	 * This constructor forces the action to use the TaskFactory.isReady() method
	 * to determine whether the action should be enabled.
	 */
	public TaskFactoryTunableAction(final CyServiceRegistrar serviceRegistrar, final TaskFactory factory,
			final Map<String, String> serviceProps) {
		super(serviceProps, factory);
		this.factory = factory;
		this.serviceRegistrar = serviceRegistrar;
	}

	/**
	 * This constructor forces the action to use the "enableFor" property in serviceProps 
	 * to determine whether the action should be enabled.
	 */
	public TaskFactoryTunableAction(final TaskFactory factory, final Map<String, String> serviceProps,
			final CyServiceRegistrar serviceRegistrar) {
		super(serviceProps, factory);
		this.factory = factory;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void actionPerformed(ActionEvent a) {
		logger.debug("About to execute task from factory: " + factory.toString());

		// execute the task(s) in a separate thread
		final DialogTaskManager taskManager = serviceRegistrar.getService(DialogTaskManager.class);
		taskManager.execute(factory.createTaskIterator());
	}
}
