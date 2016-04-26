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
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.task.internal.utils.DataUtils;

public class SetNetworkPropertiesTask extends AbstractPropertyTask {
	@Tunable(description="Network to get properties for", context="nogui")
	public CyNetwork network = null;

	@Tunable(description="Properties to get the value for", context="nogui")
	public String propertyList = null;

	@Tunable(description="Values to set for the properties", context="nogui")
	public String valueList = null;

	public SetNetworkPropertiesTask(CyApplicationManager appMgr, CyNetworkViewManager viewManager,
	                                RenderingEngineManager reManager) {
		super(appMgr, viewManager, reManager);
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		if (network == null) {
			network = appManager.getCurrentNetwork();
		}

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

		int valueIndex = 0;
		for (String property: props) {
			String value = values[valueIndex];
			try {
				VisualProperty vp = getProperty(network, network, property.trim());
				setPropertyValue(network, network, vp, value);
				taskMonitor.showMessage(TaskMonitor.Level.INFO, DataUtils.getNetworkTitle(network)+" "+vp.getDisplayName()+" set to "+value);
			} catch (Exception e) {
				taskMonitor.showMessage(TaskMonitor.Level.ERROR, e.getMessage());
				return;
			}
		}
	
	}
}
