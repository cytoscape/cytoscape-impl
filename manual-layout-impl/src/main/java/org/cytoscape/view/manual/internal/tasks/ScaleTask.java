package org.cytoscape.view.manual.internal.tasks;

import java.util.Collection;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;;

import org.cytoscape.view.manual.internal.common.GraphConverter2;
import org.cytoscape.view.manual.internal.layout.algorithm.MutablePolyEdgeGraphLayout;
import org.cytoscape.view.manual.internal.scale.ScaleLayouter;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2023 The Cytoscape Consortium
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

public class ScaleTask extends AbstractTask {

  private final CyServiceRegistrar serviceRegistrar;

	@Tunable(description="The network to be rotated", context="nogui", longDescription="The name of the network to be rotated.  CURRENT may be used to indicate the currently selected network")
	public CyNetwork network;

	@Tunable(description="The scale factor", context="nogui", longDescription="The scale factor to apply to the network.")
	public double scaleFactor;

	@Tunable(description="Axis", context="nogui", longDescription="The axis to scale")
	public ListSingleSelection<String> axis = new ListSingleSelection<String>("X Axis", "Y Axis", "Both Axes");

	@Tunable(description="Only scale selected nodes", context="nogui", longDescription="Only scale the selected nodes")
	public boolean selectedOnly = false;

  public ScaleTask(CyServiceRegistrar serviceRegistrar) {
    this.serviceRegistrar = serviceRegistrar;
		axis.setSelectedValue("Both Axes");
  }

  @Override
	public void run(TaskMonitor tm) {
		tm.setTitle("Rotate Network View");

		// Get the view
		CyNetworkViewManager viewManager = serviceRegistrar.getService(CyNetworkViewManager.class);
		Collection<CyNetworkView> views = viewManager.getNetworkViews(network);
		CyNetworkView currentView = null;
		for (CyNetworkView view: views) {
			if (view.getRendererId().contains("ding")) {
				currentView = view;
				break;
			}
		}

		if (currentView == null) 
			currentView = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetworkView();

		if (currentView == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Can't find network view");
			return;
		}

		MutablePolyEdgeGraphLayout nativeGraph = GraphConverter2.getGraphReference(128.0d, true,
				selectedOnly, currentView);

		double scale = Math.pow(2, scaleFactor);

		ScaleLayouter.Direction direction = ScaleLayouter.Direction.BOTH_AXES;
		String dir = axis.getSelectedValue();
		if (dir.startsWith("X"))
			direction = ScaleLayouter.Direction.X_AXIS_ONLY;
		else if (dir.startsWith("Y"))
			direction = ScaleLayouter.Direction.Y_AXIS_ONLY;

		ScaleLayouter scaler = new ScaleLayouter(nativeGraph);

		scaler.scaleGraph(scale, direction);
	}
}
