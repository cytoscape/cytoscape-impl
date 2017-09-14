package org.cytoscape.task.internal.destruction;

import java.util.Collection;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewCollectionTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

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

public class DestroyNetworkViewTask extends AbstractNetworkViewCollectionTask {

	@Tunable(
			description = "Deprecated",
			longDescription = "Deprecated since version 3.6.",
			context = "nogui"
	)
	@Deprecated
	public boolean destroyCurrentNetworkView = true;
	
	@Tunable(
			description = "<html>The selected views will be lost.<br />Do you want to continue?</html>",
			params = "ForceSetDirectly=true;ForceSetTitle=Destroy Views",
			context = "gui"
	)
	public boolean confirm = true;

	private final CyServiceRegistrar serviceRegistrar;

	public DestroyNetworkViewTask(Collection<CyNetworkView> views, CyServiceRegistrar serviceRegistrar) {
		super(views);
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor tm) {
		int i = 0;
		int viewCount;
		
		if (confirm && destroyCurrentNetworkView) { // Also checks destroyCurrentNetworkView for backwards compatibility
			tm.setProgress(0.0);
			final CyNetworkViewManager viewManager = serviceRegistrar.getService(CyNetworkViewManager.class);
			viewCount = networkViews.size();
			
			for (final CyNetworkView n : networkViews) {
				viewManager.destroyNetworkView(n);
				i++;
				tm.setProgress((i / (double) viewCount));
			}
			
			tm.setProgress(1.0);
		}
	}
}
