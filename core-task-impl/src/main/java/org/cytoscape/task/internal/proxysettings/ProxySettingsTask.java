package org.cytoscape.task.internal.proxysettings;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

/**
 * Dialog for assigning proxy settings.
 */
public class ProxySettingsTask extends AbstractTask {

	private final CyServiceRegistrar serviceRegistrar;

	public ProxySettingsTask(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor tm) {
		tm.setProgress(0.01);
		tm.setTitle("Set proxy server");
		tm.setStatusMessage("Setting proxy server...");

		// We run ProxySeting in another task, because TunableValidator is used.
		// If we run it in the same task, Cytoscape will be frozen during validating process
		ProxySettingsTask2 task = new ProxySettingsTask2(serviceRegistrar);
		insertTasksAfterCurrentTask(task);

		tm.setProgress(1.0);
	}
}
