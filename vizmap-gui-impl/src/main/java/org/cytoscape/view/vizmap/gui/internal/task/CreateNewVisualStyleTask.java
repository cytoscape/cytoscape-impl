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

import java.io.IOException;
import java.util.Iterator;

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateNewVisualStyleTask extends AbstractTask implements TunableValidator {

	private static final Logger logger = LoggerFactory.getLogger(CreateNewVisualStyleTask.class);

	@ProvidesTitle
	public String getTitle() {
		return "Create New Visual Style";
	}

	@Tunable(description = "Name of new Visual Style:")
	public String vsName;

	private final VisualStyleFactory vsFactory;
	private final VisualMappingManager vmm;

	public CreateNewVisualStyleTask(final VisualStyleFactory vsFactory, final VisualMappingManager vmm) {
		super();
		this.vsFactory = vsFactory;
		this.vmm = vmm;
	}

	public void run(TaskMonitor tm) {
		if (vsName == null)
			return;

		final VisualStyle style = vsFactory.createVisualStyle(vsName);

		vmm.addVisualStyle(style);
		vmm.setCurrentVisualStyle(style);
	}

	public ValidationState getValidationState(final Appendable errMsg) {
		Iterator<VisualStyle> it = this.vmm.getAllVisualStyles().iterator();

		while (it.hasNext()) {
			VisualStyle exist_vs = it.next();

			if (exist_vs.getTitle().equalsIgnoreCase(vsName)) {
				try {
					errMsg.append("Visual style " + vsName + " already existed.");
					return ValidationState.INVALID;
				} catch (IOException e) {
				}
			}
		}

		return ValidationState.OK;
	}
}