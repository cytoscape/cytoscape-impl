package org.cytoscape.task.internal.creation;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.VirtualColumnInfo;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class CloneNetworkTask extends AbstractCreationTask implements ObservableTask {
	
	private Map<CyNode, CyNode> orig2NewNodeMap;
	private Map<CyNode, CyNode> new2OrigNodeMap;
	private Map<CyEdge, CyEdge> new2OrigEdgeMap;
	private Map<CyEdge, CyEdge> orig2NewEdgeMap;

	private final VisualMappingManager vmm;
	private final CyNetworkFactory netFactory;
	private final CyNetworkViewFactory netViewFactory;
	private final CyNetworkNaming naming;
	private final CyApplicationManager appMgr;
	private final CyNetworkTableManager netTableMgr;
	private final CyRootNetworkManager rootNetMgr;
	private final CyGroupManager groupMgr;
	private final CyGroupFactory groupFactory;
	private final RenderingEngineManager renderingEngineMgr;
	private final CyNetworkViewFactory nullNetworkViewFactory;
	
	private CyNetworkView result;

	public CloneNetworkTask(final CyNetwork net,
							final CyNetworkManager netmgr,
							final CyNetworkViewManager networkViewManager,
							final VisualMappingManager vmm,
							final CyNetworkFactory netFactory,
							final CyNetworkViewFactory netViewFactory,
							final CyNetworkNaming naming,
							final CyApplicationManager appMgr,
							final CyNetworkTableManager netTableMgr,
							final CyRootNetworkManager rootNetMgr,
							final CyGroupManager groupMgr,
							final CyGroupFactory groupFactory,
							final RenderingEngineManager renderingEngineMgr,
							final CyNetworkViewFactory nullNetworkViewFactory) {
		super(net, netmgr, networkViewManager);

		this.vmm = vmm;
		this.netFactory = netFactory;
		this.netViewFactory = netViewFactory;
		this.naming = naming;
		this.appMgr = appMgr;
		this.netTableMgr = netTableMgr;
		this.rootNetMgr = rootNetMgr;
		this.groupMgr = groupMgr;
		this.groupFactory = groupFactory;
		this.renderingEngineMgr = renderingEngineMgr;
		this.nullNetworkViewFactory = nullNetworkViewFactory;
	}

	public void run(TaskMonitor tm) {
		tm.setProgress(0.0);
		
		// Create copied network model
		final CyNetwork newNet = cloneNetwork(parentNetwork);
		tm.setProgress(0.5);
		
		final Collection<CyNetworkView> views = networkViewManager.getNetworkViews(parentNetwork);
		
		// TODO What if the network has more than one view
		final CyNetworkView origView = views.size() != 0 ? views.iterator().next() : null; 
		
		if (origView != null) {
			final VisualStyle style = vmm.getVisualStyle(origView);
	        final CyNetworkView newView = netViewFactory.createNetworkView(newNet);
	        tm.setProgress(0.6);
	        
	        // Let the CopyExistingViewTask respond to the Observer (if any)
			final CopyExistingViewTask copyExistingViewTask = new CopyExistingViewTask(renderingEngineMgr, newView,
					origView, style, new2OrigNodeMap, new2OrigEdgeMap, false);
			final RegisterNetworkTask registerNetworkTask = new RegisterNetworkTask(newView, style);
			insertTasksAfterCurrentTask(copyExistingViewTask, registerNetworkTask);
		} else {
			final RegisterNetworkTask registerNetworkTask = new RegisterNetworkTask(newNet);
			insertTasksAfterCurrentTask(registerNetworkTask);
			
			result = nullNetworkViewFactory.createNetworkView(newNet);
		}
		
		tm.setProgress(1.0);
	}

	@Override
	public Object getResults(Class type) {
		if (result == null) return null;
		if (type.equals(String.class))
			return result.toString();
		if (type.equals(CyNetwork.class))
			return result.getModel();
		return result;
	}

	private CyNetwork cloneNetwork(final CyNetwork origNet) {
		final CyNetwork newNet = netFactory.createNetwork(origNet.getSavePolicy());
		
		// copy default columns
		addColumns(origNet, newNet, CyNetwork.class, CyNetwork.LOCAL_ATTRS);
		addColumns(origNet, newNet, CyNode.class, CyNetwork.LOCAL_ATTRS);
		addColumns(origNet, newNet, CyEdge.class, CyNetwork.LOCAL_ATTRS);

		cloneNodes(origNet, newNet);
		cloneEdges(origNet, newNet);

		cloneGroups(origNet, newNet);

		newNet.getRow(newNet).set(CyNetwork.NAME, 
				naming.getSuggestedNetworkTitle(origNet.getRow(origNet).get(CyNetwork.NAME, String.class)));
		
		return newNet;
	}
	
	private void cloneNodes(final CyNetwork origNet, final CyNetwork newNet) {
		orig2NewNodeMap = new WeakHashMap<CyNode, CyNode>();
		new2OrigNodeMap = new WeakHashMap<CyNode, CyNode>();
		
		for (final CyNode origNode : origNet.getNodeList()) {
			cloneNode(origNet, newNet, origNode);
		}
	}

	private CyNode cloneNode(final CyNetwork origNet, final CyNetwork newNet, final CyNode origNode) {
		if (orig2NewNodeMap.containsKey(origNode))
			return orig2NewNodeMap.get(origNode);

		final CyNode newNode = newNet.addNode();
		orig2NewNodeMap.put(origNode, newNode);
		new2OrigNodeMap.put(newNode, origNode);
		cloneRow(newNet, CyNode.class, origNet.getRow(origNode, CyNetwork.LOCAL_ATTRS), newNet.getRow(newNode, CyNetwork.LOCAL_ATTRS));
		cloneRow(newNet, CyNode.class, origNet.getRow(origNode, CyNetwork.HIDDEN_ATTRS), newNet.getRow(newNode, CyNetwork.HIDDEN_ATTRS));
		
		if (!groupMgr.isGroup(origNode, origNet))
			cloneNetworkPointer(origNet, newNet, newNode, origNode.getNetworkPointer());
		return newNode;
	}

	private void cloneEdges(final CyNetwork origNet, final CyNetwork newNet) {
		new2OrigEdgeMap = new WeakHashMap<CyEdge, CyEdge>();
		orig2NewEdgeMap = new WeakHashMap<CyEdge, CyEdge>();
		
		for (final CyEdge origEdge : origNet.getEdgeList()) {
			cloneEdge(origNet, newNet, origEdge);
		}
	}

	private CyEdge cloneEdge(final CyNetwork origNet, final CyNetwork newNet, final CyEdge origEdge) {
		if (orig2NewEdgeMap.containsKey(origEdge))
			return orig2NewEdgeMap.get(origEdge);

		final CyNode newSource = orig2NewNodeMap.get(origEdge.getSource());
		final CyNode newTarget = orig2NewNodeMap.get(origEdge.getTarget());
		final boolean newDirected = origEdge.isDirected();
		final CyEdge newEdge = newNet.addEdge(newSource, newTarget, newDirected);
		new2OrigEdgeMap.put(newEdge, origEdge);
		orig2NewEdgeMap.put(origEdge, newEdge);
		cloneRow(newNet, CyEdge.class, origNet.getRow(origEdge, CyNetwork.LOCAL_ATTRS), newNet.getRow(newEdge, CyNetwork.LOCAL_ATTRS));
		cloneRow(newNet, CyEdge.class, origNet.getRow(origEdge, CyNetwork.HIDDEN_ATTRS), newNet.getRow(newEdge, CyNetwork.HIDDEN_ATTRS));
		return newEdge;
	}

	private void cloneNetworkPointer(final CyNetwork origNet, final CyNetwork newNet, final CyNode newNode,
			CyNetwork netPointer) {
		if (netPointer != null) {
			// If the referenced network is the original network itself, do the same with the new network,
			// rather than pointing to the original one.
			if (origNet.equals(netPointer))
				netPointer = newNet;

			newNode.setNetworkPointer(netPointer);
		}
	}

	private void cloneGroups(final CyNetwork origNet, final CyNetwork newNet) {
		// Get all of the groups in our original network
		Set<CyGroup> origGroups = groupMgr.getGroupSet(origNet);

		// First, make sure we clone all of the child nodes for all of our
		// collapsed groups.  Otherwise, we might not be able to create some or our
		// edges
		for (CyGroup origGroup: origGroups) {
			if (origGroup.isCollapsed(origNet)) {
				for (CyNode origNode: origGroup.getNodeList()) {
					// add the node back into the network
					((CySubNetwork)origNet).addNode(origNode);
					cloneNode(origNet, newNet, origNode);
				}
			}
		}

		// Now, we can clone the group itself
		for (CyGroup origGroup: origGroups) {
			cloneGroup(origNet, newNet, origGroup);
		}
	}

	private CyGroup cloneGroup(final CyNetwork origNet, final CyNetwork newNet, final CyGroup origGroup) {
		List<CyNode> nodeList = new ArrayList<CyNode>();
		List<CyEdge> edgeList = new ArrayList<CyEdge>();

		boolean collapsed = origGroup.isCollapsed(origNet);
		if (collapsed)
			origGroup.expand(origNet);
		else {
			// If we're not collapsed, we need to clone the group node and it's edges
			CyNode groupNode = origGroup.getGroupNode();
			((CySubNetwork)origNet).addNode(groupNode);
			cloneNode(origNet, newNet, groupNode);
			// Now remove it
			((CySubNetwork)origNet).removeNodes(Collections.singletonList(groupNode));

			// TODO: What about non-meta edges?
		}

		// Get the list of nodes for the group
		for (CyNode node: origGroup.getNodeList()) {
			nodeList.add(orig2NewNodeMap.get(node));
		}

		for (CyEdge iEdge: origGroup.getInternalEdgeList()) {
			cloneEdge(origNet, newNet, iEdge);
		}

		for (CyEdge eEdge: origGroup.getExternalEdgeList()) {
			cloneEdge(origNet, newNet, eEdge);
		}

		// Get the group node
		CyNode newNode = orig2NewNodeMap.get(origGroup.getGroupNode());

		// Copy our metaEdge information (if any), which is stored in the root network hidden table
		cloneMetaEdgeInfo(origNet, newNet, origGroup);

		// Create the group
		CyGroup newGroup = groupFactory.createGroup(newNet, newNode, nodeList, null, true);

		// We need to update all of our positions hints
		cloneGroupTables(origNet, newNet, origGroup, newGroup);

		// Because we're providing a group node with a network pointer, the groups code
		// is going to think we're coming from a session.  We need to remove the group node
		newNet.removeNodes(Collections.singletonList(newNode));

		if (collapsed) {
			//  ...and collapse it...
			origGroup.collapse(origNet);
			newGroup.collapse(newNet);
		} 

		return newGroup;
	}

	private void cloneMetaEdgeInfo(CyNetwork origNet, CyNetwork newNet, CyGroup origGroup) {
		CyRootNetwork origRoot = ((CySubNetwork)origNet).getRootNetwork();
		for (CyEdge edge: origRoot.getAdjacentEdgeList(origGroup.getGroupNode(), CyEdge.Type.ANY)) {
			GroupUtils.updateMetaEdgeInformation(origNet, newNet, edge, orig2NewEdgeMap.get(edge));
		}
	}

	private void cloneGroupTables(CyNetwork origNet, CyNetwork newNet, 
	                              CyGroup origGroup, CyGroup newGroup) {
		CyNetwork origGroupNet = origGroup.getGroupNetwork();
		CyNetwork newGroupNet = newGroup.getGroupNetwork();

		addColumns(origGroupNet, newGroupNet, CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
		addColumns(origGroupNet, newGroupNet, CyNode.class, CyNetwork.HIDDEN_ATTRS);

		Long groupNetworkSUID = origGroupNet.getSUID();
		Dimension d = GroupUtils.getPosition(origNet, origGroup, 
		                                     groupNetworkSUID, CyNetwork.class);
		if (d != null) {
			GroupUtils.initializePositions(newNet, newGroup, groupNetworkSUID, CyNetwork.class);
			GroupUtils.updatePosition(newNet, newGroup, groupNetworkSUID, CyNetwork.class, d);
		}

		// Clone the node table
		for (CyNode node: origGroup.getNodeList()) {
			Long nodeSUID = node.getSUID();
			d = GroupUtils.getPosition(origNet, origGroup, nodeSUID, CyNode.class);
			// System.out.println("Position of node "+node+" is "+d);
			if (d != null) {
				GroupUtils.initializePositions(newNet, newGroup, orig2NewNodeMap.get(node).getSUID(), CyNode.class);
				GroupUtils.updatePosition(newNet, newGroup, orig2NewNodeMap.get(node).getSUID(), CyNode.class, d);
			}
		}
	}

	private void addColumns(final CyNetwork origNet,
							final CyNetwork newNet, 
							final Class<? extends CyIdentifiable> tableType,
							final String namespace) {
		final CyTable from = origNet.getTable(tableType, namespace); 
		final CyTable to = newNet.getTable(tableType, namespace); 
		final CyRootNetwork origRoot = rootNetMgr.getRootNetwork(origNet);
		final CyRootNetwork newRoot = rootNetMgr.getRootNetwork(newNet);
		final Map<String, CyTable> origRootTables = netTableMgr.getTables(origRoot, tableType);
		
		for (final CyColumn col : from.getColumns()){
			final String name = col.getName();
			
			if (to.getColumn(name) == null){
				final VirtualColumnInfo info = col.getVirtualColumnInfo();
				
				if (info.isVirtual()) {
					if (origRootTables.containsValue(info.getSourceTable())) {
						// If the virtual column is from a root-network table, do NOT set this virtual column directly to
						// the new table:
						// Get the original column (not the virtual one!)
						final CyColumn origCol = info.getSourceTable().getColumn(info.getSourceColumn());
						// Copy the original column to the root-network's table first
						final CyTable newRootTable = newRoot.getTable(tableType, namespace);
						
						if (newRootTable.getColumn(origCol.getName()) == null)
							copyColumn(origCol, newRootTable);
					
						// Now we can add the new "root" column as a virtual one to the new network's table
						to.addVirtualColumn(name, origCol.getName(), newRootTable, CyIdentifiable.SUID, col.isImmutable());
					} else {
						// Otherwise (e.g. virtual column from a global table) just add the virtual column directly
						addVirtualColumn(col, to);
					}
				} else {
					// Not a virtual column, so just copy it to the new network's table
					copyColumn(col, to);
				}
			}
		}
	}

	private void addVirtualColumn(CyColumn col, CyTable subTable){
		VirtualColumnInfo colInfo = col.getVirtualColumnInfo();
		CyColumn checkCol= subTable.getColumn(col.getName());
		
		if (checkCol == null)
			subTable.addVirtualColumn(col.getName(), colInfo.getSourceColumn(), colInfo.getSourceTable(), colInfo.getTargetJoinKey(), true);
		else if (!checkCol.getVirtualColumnInfo().isVirtual() ||
					!checkCol.getVirtualColumnInfo().getSourceTable().equals(colInfo.getSourceTable()) ||
					!checkCol.getVirtualColumnInfo().getSourceColumn().equals(colInfo.getSourceColumn()))
			subTable.addVirtualColumn(col.getName(), colInfo.getSourceColumn(), colInfo.getSourceTable(), colInfo.getTargetJoinKey(), true);
	}

	private void copyColumn(CyColumn col, CyTable subTable) {
		CyColumn checkCol= subTable.getColumn(col.getName());
		if (checkCol == null) {
			if (List.class.isAssignableFrom(col.getType()))
				subTable.createListColumn(col.getName(), col.getListElementType(), false);
			else
				subTable.createColumn(col.getName(), col.getType(), false);	
		}
	}
	
	private void cloneRow(final CyNetwork newNet, final Class<? extends CyIdentifiable> tableType, final CyRow from,
			final CyRow to) {
		final CyRootNetwork newRoot = rootNetMgr.getRootNetwork(newNet);
		Map<String, CyTable> rootTables = netTableMgr.getTables(newRoot, tableType);
		
		for (final CyColumn col : to.getTable().getColumns()){
			final String name = col.getName();
			
			if (name.equals(CyIdentifiable.SUID))
				continue;
			
			final VirtualColumnInfo info = col.getVirtualColumnInfo();
			
			// If it's a virtual column whose source table is assigned to the new root-network,
			// then we have to set the value, because the rows of the new root table may not have been copied yet
			if (!info.isVirtual() || rootTables.containsValue(info.getSourceTable()))
				to.set(name, from.getRaw(name));
		}
	}
	
	/**
	 * Registers a new Network and/or Network View and set them as current.
	 */
	private class RegisterNetworkTask extends AbstractTask {

		private final CyNetwork network;
		private final CyNetworkView view;
		private final VisualStyle style;
		
		public RegisterNetworkTask(final CyNetwork network) {
			this.network = network;
			this.view = null;
			this.style = null;
		}
		
		public RegisterNetworkTask(final CyNetworkView view, final VisualStyle style) {
			this.network = view.getModel();
			this.view = view;
			this.style = style;
		}
		
		@Override
		public void run(TaskMonitor tm) throws Exception {
			tm.setProgress(0.0);
			
			if (!networkManager.networkExists(network.getSUID()))
				networkManager.addNetwork(network);
			
			tm.setProgress(0.1);
			
			if (view != null) {
				networkViewManager.addNetworkView(view);
				tm.setProgress(0.2);
				
				if (style != null) {
					vmm.setVisualStyle(style, view);
					tm.setProgress(0.8);
				}
			}
			
			if (view != null) {
				appMgr.setCurrentNetworkView(view);
				appMgr.setSelectedNetworkViews(Collections.singletonList(view));
				tm.setProgress(0.9);
				
				view.updateView();
			} else {
				appMgr.setCurrentNetwork(network);
			}
			
			tm.setProgress(1.0);
		}
	}
}
