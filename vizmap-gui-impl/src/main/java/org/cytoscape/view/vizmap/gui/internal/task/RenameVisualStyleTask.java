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
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class RenameVisualStyleTask extends AbstractTask {

	@ProvidesTitle
	public String getTitle() {
		return "Rename Visual Style";
	}

	@Tunable(description = "Enter new Visual Style name:")
	public String vsName;

	private final VisualMappingManager vmm;

	public RenameVisualStyleTask(final VisualMappingManager vmm) {
		this.vmm = vmm;
		this.vsName = vmm.getCurrentVisualStyle().getTitle();
	}

	@Override
	public void run(TaskMonitor monitor) throws Exception {

		final VisualStyle currentStyle = vmm.getCurrentVisualStyle();

		if (currentStyle.equals(this.vmm.getDefaultVisualStyle()))
			throw new IllegalArgumentException("You cannot rename the default style.");

		// Ignore if user does not enter new name.
		if (vsName == null)
			return;

		currentStyle.setTitle(vsName);
	}

}
