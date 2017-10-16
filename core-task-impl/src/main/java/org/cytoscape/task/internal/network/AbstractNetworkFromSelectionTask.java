package org.cytoscape.task.internal.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.VirtualColumnInfo;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.task.internal.view.CreateNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

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

abstract class AbstractNetworkFromSelectionTask extends AbstractCreationTask {
	
	private final UndoSupport undoSupport;
	protected final CyRootNetworkManager rootNetMgr;
	protected final CyNetworkViewFactory viewFactory;
	protected final VisualMappingManager vmMgr;
	protected final CyNetworkNaming networkNaming;
	protected final CyApplicationManager appMgr;
	private final CyEventHelper eventHelper;
	private final RenderingEngineManager renderingEngineMgr;
	protected final CyGroupManager groupMgr;
	protected final CyServiceRegistrar serviceRegistrar;
	protected CySubNetwork newNet;

	public AbstractNetworkFromSelectionTask(final UndoSupport undoSupport,
	                                        final CyNetwork parentNetwork,
	                                        final CyRootNetworkManager rootNetMgr,
	                                        final CyNetworkViewFactory viewFactory,
	                                        final CyNetworkManager netMgr,
	                                        final CyNetworkViewManager netViewMgr,
	                                        final CyNetworkNaming networkNaming,
	                                        final VisualMappingManager vmMgr,
	                                        final CyApplicationManager appManager,
	                                        final CyEventHelper eventHelper,
	                                        final CyGroupManager groupMgr,
	                                        final RenderingEngineManager renderingEngineMgr,
	                                        final CyServiceRegistrar serviceRegistrar) {
		super(parentNetwork, netMgr, netViewMgr);

		this.undoSupport = undoSupport;
		this.rootNetMgr = rootNetMgr;
		this.viewFactory = viewFactory;
		this.networkNaming = networkNaming;
		this.vmMgr = vmMgr;
		this.appMgr = appManager;
		this.eventHelper = eventHelper;
		this.groupMgr = groupMgr;
		this.renderingEngineMgr = renderingEngineMgr;
		this.serviceRegistrar = serviceRegistrar;
	}

	abstract Set<CyNode> getNodes(CyNetwork net);
	
	abstract Set<CyEdge> getEdges(CyNetwork net);

	String getNetworkName() {
		return networkNaming.getSuggestedSubnetworkTitle(parentNetwork);
	}

	@Override
	public void run(TaskMonitor tm) {
		if (parentNetwork == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Source network must be specified.");
			return;
		}
		
		tm.setProgress(0.0);
		
		final Collection<CyNetworkView> views = networkViewManager.getNetworkViews(parentNetwork);		
		CyNetworkView sourceView = null;
		
		if (views.size() != 0)
			sourceView = views.iterator().next();
		
		tm.setProgress(0.1);

		// Get the selected nodes
		final Set<CyNode> nodes = getNodes(parentNetwork);
		tm.setProgress(0.2);

		if (nodes.size() <= 0)
			throw new IllegalArgumentException("No nodes are selected.");

		// create subnetwork and add selected nodes and appropriate edges
		newNet = rootNetMgr.getRootNetwork(parentNetwork).addSubNetwork();
		
		//We need to cpy the columns to local tables, since copying them to default table will duplicate the virtual columns.
		addColumns(parentNetwork.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS), newNet.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS));
		addColumns(parentNetwork.getTable(CyEdge.class, CyNetwork.LOCAL_ATTRS), newNet.getTable(CyEdge.class, CyNetwork.LOCAL_ATTRS) );
		addColumns(parentNetwork.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS), newNet.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS));

		tm.setProgress(0.3);
		
		for (final CyNode node : nodes){
			newNet.addNode(node);
			cloneRow(parentNetwork.getRow(node), newNet.getRow(node));
			//Set rows and edges to not selected state to avoid conflicts with table browser
			newNet.getRow(node).set(CyNetwork.SELECTED, false);
			
			if (groupMgr.isGroup(node, parentNetwork)) {
				CyGroup group = groupMgr.getGroup(node, parentNetwork);
				GroupUtils.addGroupToNetwork(group, parentNetwork, newNet);
			}
		}

		tm.setProgress(0.4);
		
		for (final CyEdge edge : getEdges(parentNetwork)){
			newNet.addEdge(edge);
			cloneRow(parentNetwork.getRow(edge), newNet.getRow(edge));
			//Set rows and edges to not selected state to avoid conflicts with table browser
			newNet.getRow(edge).set(CyNetwork.SELECTED, false);
		}
		
		tm.setProgress(0.5);
		
		newNet.getRow(newNet).set(CyNetwork.NAME, getNetworkName());
		DataUtils.saveParentNetworkSUID(newNet, parentNetwork.getSUID());

		networkManager.addNetwork(newNet, false);
		tm.setProgress(0.6);

		// create the view in a separate task
		final Set<CyNetwork> networks = new HashSet<CyNetwork>();
		networks.add(newNet);
		
		// Pick a CyNetworkViewFactory that is appropriate for the sourceView
		CyNetworkViewFactory sourceViewFactory = viewFactory;
		
		if (sourceView != null) {
			NetworkViewRenderer networkViewRenderer = appMgr.getNetworkViewRenderer(sourceView.getRendererId());
			if (networkViewRenderer != null) {
				sourceViewFactory = networkViewRenderer.getNetworkViewFactory();
			}
		}
		
		final CreateNetworkViewTask createViewTask = 
			new CreateNetworkViewTask(undoSupport, networks, sourceViewFactory, networkViewManager,
				                        null, eventHelper, vmMgr, renderingEngineMgr, appMgr, sourceView,
				                        serviceRegistrar);
		insertTasksAfterCurrentTask(createViewTask, new AbstractTask() {
			@Override
			@SuppressWarnings("unchecked")
			public void run(final TaskMonitor tm) throws Exception {
				// Select the new view
				tm.setProgress(0.0);
				final List<CyNetworkView> createdViews = (List<CyNetworkView>) createViewTask.getResults(List.class);
				
				if (!createdViews.isEmpty()) {
					CyNetworkView nv = createdViews.get(createdViews.size() - 1);
					if (nv != null) {
						appMgr.setCurrentNetworkView(nv);
						// Temporary workaround.  Since setting the current network view only
						// changes the current network if we're on the EDT -- BUG!!! --
						// change it explicitly here
						appMgr.setCurrentNetwork(nv.getModel());
					}
				}
				
				tm.setProgress(1.0);
			}
		});
		
		tm.setProgress(1.0);
	}

	private void addColumns(CyTable parentTable, CyTable subTable) {
		List<CyColumn> colsToAdd = new ArrayList<>();

		for (CyColumn col:  parentTable.getColumns())
			if (subTable.getColumn(col.getName()) == null)
				colsToAdd.add( col );

		for (CyColumn col:  colsToAdd) {
			VirtualColumnInfo colInfo = col.getVirtualColumnInfo();
			if (colInfo.isVirtual())
				addVirtualColumn(col, subTable);
			else
				copyColumn(col, subTable);
		}
	}

	private void addVirtualColumn (CyColumn col, CyTable subTable){
		VirtualColumnInfo colInfo = col.getVirtualColumnInfo();
		CyColumn checkCol= subTable.getColumn(col.getName());
		
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

	private void cloneRow(final CyRow from, final CyRow to) {
		for (final CyColumn column : from.getTable().getColumns()){
			if (!column.getVirtualColumnInfo().isVirtual())
				to.set(column.getName(), from.getRaw(column.getName()));
		}
	}
}
