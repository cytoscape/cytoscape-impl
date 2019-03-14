package org.cytoscape.ding;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.DingGraphLOD;
import org.cytoscape.ding.impl.DingGraphLODAll;
import org.cytoscape.graph.render.stateful.GraphLOD;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

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

public class ShowGraphicsDetailsTask extends AbstractTask {

	private final CyNetworkView view;
	private final DingGraphLOD dingGraphLOD;
	private final DingGraphLODAll dingGraphLODAll;

	public ShowGraphicsDetailsTask(CyNetworkView view, DingGraphLOD dingGraphLOD, DingGraphLODAll dingGraphLODAll) {
		this.view = view;
		this.dingGraphLOD = dingGraphLOD;
		this.dingGraphLODAll = dingGraphLODAll;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		if (view instanceof DGraphView) {
			final GraphLOD lod = ((DGraphView) view).getGraphLOD();
	
			if (lod instanceof DingGraphLODAll)
				((DGraphView) view).setGraphLOD(dingGraphLOD);
			else
				((DGraphView) view).setGraphLOD(dingGraphLODAll);
			
			view.updateView();
		}
	}
}
