package org.cytoscape.task.internal.layout;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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

public class SetPreferredLayoutTask extends AbstractTask {

	@Tunable(description = "Layout to use as preferred", context = "nogui")
	public ListSingleSelection<String> preferredLayout;
	
	private final CyServiceRegistrar serviceRegistrar;

	public SetPreferredLayoutTask(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		
		final List<String> layoutNames = new ArrayList<>();
		final CyLayoutAlgorithmManager layoutManager = serviceRegistrar.getService(CyLayoutAlgorithmManager.class);

		for (CyLayoutAlgorithm alg : layoutManager.getAllLayouts()) {
			if (!alg.getName().startsWith("yfiles."))
				layoutNames.add(alg.getName());
		}

		preferredLayout = new ListSingleSelection<>(layoutNames);
	}

	@Override
	public void run(TaskMonitor tm) {
		final CyLayoutAlgorithmManager layoutManager = serviceRegistrar.getService(CyLayoutAlgorithmManager.class);
		final String prefName = preferredLayout.getSelectedValue();
		final CyLayoutAlgorithm prefLayout = layoutManager.getLayout(prefName);

		if (prefLayout != null) {
			layoutManager.setDefaultLayout(prefLayout);
			tm.showMessage(TaskMonitor.Level.INFO, "Preferred layout set to " + prefName);
		} else {
			tm.showMessage(TaskMonitor.Level.WARN, "Can't set preferred layout -- invalid layout name");
		}
	}
}
