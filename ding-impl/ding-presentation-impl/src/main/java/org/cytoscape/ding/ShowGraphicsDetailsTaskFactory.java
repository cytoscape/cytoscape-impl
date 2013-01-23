package org.cytoscape.ding;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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
import org.cytoscape.ding.impl.DingGraphLOD;
import org.cytoscape.ding.impl.DingGraphLODAll;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.task.NetworkViewTaskFactory;

public class ShowGraphicsDetailsTaskFactory implements NetworkViewTaskFactory {

	private CyApplicationManager applicationManagerServiceRef;
	private final DingGraphLOD dingGraphLOD;
	private final DingGraphLODAll dingGraphLODAll;
	
	public ShowGraphicsDetailsTaskFactory(CyApplicationManager applicationManagerServiceRef, DingGraphLOD dingGraphLOD, DingGraphLODAll dingGraphLODAll){
		this.applicationManagerServiceRef = applicationManagerServiceRef;
		this.dingGraphLOD = dingGraphLOD;
		this.dingGraphLODAll = dingGraphLODAll;
	}
	
	
	public TaskIterator createTaskIterator(CyNetworkView networkView){
		return new TaskIterator(new ShowGraphicsDetailsTask(applicationManagerServiceRef, dingGraphLOD, dingGraphLODAll));
	}
	
	/**
	 * Returns true if this task factory is ready to produce a TaskIterator.
	 * @param networkView
	 * @return true if this task factory is ready to produce a TaskIterator.
	 */
	public boolean isReady(CyNetworkView networkView){
		return true;
	}
}
