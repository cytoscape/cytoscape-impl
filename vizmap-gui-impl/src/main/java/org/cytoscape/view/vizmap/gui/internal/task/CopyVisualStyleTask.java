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
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class CopyVisualStyleTask extends AbstractTask {

	@ProvidesTitle
	public String getTitle() {
		return "Copy Visual Style";
	}

	@Tunable(description = "Name of copied Visual Style:")
	public String vsName;

	private final VisualMappingManager vmm;
	private final VisualStyleFactory factory;

	public CopyVisualStyleTask(final VisualMappingManager vmm, final VisualStyleFactory factory) {
		this.factory = factory;
		this.vmm = vmm;
	}

	@Override
	public void run(TaskMonitor monitor) throws Exception {
		final VisualStyle originalStyle = vmm.getCurrentVisualStyle();

		// Ignore if user does not enter new name.
		if (vsName == null)
			return;

		final VisualStyle copiedStyle = factory.createVisualStyle(originalStyle);
		copiedStyle.setTitle(vsName);

		vmm.addVisualStyle(copiedStyle);
		vmm.setCurrentVisualStyle(copiedStyle);
	}
}
