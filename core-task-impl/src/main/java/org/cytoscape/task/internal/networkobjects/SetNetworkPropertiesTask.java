package org.cytoscape.task.internal.networkobjects;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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

public class SetNetworkPropertiesTask extends AbstractPropertyTask {

	@Tunable(description="Whether or not to lock the property", context="nogui", 
	         longDescription="Locking a visual property will override any mappings.  This is the same as the ```Bypass``` column in the user interface",
	         exampleStringValue="true")
	public boolean bypass = false;

	@Tunable(description="Network to set properties for", context="nogui",
	         longDescription=StringToModel.CY_NETWORK_LONG_DESCRIPTION, 
					 exampleStringValue=StringToModel.CY_NETWORK_EXAMPLE_STRING)
	public CyNetwork network = null;

	@Tunable(description="Properties to set the values for", context="nogui", 
	         longDescription="A comma-separated list of network properties", 
					 exampleStringValue="background paint,title",
	         required=true)
	public String propertyList = null;

	@Tunable(description="Values to set for the properties", context="nogui", required=true,
	         longDescription="A comma-separated list of property values.  This list must have "+
					                 "the same number of elements as the ``propertyList``.  Each value "+
					                 "will be applied to the property specified in the same position "+
					                 "in the ``propertyList``", 
					 exampleStringValue="white,My network title")
	public String valueList = null;

	public SetNetworkPropertiesTask(CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
	}

	@Override
	public void run(final TaskMonitor tm) {
		if (network == null) {
			network = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork();
			if (network == null) {
				tm.showMessage(TaskMonitor.Level.ERROR, "Network must be specified");
				throw new RuntimeException( "Network must be specified");
			}
		}

		if (propertyList == null || propertyList.length() == 0) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Property list must be specified");
			throw new RuntimeException( "Property list must be specified");
		}

		if (valueList == null || valueList.length() == 0) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Value list must be specified");
			throw new RuntimeException( "Value list must be specified");
		}

		String[] props = propertyList.split(",");
		String[] values = DataUtils.getCSV(valueList);
		if (props.length != values.length) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Property list and value list are not the same length");
			throw new RuntimeException( "Property list and value list are not the same length");
		}

		int valueIndex = 0;
		for (String property: props) {
			String value = values[valueIndex];
			try {
				VisualProperty vp = getProperty(network, network, property.trim());
				setPropertyValue(network, network, vp, value, bypass);
				tm.showMessage(TaskMonitor.Level.INFO, DataUtils.getNetworkName(network)+" "+vp.getDisplayName()+" set to "+value.toString());
			} catch (Exception e) {
				tm.showMessage(TaskMonitor.Level.ERROR, e.getMessage());
				throw new RuntimeException(e.getMessage());
			}
		}
	}
}
