package org.cytoscape.task.internal.layout;

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

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

public class SetPreferredLayoutTask extends AbstractTask {

	private final CyLayoutAlgorithmManager layouts;

	@Tunable(description="Layout to use as preferred", context="nogui")
	public ListSingleSelection<String> preferredLayout;

	public SetPreferredLayoutTask(final CyLayoutAlgorithmManager layouts) {
		this.layouts = layouts;
		List<String> layoutNames = new ArrayList<String>();
		
		for (CyLayoutAlgorithm alg: layouts.getAllLayouts()) {
			layoutNames.add(alg.getName());
		}
		
		preferredLayout = new ListSingleSelection<String>(layoutNames);
	}

	@Override
	public void run(TaskMonitor tm) {
		final String prefName = preferredLayout.getSelectedValue();
		final CyLayoutAlgorithm prefLayout = layouts.getLayout(prefName);

		if (prefLayout != null) {
			layouts.setDefaultLayout(prefLayout);
			tm.showMessage(TaskMonitor.Level.INFO, "Preferred layout set to " + prefName);
		} else {
			tm.showMessage(TaskMonitor.Level.WARN, "Can't set preferred layout -- invalid layout name");
		}
	}
}
