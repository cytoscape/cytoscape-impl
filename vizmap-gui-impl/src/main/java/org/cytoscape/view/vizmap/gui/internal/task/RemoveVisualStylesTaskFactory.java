package org.cytoscape.view.vizmap.gui.internal.task;

import java.util.Set;

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

/**
 * Creates a task that will remove the current {@link VisualStyle} or the passed ones.
 */
public class RemoveVisualStylesTaskFactory extends AbstractTaskFactory {

	private final ServicesUtil servicesUtil;

	public RemoveVisualStylesTaskFactory(ServicesUtil servicesUtil) {
		this.servicesUtil = servicesUtil;
	}

	@Override
	public TaskIterator createTaskIterator() {
		var style = servicesUtil.get(VisualMappingManager.class).getCurrentVisualStyle();
		
		return new TaskIterator(new RemoveVisualStylesTask(style, servicesUtil));
	}
	
	public TaskIterator createTaskIterator(Set<VisualStyle> styles) {
		return new TaskIterator(new RemoveVisualStylesTask(styles, servicesUtil));
	}
	
	@Override
	public boolean isReady() {
		var vmMgr = servicesUtil.get(VisualMappingManager.class);
		
		return !isDefaultVisualStyle(vmMgr.getCurrentVisualStyle());
	}
	
	public boolean isReady(Set<VisualStyle> styles) {
		if (styles == null || styles.isEmpty())
			return false;
		
		if (styles.size() == 1)
			return !isDefaultVisualStyle(styles.iterator().next());
		
		return true;
	}
	
	private boolean isDefaultVisualStyle(VisualStyle style) {
		var vmMgr = servicesUtil.get(VisualMappingManager.class);
		
		return vmMgr.getDefaultVisualStyle().equals(style);
	}
}
