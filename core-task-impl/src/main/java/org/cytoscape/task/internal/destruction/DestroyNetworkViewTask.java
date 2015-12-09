package org.cytoscape.task.internal.destruction;

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

import java.util.Collection;

import org.cytoscape.task.AbstractNetworkViewCollectionTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class DestroyNetworkViewTask extends AbstractNetworkViewCollectionTask {

	private final CyNetworkViewManager networkViewManager;
	
	@Tunable(description="<html>The selected views will be lost.<br />Do you want to continue?</html>", params="ForceSetDirectly=true")
	public boolean destroyCurrentNetworkView = true;

	public DestroyNetworkViewTask(final Collection<CyNetworkView> views, final CyNetworkViewManager networkViewManager) {
		super(views);
		this.networkViewManager = networkViewManager;
	}

	@Override
	public void run(TaskMonitor tm) {
		int i = 0;
		int viewCount;
		
		if (destroyCurrentNetworkView) {
			tm.setProgress(0.0);
			viewCount = networkViews.size();
			
			for (final CyNetworkView n : networkViews) {
				networkViewManager.destroyNetworkView(n);
				i++;
				tm.setProgress((i / (double) viewCount));
			}
			
			tm.setProgress(1.0);
		}
	}
}
