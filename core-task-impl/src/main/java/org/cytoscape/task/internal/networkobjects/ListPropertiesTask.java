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

import java.util.Arrays;
import java.util.List;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.task.internal.utils.DataUtils;

public class ListPropertiesTask extends AbstractPropertyTask implements ObservableTask {
	Class <? extends CyIdentifiable> type;
	List<String> resultList;

	@Tunable(description="Network to get properties for", 
	         context="nogui", 
	         longDescription=StringToModel.CY_NETWORK_LONG_DESCRIPTION, 
					 exampleStringValue=StringToModel.CY_NETWORK_EXAMPLE_STRING)
	public CyNetwork network = null;

	public ListPropertiesTask(CyApplicationManager appMgr, Class<? extends CyIdentifiable> type,
 	                          CyNetworkViewManager viewManager,
	                          RenderingEngineManager reManager) {
		super(appMgr, viewManager, reManager);
		this.type = type;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		if (network == null) {
			network = appManager.getCurrentNetwork();
			if (network == null) {
				taskMonitor.showMessage(TaskMonitor.Level.ERROR, 
				                        "No network");
				return;
			}
		}
	
		resultList = listProperties(type, network);

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Properties for "+DataUtils.getIdentifiableType(type)+"s:");
		for (String prop: resultList) {
			taskMonitor.showMessage(TaskMonitor.Level.INFO, "     "+prop);
		}
	}

	public Object getResults(Class type) {
		if (type.equals(JSONResult.class)) {
			JSONResult res = () -> {
				StringBuilder stringBuilder = new StringBuilder("[");
				int count = resultList.size();
				for (String column : resultList) {
					stringBuilder.append("\"" + column + "\"");
					if (count > 1) {
						stringBuilder.append(",");
					}
					count--;
				}
				stringBuilder.append("]");
				return stringBuilder.toString();
			};
			return res;
		} else if (type.equals(String.class)) {
			StringBuilder stringBuilder = new StringBuilder("Properties for "+DataUtils.getIdentifiableType(type)+"s:");
			int count = resultList.size();
			for (String column : resultList) {
				stringBuilder.append("    " + column + "\n");
			}
			return stringBuilder.toString();
		}
		return resultList;
	}
	
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(List.class, String.class, JSONResult.class);
	}
}
