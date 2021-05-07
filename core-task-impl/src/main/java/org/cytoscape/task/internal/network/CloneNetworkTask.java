package org.cytoscape.task.internal.network;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.cytoscape.command.StringToModel;
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
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.internal.view.CopyExistingViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
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

public class CloneNetworkTask extends AbstractCreationTask {
	
	private Map<CyNode, CyNode> orig2NewNodeMap;
	private Map<CyNode, CyNode> new2OrigNodeMap;
	private Map<CyEdge, CyEdge> new2OrigEdgeMap;
	private Map<CyEdge, CyEdge> orig2NewEdgeMap;

	private final VisualMappingManager visMapManager;
	private final CyNetworkFactory netFactory;
	private final CyNetworkViewFactory viewFactory;
	private final CyNetworkViewFactory nullViewFactory;
	private final CyNetworkNaming netNaming;
	private final CyNetworkManager netManager;
	private final CyNetworkViewManager viewManager;
	private final CyNetworkTableManager netTableManager;
	private final CyRootNetworkManager rootNetManager;
	private final CyGroupManager groupManager;
	private final CyGroupFactory groupFactory;
	
	@Tunable(
			description = "Network",
			context = "nogui",
			longDescription = StringToModel.CY_NETWORK_LONG_DESCRIPTION,
			exampleStringValue = StringToModel.CY_NETWORK_EXAMPLE_STRING
	)
	public CyNetwork network;
	
	@Tunable(
			description = "Clone View",
			context = "nogui",
			longDescription = "Whether or not to clone the network view as well. Only boolean values are allowed: ```true``` (default) or ```false```\"",
			exampleStringValue = "true"
	)
	public boolean cloneView = true;

	private CyNetworkView result;

	public CloneNetworkTask(CyNetwork net, CyServiceRegistrar serviceRegistrar) {
		super(net, serviceRegistrar);
		
		visMapManager = serviceRegistrar.getService(VisualMappingManager.class);
		netFactory = serviceRegistrar.getService(CyNetworkFactory.class);
		viewFactory = serviceRegistrar.getService(CyNetworkViewFactory.class);
		nullViewFactory = serviceRegistrar.getService(CyNetworkViewFactory.class, "(id=NullCyNetworkViewFactory)");
		netNaming = serviceRegistrar.getService(CyNetworkNaming.class);
		netManager = serviceRegistrar.getService(CyNetworkManager.class);
		viewManager = serviceRegistrar.getService(CyNetworkViewManager.class);
		netTableManager = serviceRegistrar.getService(CyNetworkTableManager.class);
		rootNetManager = serviceRegistrar.getService(CyRootNetworkManager.class);
		groupManager = serviceRegistrar.getService(CyGroupManager.class);
		groupFactory = serviceRegistrar.getService(CyGroupFactory.class);
	}

	@Override
	public void run(TaskMonitor tm) {
		tm.setTitle("Clone Network");
		tm.setStatusMessage("Cloning network...");
		tm.setProgress(0.0);
		
		// nogui?
		if (network != null)
			parentNetwork = network;

		if (parentNetwork == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "No network to clone");
			return;
		}
		
		// Create copied network model
		var newNet = cloneNetwork(parentNetwork);
		tm.setProgress(0.5);
		
		var views = viewManager.getNetworkViews(parentNetwork);
		
		// TODO What if the network has more than one view
		var origView = views.size() != 0 ? views.iterator().next() : null; 
		
		if (cloneView && origView != null && !cancelled) {
			tm.setStatusMessage("Cloning view...");
			
			var style = visMapManager.getVisualStyle(origView);
	        var newView = viewFactory.createNetworkView(newNet);
	        tm.setProgress(0.6);
	        
			if (!cancelled) {
				// Let the CopyExistingViewTask respond to the Observer (if any)
				var copyExistingViewTask = new CopyExistingViewTask(newView, origView, style,
						new2OrigNodeMap, new2OrigEdgeMap, false, serviceRegistrar);
				var registerNetworkTask = new RegisterNetworkTask(newView, style, serviceRegistrar);
				insertTasksAfterCurrentTask(copyExistingViewTask, registerNetworkTask);
			}
		} else if (!cancelled) {
			var registerNetworkTask = new RegisterNetworkTask(newNet, serviceRegistrar);
			insertTasksAfterCurrentTask(registerNetworkTask);
			result = nullViewFactory.createNetworkView(newNet);
		}

