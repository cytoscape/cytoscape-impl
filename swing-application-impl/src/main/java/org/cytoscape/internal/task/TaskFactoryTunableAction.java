package org.cytoscape.internal.task;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.awt.event.ActionEvent;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskFactoryTunableAction extends AbstractCyAction {

	private static final long serialVersionUID = 8009915597814265396L;

	private final static Logger logger = LoggerFactory.getLogger(TaskFactoryTunableAction.class);

	final protected TaskFactory factory;
	final protected DialogTaskManager manager;

	/**
	 * This constructor forces the action to use the TaskFactory.isReady() method
	 * to determine whether the action should be enabled.
	 */
	public TaskFactoryTunableAction(final DialogTaskManager manager, 
	                                final TaskFactory factory,
	                                final Map<String, String> serviceProps) {
		super(serviceProps, factory);
		this.manager = manager;
		this.factory = factory;
	}

	/**
	 * This constructor forces the action to use the "enableFor" property in serviceProps 
	 * to determine whether the action should be enabled.
	 */
	public TaskFactoryTunableAction(final DialogTaskManager manager, 
	                                final TaskFactory factory,
	                                final Map<String, String> serviceProps, 
	                                final CyApplicationManager applicationManager, 
	                                final CyNetworkViewManager networkViewManager) {
		super(serviceProps, applicationManager, networkViewManager, factory);
		this.manager = manager;
		this.factory = factory;
	}

	@Override
	public void actionPerformed(ActionEvent a) {
		logger.debug("About to execute task from factory: " + factory.toString());

		// execute the task(s) in a separate thread
		manager.execute(factory.createTaskIterator());
	}
}
