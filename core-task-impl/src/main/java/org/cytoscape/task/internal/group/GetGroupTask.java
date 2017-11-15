package org.cytoscape.task.internal.group;

import java.util.Arrays;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2012 - 2013 The Cytoscape Consortium
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

import java.util.List;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.CoreImplDocumentationConstants;
import org.cytoscape.util.json.CyJSONUtil;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

public class GetGroupTask extends AbstractGroupTask implements ObservableTask {
	private CyApplicationManager appMgr;
	
	private CyGroup group = null;
	private CyServiceRegistrar serviceRegistrar;
	private CyJSONUtil jsonUtil;
	private static int groupNumber = 1;

	@Tunable(description="Network", context="nogui", longDescription=StringToModel.CY_NETWORK_LONG_DESCRIPTION, exampleStringValue=StringToModel.CY_NETWORK_EXAMPLE_STRING)
	public CyNetwork network = null;

	@Tunable(description="Group to get", context="nogui", longDescription=CoreImplDocumentationConstants.NODE_LONG_DESCRIPTION, exampleStringValue="Node 1")
	public String node = null;

	public GetGroupTask(CyApplicationManager appMgr, 
	                    CyGroupManager mgr, CyServiceRegistrar serviceRegistrar) {
		this.appMgr = appMgr;
		this.groupMgr = mgr;
		this.serviceRegistrar = serviceRegistrar;
		
	}

	public void run(TaskMonitor tm) throws Exception {
		tm.setProgress(0.0);
		if (network == null) network = appMgr.getCurrentNetwork();
		net = network;

		group = getGroup(node);
		if (group == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Can't find a group with that group node "+node);
			return;
		}
		// Now find the corresponding group
		//group = mgr.getGroup(returnedNode.getGroupNode(), network);

		// mgr.addGroup(group);
		tm.setProgress(1.0d);
	}

	public Object getResults(Class requestedType) {
		if (group == null) return null;
		if (requestedType.equals(CyGroup.class))		return group;
		if (requestedType.equals(String.class))			return group.toString();
		if (requestedType.equals(JSONResult.class))  {
			JSONResult res = () -> { 
				if (group == null) return "{}";
				return groupJSON(network, group);
			};
			return res;
		}
		return null;
	}
	public List<Class<?>> getResultClasses() {	return Arrays.asList(String.class, CyGroup.class, JSONResult.class);	}

	public static String EXAMPLE_JSON = "{\"group\":1234,\"name\":\"my group\",\"nodes\":[122,123,124,125],"+
	                                    "\"externalEdges\":[201,202,203],\"internalEdges\":[300,301],\"collapsed\": false}";

	public String groupJSON(CyNetwork network, CyGroup group) {
		this.jsonUtil = serviceRegistrar.getService(CyJSONUtil.class);
		long suid = group.getGroupNode().getSUID();
		List<CyNode> nodes = group.getNodeList();
		List<CyEdge> internalEdges = group.getInternalEdgeList();
		Set<CyEdge> externalEdges = group.getExternalEdgeList();
		CyRow groupRow = ((CySubNetwork)net).getRootNetwork().getRow(group.getGroupNode(), CyRootNetwork.SHARED_ATTRS);
		String name = groupRow.get(CyRootNetwork.SHARED_NAME, String.class);
		String result = "{\"group\":"+suid+","+"\"name\":\""+name+"\",";
		result += "\"nodes\":"+jsonUtil.cyIdentifiablesToJson(nodes)+",";
		result += "\"internalEdges\":"+jsonUtil.cyIdentifiablesToJson(internalEdges)+",";
		result += "\"externalEdges\":"+jsonUtil.cyIdentifiablesToJson(externalEdges)+",";
		result += "\"collapsed\":";
		if (group.isCollapsed(network))
			result += "true";
		else
			result += "false";
		result += "}";
		return result;
	}

}
