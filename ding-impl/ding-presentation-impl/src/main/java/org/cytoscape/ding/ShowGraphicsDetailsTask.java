package org.cytoscape.ding;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.DingGraphLOD;
import org.cytoscape.ding.impl.DingGraphLODAll;
import org.cytoscape.graph.render.stateful.GraphLOD;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

public class ShowGraphicsDetailsTask extends AbstractTask {

	private final DRenderingEngine renderer;
	private final DingGraphLOD dingGraphLOD;
	private final DingGraphLODAll dingGraphLODAll;

	public ShowGraphicsDetailsTask(DRenderingEngine renderer, DingGraphLOD dingGraphLOD, DingGraphLODAll dingGraphLODAll) {
		this.renderer = renderer;
		this.dingGraphLOD = dingGraphLOD;
		this.dingGraphLODAll = dingGraphLODAll;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		final GraphLOD lod = renderer.getGraphLOD();

		if (lod instanceof DingGraphLODAll)
			renderer.setGraphLOD(dingGraphLOD);
		else
			renderer.setGraphLOD(dingGraphLODAll);
		
		renderer.updateView();
	}
}
