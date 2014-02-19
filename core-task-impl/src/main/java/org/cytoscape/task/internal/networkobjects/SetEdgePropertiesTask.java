package org.cytoscape.task.internal.networkobjects;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.task.internal.utils.EdgeTunable;
import org.cytoscape.task.internal.utils.DataUtils;

public class SetEdgePropertiesTask extends AbstractPropertyTask {

	@ContainsTunables
	public EdgeTunable edgeTunable;

	@Tunable(description="Properties to get the value for", context="nogui")
	public String propertyList = null;

	@Tunable(description="Values to set for the properties", context="nogui")
	public String valueList = null;

	public SetEdgePropertiesTask(CyApplicationManager appMgr, CyNetworkViewManager viewManager,
	                             RenderingEngineManager reManager) {
		super(appMgr, viewManager, reManager);
		edgeTunable = new EdgeTunable(appMgr);
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {

		CyNetwork network = edgeTunable.getNetwork();

		if (propertyList == null || propertyList.length() == 0) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Property list must be specified");
			return;
		}

		if (valueList == null || valueList.length() == 0) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Value list must be specified");
			return;
		}

		String[] props = propertyList.split(",");
		String[] values = DataUtils.getCSV(valueList);
		if (props.length != values.length) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Property list and value list are not the same length");
			return;
		}

		for (CyEdge edge: edgeTunable.getEdgeList()) {
			taskMonitor.showMessage(TaskMonitor.Level.INFO,
			                        "   Setting properties for edge "+DataUtils.getEdgeName(network.getDefaultEdgeTable(), edge));
			int valueIndex = 0;
			for (String property: props) {
				String value = values[valueIndex];
				try {
					VisualProperty vp = getProperty(network, edge, property.trim());
					setPropertyValue(network, edge, vp, value);
					taskMonitor.showMessage(TaskMonitor.Level.INFO, "       "+vp.getDisplayName()+" set to "+value.toString());
				} catch (Exception e) {
					taskMonitor.showMessage(TaskMonitor.Level.ERROR, e.getMessage());
					return;
				}
			}
		}
	
	}
}
