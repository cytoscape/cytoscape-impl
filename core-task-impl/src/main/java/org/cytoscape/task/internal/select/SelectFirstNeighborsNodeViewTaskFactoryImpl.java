package org.cytoscape.task.internal.select;

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

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.task.select.SelectFirstNeighborsNodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

public class SelectFirstNeighborsNodeViewTaskFactoryImpl extends AbstractNodeViewTaskFactory implements SelectFirstNeighborsNodeViewTaskFactory{

	private final Type direction;
	private final CyEventHelper cyEventHelper;

	public SelectFirstNeighborsNodeViewTaskFactoryImpl(final Type direction, CyEventHelper eventHelper) {
		super();
		this.direction = direction;
		this.cyEventHelper = eventHelper;
	}

	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView netView) {
		return new TaskIterator(new SelectFirstNeighborsNodeViewTask(nodeView, netView, direction, cyEventHelper));
	}
}