		if (cancelled) {
			dispose(newNet);
			return;
		}
		
		tm.setProgress(1.0);
	}

	private CyNetwork cloneNetwork(CyNetwork origNet) {
		var newNet = netFactory.createNetwork(origNet.getSavePolicy());
		
		if (cancelled)
			return newNet;
		
		// Copy default columns
		addColumns(origNet, newNet, CyNetwork.class, CyNetwork.LOCAL_ATTRS);
		addColumns(origNet, newNet, CyNode.class, CyNetwork.LOCAL_ATTRS);
		
		if (cancelled)
			return newNet;
		
		addColumns(origNet, newNet, CyEdge.class, CyNetwork.LOCAL_ATTRS);
		
		if (cancelled)
			return newNet;

		var origRoot = rootNetManager.getRootNetwork(origNet);
		var newRoot = rootNetManager.getRootNetwork(newNet);
		addColumns(origRoot, newRoot, CyNetwork.class, CyNetwork.LOCAL_ATTRS);
		
		if (cancelled)
			return newNet;
		
		addColumns(origRoot, newRoot, CyNode.class, CyNetwork.LOCAL_ATTRS);
		
		if (cancelled)
			return newNet;
		
		addColumns(origRoot, newRoot, CyEdge.class, CyNetwork.LOCAL_ATTRS);
		
		if (cancelled)
			return newNet;
		
		// Clone nodes, edges, groups
		cloneNodes(origNet, newNet);
		
		if (cancelled)
			return newNet;
		
		cloneEdges(origNet, newNet);
		
		if (cancelled)
			return newNet;

		cloneGroups(origNet, newNet);
		
		if (cancelled)
			return newNet;

		// Clone any network columns
		cloneNetwork(origNet, newNet);

		// Now, override the name so we don't have two networks with the same name
		newNet.getRow(newNet).set(CyNetwork.NAME, 
				netNaming.getSuggestedNetworkTitle(origNet.getRow(origNet).get(CyNetwork.NAME, String.class)));

		return newNet;
	}
	
	private void cloneNodes(CyNetwork origNet, CyNetwork newNet) {
		orig2NewNodeMap = new WeakHashMap<>();
		new2OrigNodeMap = new WeakHashMap<>();
		
		for (var origNode : origNet.getNodeList()) {
			if (cancelled)
				return;
			
			cloneNode(origNet, newNet, origNode);
		}
	}

	private CyNode cloneNode(CyNetwork origNet, CyNetwork newNet, CyNode origNode) {
		if (orig2NewNodeMap.containsKey(origNode))
			return orig2NewNodeMap.get(origNode);

		CyNode newNode = newNet.addNode();
		orig2NewNodeMap.put(origNode, newNode);
		new2OrigNodeMap.put(newNode, origNode);
		cloneRow(newNet, CyNode.class, origNet.getRow(origNode, CyNetwork.LOCAL_ATTRS), newNet.getRow(newNode, CyNetwork.LOCAL_ATTRS));
		cloneRow(newNet, CyNode.class, origNet.getRow(origNode, CyNetwork.HIDDEN_ATTRS), newNet.getRow(newNode, CyNetwork.HIDDEN_ATTRS));
		
		if (!groupManager.isGroup(origNode, origNet))
			cloneNetworkPointer(origNet, newNet, newNode, origNode.getNetworkPointer());
		
		return newNode;
	}

	private void cloneEdges(CyNetwork origNet, CyNetwork newNet) {
		new2OrigEdgeMap = new WeakHashMap<>();
		orig2NewEdgeMap = new WeakHashMap<>();
		
		for (CyEdge origEdge : origNet.getEdgeList()) {
			if (cancelled)
				return;
			
			cloneEdge(origNet, newNet, origEdge);
		}
	}

	private CyEdge cloneEdge(CyNetwork origNet, CyNetwork newNet, CyEdge origEdge) {
		if (orig2NewEdgeMap.containsKey(origEdge))
			return orig2NewEdgeMap.get(origEdge);

		CyNode newSource = orig2NewNodeMap.get(origEdge.getSource());
		CyNode newTarget = orig2NewNodeMap.get(origEdge.getTarget());
		boolean newDirected = origEdge.isDirected();
		CyEdge newEdge = newNet.addEdge(newSource, newTarget, newDirected);
		new2OrigEdgeMap.put(newEdge, origEdge);
		orig2NewEdgeMap.put(origEdge, newEdge);
		cloneRow(newNet, CyEdge.class, origNet.getRow(origEdge, CyNetwork.LOCAL_ATTRS), newNet.getRow(newEdge, CyNetwork.LOCAL_ATTRS));
		cloneRow(newNet, CyEdge.class, origNet.getRow(origEdge, CyNetwork.HIDDEN_ATTRS), newNet.getRow(newEdge, CyNetwork.HIDDEN_ATTRS));
		
		return newEdge;
	}

	private void cloneNetworkPointer(CyNetwork origNet, CyNetwork newNet, CyNode newNode,
			CyNetwork netPointer) {
		if (netPointer != null) {
			// If the referenced network is the original network itself, do the same with the new network,
			// rather than pointing to the original one.
			if (origNet.equals(netPointer))
				netPointer = newNet;

			newNode.setNetworkPointer(netPointer);
		}
	}

	private void cloneGroups(CyNetwork origNet, CyNetwork newNet) {
		// Get all of the groups in our original network
		Set<CyGroup> origGroups = groupManager.getGroupSet(origNet);

		// First, make sure we clone all of the child nodes for all of our collapsed groups.
		// Otherwise, we might not be able to create some or our edges
		for (CyGroup origGroup : origGroups) {
			if (cancelled)
				return;
			
			if (origGroup.isCollapsed(origNet)) {
				for (CyNode origNode : origGroup.getNodeList()) {
					if (cancelled)
						return;
					
					// add the node back into the network
					((CySubNetwork)origNet).addNode(origNode);
					cloneNode(origNet, newNet, origNode);
				}
			}
		}

		// Now, we can clone the group itself
		for (CyGroup origGroup : origGroups) {
			if (cancelled)
				return;
			
			cloneGroup(origNet, newNet, origGroup);
		}
	}

	private CyGroup cloneGroup(CyNetwork origNet, CyNetwork newNet, CyGroup origGroup) {
		List<CyNode> nodeList = new ArrayList<>();
		
		// Check to see if the group node is already in the network
		boolean groupNodeExists = origNet.containsNode(origGroup.getGroupNode());
		boolean collapsed = origGroup.isCollapsed(origNet);
		
		if (collapsed) {
			origGroup.expand(origNet);
		} else {
			// If we're not collapsed, we need to clone the group node and it's edges
			CyNode groupNode = origGroup.getGroupNode();

			// If the node already exists, we shouldn't need to do anything
			if (!groupNodeExists) {
				((CySubNetwork)origNet).addNode(groupNode);
				cloneNode(origNet, newNet, groupNode);
				// Now remove it
				((CySubNetwork)origNet).removeNodes(Collections.singletonList(groupNode));

				// TODO: What about non-meta edges?
			}
		}

		// Get the list of nodes for the group
		for (var node : origGroup.getNodeList())
			nodeList.add(orig2NewNodeMap.get(node));

		for (var iEdge : origGroup.getInternalEdgeList())
			cloneEdge(origNet, newNet, iEdge);

		for (var eEdge : origGroup.getExternalEdgeList())
			cloneEdge(origNet, newNet, eEdge);

		// Get the group node
		CyNode newNode = orig2NewNodeMap.get(origGroup.getGroupNode());

		// Copy our metaEdge information (if any), which is stored in the root network hidden table
		cloneMetaEdgeInfo(origNet, newNet, origGroup);

		// Create the group
		CyGroup newGroup = groupFactory.createGroup(newNet, newNode, nodeList, null, true);

		// We need to update all of our positions hints
		cloneGroupTables(origNet, newNet, origGroup, newGroup);

		if (!groupNodeExists) {
			// Because we're providing a group node with a network pointer, the groups code
			// is going to think we're coming from a session.  We need to remove the group node
			newNet.removeNodes(Collections.singletonList(newNode));
		}

		if (collapsed) {
			//  ...and collapse it...
			origGroup.collapse(origNet);
			newGroup.collapse(newNet);
		} 

		return newGroup;
	}

	private void cloneMetaEdgeInfo(CyNetwork origNet, CyNetwork newNet, CyGroup origGroup) {
		CyRootNetwork origRoot = ((CySubNetwork)origNet).getRootNetwork();
		
		for (var edge : origRoot.getAdjacentEdgeList(origGroup.getGroupNode(), CyEdge.Type.ANY)) {
			GroupUtils.updateMetaEdgeInformation(origNet, newNet, edge, orig2NewEdgeMap.get(edge));
		}
	}

	private void cloneGroupTables(CyNetwork origNet, CyNetwork newNet, CyGroup origGroup, CyGroup newGroup) {
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
		for (var node : origGroup.getNodeList()) {
			Long nodeSUID = node.getSUID();
			d = GroupUtils.getPosition(origNet, origGroup, nodeSUID, CyNode.class);
			// System.out.println("Position of node "+node+" is "+d);
			
			if (d != null) {
				GroupUtils.initializePositions(newNet, newGroup, orig2NewNodeMap.get(node).getSUID(), CyNode.class);
				GroupUtils.updatePosition(newNet, newGroup, orig2NewNodeMap.get(node).getSUID(), CyNode.class, d);
			}
		}
	}

	private void cloneNetwork(CyNetwork origNet, CyNetwork newNet) {
		cloneRow(newNet, CyNetwork.class, origNet.getRow(origNet, CyNetwork.LOCAL_ATTRS), newNet.getRow(newNet, CyNetwork.LOCAL_ATTRS));
	}

	private void addColumns(
			CyNetwork origNet,
			CyNetwork newNet,
			Class<? extends CyIdentifiable> tableType,
			String namespace
	) {
		final CyTable from = origNet.getTable(tableType, namespace); 
		final CyTable to = newNet.getTable(tableType, namespace); 
		final CyRootNetwork origRoot = rootNetManager.getRootNetwork(origNet);
		final CyRootNetwork newRoot = rootNetManager.getRootNetwork(newNet);
		final Map<String, CyTable> origRootTables = netTableManager.getTables(origRoot, tableType);
		
		for (var col : from.getColumns()){
			if (cancelled)
				return;
			
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
						String sourceNamespace = netTableManager.getTableNamespace(info.getSourceTable());
						final CyTable newRootTable = newRoot.getTable(tableType, sourceNamespace);
						
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

	private void addVirtualColumn(CyColumn col, CyTable subTable) {
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
	
	private void cloneRow(CyNetwork newNet, Class<? extends CyIdentifiable> tableType, CyRow from, CyRow to) {
		CyRootNetwork newRoot = rootNetManager.getRootNetwork(newNet);
		Map<String, CyTable> rootTables = netTableManager.getTables(newRoot, tableType);
		
		for (CyColumn col : to.getTable().getColumns()){
			String name = col.getName();
			
			if (name.equals(CyIdentifiable.SUID))
				continue;
			
			VirtualColumnInfo info = col.getVirtualColumnInfo();
			
			// If it's a virtual column whose source table is assigned to the new root-network,
			// then we have to set the value, because the rows of the new root table may not have been copied yet
			if (!info.isVirtual() || rootTables.containsValue(info.getSourceTable()))
				to.set(name, from.getRaw(name));
		}
	}
	
	private void dispose(CyNetwork net) {
		if (netManager.networkExists(net.getSUID()))
			netManager.destroyNetwork(net);
		else
			net.dispose();
	}
}
