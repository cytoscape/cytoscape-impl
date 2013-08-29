package org.cytoscape.view.vizmap.gui.internal.task;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class RenameVisualStyleTaskFactory extends AbstractTaskFactory {

	private final ServicesUtil servicesUtil;

	public RenameVisualStyleTaskFactory(final ServicesUtil servicesUtil) {
		this.servicesUtil = servicesUtil;
	}

	@Override
	public TaskIterator createTaskIterator() {
		final VisualMappingManager vmm = servicesUtil.get(VisualMappingManager.class);
		
		return new TaskIterator(new RenameVisualStyleTask(vmm.getCurrentVisualStyle(), servicesUtil));
	}
	
	@Override
	public boolean isReady() {
		final VisualMappingManager vmm = servicesUtil.get(VisualMappingManager.class);
		
		return vmm != null && !vmm.getDefaultVisualStyle().equals(vmm.getCurrentVisualStyle());
	}
}
