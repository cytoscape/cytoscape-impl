package org.cytoscape.task.internal.networkobjects;

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
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;


public class SetPropertiesTaskFactory extends AbstractTaskFactory {
	
	//Set statements don't have to return anything, much like a RESTful PUT.
	public static final String COMMAND_EXAMPLE_JSON = "{}";
	
	Class <? extends CyIdentifiable> type;
	CyApplicationManager appMgr;
	CyNetworkViewManager viewManager;
	RenderingEngineManager reManager;

	public SetPropertiesTaskFactory(CyApplicationManager appMgr, 
	                                Class <? extends CyIdentifiable> type,
 	                                CyNetworkViewManager viewManager,
	                                RenderingEngineManager reManager) {
		this.type = type;
		this.appMgr = appMgr;
		this.viewManager = viewManager;
		this.reManager = reManager;
	}

	public TaskIterator createTaskIterator() {
		if (type.equals(CyNetwork.class))
			return new TaskIterator(new SetNetworkPropertiesTask(appMgr, viewManager, reManager));
		else if (type.equals(CyNode.class))
			return new TaskIterator(new SetNodePropertiesTask(appMgr, viewManager, reManager));
		else if (type.equals(CyEdge.class))
			return new TaskIterator(new SetEdgePropertiesTask(appMgr, viewManager, reManager));
		return null;
	}
}
