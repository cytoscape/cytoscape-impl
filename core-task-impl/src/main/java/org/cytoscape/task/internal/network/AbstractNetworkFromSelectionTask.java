package org.cytoscape.task.internal.network;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.task.internal.view.CreateNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskMonitor;

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

abstract class AbstractNetworkFromSelectionTask extends AbstractCreationTask {
	
	protected final CyApplicationManager applicationManager;
	protected final CyRootNetworkManager rootNetManager;
	protected final CyNetworkManager netManager;
	protected final CyNetworkViewManager viewManager;
	protected final CyNetworkViewFactory viewFactory;
	protected final CyNetworkNaming netNaming;
	protected final CyGroupManager groupManager;
	protected final CyEventHelper eventHelper;
	
	protected CySubNetwork newNet;

	public AbstractNetworkFromSelectionTask(CyNetwork parentNetwork, CyServiceRegistrar serviceRegistrar) {
		super(parentNetwork, serviceRegistrar);

		applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
		rootNetManager = serviceRegistrar.getService(CyRootNetworkManager.class);
		netManager = serviceRegistrar.getService(CyNetworkManager.class);
		viewManager = serviceRegistrar.getService(CyNetworkViewManager.class);
		viewFactory = serviceRegistrar.getService(CyNetworkViewFactory.class);
		netNaming = serviceRegistrar.getService(CyNetworkNaming.class);
		groupManager = serviceRegistrar.getService(CyGroupManager.class);
		eventHelper = serviceRegistrar.getService(CyEventHelper.class);
	}

	abstract Set<CyNode> getNodes(CyNetwork net);
	
	abstract Set<CyEdge> getEdges(CyNetwork net);

	String getNetworkName() {
		return netNaming.getSuggestedSubnetworkTitle(parentNetwork);
	}

