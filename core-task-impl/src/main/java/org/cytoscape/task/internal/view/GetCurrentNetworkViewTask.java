package org.cytoscape.task.internal.view;

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
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class GetCurrentNetworkViewTask extends AbstractTask 
                                       implements ObservableTask  {
	final CyApplicationManager appMgr;

	public GetCurrentNetworkViewTask(CyApplicationManager appMgr) {
		this.appMgr = appMgr;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Current network view is "+appMgr.getCurrentNetworkView());
	}

	@Override
	public Object getResults(Class requestedType) {
		// Support Collection<CyNetwork> or String
		if (requestedType.equals(String.class)) {
			return appMgr.getCurrentNetworkView().toString();
		} else
			return appMgr.getCurrentNetworkView();
	}

}
