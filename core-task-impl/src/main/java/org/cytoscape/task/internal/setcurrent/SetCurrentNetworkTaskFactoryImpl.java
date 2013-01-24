package org.cytoscape.task.internal.setcurrent;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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


import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;


// TODO Verify that we need this class in headless mode!
/**
 * This TaskFactory is for headless mode and not GUI mode. This
 * factory shouldn't be registered by the swing GUI as it doesn't
 * make sense in that context.
 */
public class SetCurrentNetworkTaskFactoryImpl extends AbstractTaskFactory {
	private final CyApplicationManager applicationManager;
	private final CyNetworkManager netmgr;

	public SetCurrentNetworkTaskFactoryImpl(final CyApplicationManager applicationManager,
						final CyNetworkManager netmgr)
	{
		this.applicationManager = applicationManager;
		this.netmgr = netmgr;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new SetCurrentNetworkTask(applicationManager, netmgr));
	}
}
