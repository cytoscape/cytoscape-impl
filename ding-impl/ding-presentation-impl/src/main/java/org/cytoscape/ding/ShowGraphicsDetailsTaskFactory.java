package org.cytoscape.ding;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.DingGraphLOD;
import org.cytoscape.ding.impl.DingGraphLODAll;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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

public class ShowGraphicsDetailsTaskFactory implements NetworkViewTaskFactory {

	private final DingGraphLOD dingGraphLOD;
	private final DingGraphLODAll dingGraphLODAll;
	
	public ShowGraphicsDetailsTaskFactory(final DingGraphLOD dingGraphLOD, final DingGraphLODAll dingGraphLODAll) {
		this.dingGraphLOD = dingGraphLOD;
		this.dingGraphLODAll = dingGraphLODAll;
	}
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView view) {
		return new TaskIterator(new ShowGraphicsDetailsTask(view, dingGraphLOD, dingGraphLODAll));
	}
	
	@Override
	public boolean isReady(CyNetworkView view) {
		return view instanceof DGraphView;
	}
}
