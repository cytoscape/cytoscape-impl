package org.cytoscape.ding;

import org.cytoscape.ding.impl.DingGraphLOD;
import org.cytoscape.ding.impl.DingGraphLODAll;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskIterator;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class ShowGraphicsDetailsTaskFactory implements NetworkTaskFactory {

	private final DingGraphLOD dingGraphLOD;
	private final DingGraphLODAll dingGraphLODAll;
	private final CyServiceRegistrar serviceRegistrar;
	
	public ShowGraphicsDetailsTaskFactory(final DingGraphLOD dingGraphLOD, final DingGraphLODAll dingGraphLODAll,
			final CyServiceRegistrar serviceRegistrar){
		this.dingGraphLOD = dingGraphLOD;
		this.dingGraphLODAll = dingGraphLODAll;
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public TaskIterator createTaskIterator(CyNetwork network){
		return new TaskIterator(new ShowGraphicsDetailsTask(dingGraphLOD, dingGraphLODAll, serviceRegistrar));
	}
	
	@Override
	public boolean isReady(CyNetwork network){
		return true;
	}
}