	@Override
	public void run(TaskMonitor tm) {
		tm.setProgress(0.0);
		
		if (parentNetwork == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Source network must be specified.");
			return;
		}
		
		var views = viewManager.getNetworkViews(parentNetwork);		
		CyNetworkView sourceView = null;
		
		if (views.size() != 0)
			sourceView = views.iterator().next();
		
		tm.setProgress(0.1);

		// Get the selected nodes
		var nodes = getNodes(parentNetwork);
		tm.setProgress(0.2);

		if (nodes.isEmpty()) // return;
			throw new IllegalArgumentException("No nodes are selected.");

		// create subnetwork and add selected nodes and appropriate edges
		newNet = rootNetManager.getRootNetwork(parentNetwork).addSubNetwork();
		
		//We need to cpy the columns to local tables, since copying them to default table will duplicate the virtual columns.
		addColumns(parentNetwork.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS), newNet.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS));
		addColumns(parentNetwork.getTable(CyEdge.class, CyNetwork.LOCAL_ATTRS), newNet.getTable(CyEdge.class, CyNetwork.LOCAL_ATTRS) );
		addColumns(parentNetwork.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS), newNet.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS));

		tm.setProgress(0.3);
		
		for (var node : nodes){
			newNet.addNode(node);
			cloneRow(parentNetwork.getRow(node), newNet.getRow(node));
			//Set rows and edges to not selected state to avoid conflicts with table browser
			newNet.getRow(node).set(CyNetwork.SELECTED, false);
			
			if (groupManager.isGroup(node, parentNetwork)) {
				var group = groupManager.getGroup(node, parentNetwork);
				GroupUtils.addGroupToNetwork(group, parentNetwork, newNet);
			}
		}

		tm.setProgress(0.4);
		
		for (var edge : getEdges(parentNetwork)){
			newNet.addEdge(edge);
			cloneRow(parentNetwork.getRow(edge), newNet.getRow(edge));
			//Set rows and edges to not selected state to avoid conflicts with table browser
			newNet.getRow(edge).set(CyNetwork.SELECTED, false);
		}
		
		tm.setProgress(0.5);
		
		newNet.getRow(newNet).set(CyNetwork.NAME, getNetworkName());
		DataUtils.saveParentNetworkSUID(newNet, parentNetwork.getSUID());

		netManager.addNetwork(newNet, false);
		tm.setProgress(0.6);

		// create the view in a separate task
		var networks = new HashSet<CyNetwork>();
		networks.add(newNet);
		
		// Pick a CyNetworkViewFactory that is appropriate for the sourceView
		var sourceViewFactory = viewFactory;
		
		if (sourceView != null) {
			var networkViewRenderer = applicationManager.getNetworkViewRenderer(sourceView.getRendererId());
			
			if (networkViewRenderer != null)
				sourceViewFactory = networkViewRenderer.getNetworkViewFactory();
		}
		
		var createViewTask = new CreateNetworkViewTask(networks, sourceViewFactory, netManager, null,
				applicationManager, sourceView, serviceRegistrar);
		insertTasksAfterCurrentTask(createViewTask);
		/*
		insertTasksAfterCurrentTask(createViewTask, new AbstractTask() {
			@Override
			@SuppressWarnings("unchecked")
			public void run(final TaskMonitor tm) throws Exception {
				// Select the new view
				tm.setProgress(0.0);
				List<CyNetworkView> createdViews = (List<CyNetworkView>) createViewTask.getResults(List.class);
				
				if (!createdViews.isEmpty()) {
					CyNetworkView nv = createdViews.get(createdViews.size() - 1);

					if (nv != null) {
						insertTasksAfterCurrentTask(new RegisterNetworkTask(nv, null, netManager, visMapManager, appMgr, viewManager));
						return;
					}
				}
				insertTasksAfterCurrentTask(new RegisterNetworkTask(newNet, netManager, visMapManager, appMgr, viewManager));
				
				tm.setProgress(1.0);
			}
		});
		*/
		
		tm.setProgress(1.0);
	}

	private void addColumns(CyTable parentTable, CyTable subTable) {
		var colsToAdd = new ArrayList<CyColumn>();

		for (var col :  parentTable.getColumns())
			if (subTable.getColumn(col.getName()) == null)
				colsToAdd.add( col );

		for (var col :  colsToAdd) {
			var colInfo = col.getVirtualColumnInfo();
			
			if (colInfo.isVirtual())
				addVirtualColumn(col, subTable);
			else
				copyColumn(col, subTable);
		}
	}

	private void addVirtualColumn (CyColumn col, CyTable subTable){
		var colInfo = col.getVirtualColumnInfo();
		var checkCol = subTable.getColumn(col.getName());
		
		if (checkCol == null)
			subTable.addVirtualColumn(col.getName(), colInfo.getSourceColumn(), colInfo.getSourceTable(), 
					colInfo.getTargetJoinKey(), col.isImmutable());

		else
			if (!checkCol.getVirtualColumnInfo().isVirtual() ||
					!checkCol.getVirtualColumnInfo().getSourceTable().equals(colInfo.getSourceTable()) ||
					!checkCol.getVirtualColumnInfo().getSourceColumn().equals(colInfo.getSourceColumn()))
				subTable.addVirtualColumn(col.getName(), colInfo.getSourceColumn(), colInfo.getSourceTable(), 
						colInfo.getTargetJoinKey(), col.isImmutable());
	}

	private void copyColumn(CyColumn col, CyTable subTable) {
		if (List.class.isAssignableFrom(col.getType()))
			subTable.createListColumn(col.getName(), col.getListElementType(), false);
		else
			subTable.createColumn(col.getName(), col.getType(), false);	
	}

	private void cloneRow(CyRow from, CyRow to) {
		for (var column : from.getTable().getColumns()){
			if (!column.getVirtualColumnInfo().isVirtual())
				to.set(column.getName(), from.getRaw(column.getName()));
		}
	}
}
