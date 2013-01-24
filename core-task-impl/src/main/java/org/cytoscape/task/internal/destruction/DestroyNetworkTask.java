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

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.task.AbstractNetworkCollectionTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;


public class DestroyNetworkTask extends AbstractNetworkCollectionTask {
	private final CyNetworkManager netmgr;
	
	@Tunable(description="<html>Current network will be lost.<br />Do you want to continue?</html>", params="ForceSetDirectly=true")
	public boolean destroyCurrentNetwork = true;

	public DestroyNetworkTask(final Collection<CyNetwork> nets, final CyNetworkManager netmgr) {
		super(nets);
		this.netmgr = netmgr;
	}

	public void run(TaskMonitor tm) {
		
		int i=0;
		int networkCount;
		if(destroyCurrentNetwork)
		{
			tm.setProgress(0.0);
			networkCount = networks.size();
			for ( CyNetwork n : networks ){
				netmgr.destroyNetwork(n);
				i++;
				tm.setProgress((i/(double)networkCount));
			}
			tm.setProgress(1.0);
		}
	}
}
