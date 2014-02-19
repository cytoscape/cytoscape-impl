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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.task.internal.utils.DataUtils;

public class ListNetworkViewsTask extends AbstractTask implements ObservableTask {
	final CyApplicationManager appMgr;
	private final CyNetworkViewManager viewMgr;
	private List<CyNetworkView> views;

	@Tunable(description="Network's views to list", context="nogui")
	public CyNetwork network = null;

	public ListNetworkViewsTask(CyApplicationManager appMgr,
	                            final CyNetworkViewManager viewMgr) {
		this.appMgr = appMgr;
		this.viewMgr = viewMgr;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (network == null) network = appMgr.getCurrentNetwork();
		views = new ArrayList<CyNetworkView>(viewMgr.getNetworkViews(network));
		
		taskMonitor.showMessage(TaskMonitor.Level.INFO, 
		                        "Views for network "+DataUtils.getNetworkTitle(network));
		for (CyNetworkView view: views) {
			taskMonitor.showMessage(TaskMonitor.Level.INFO, "    "+view);
			return;
		}
	}

	@Override
	public Object getResults(Class type) {
		if (type.equals(String.class))
			return DataUtils.convertData(views);
		return views;
	}
}
