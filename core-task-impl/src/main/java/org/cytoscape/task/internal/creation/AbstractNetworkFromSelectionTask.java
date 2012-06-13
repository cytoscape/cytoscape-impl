/*
 File: AbstractNetworkFromSelectionTask.java

 Copyright (c) 2006, 2010-2011, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.task.internal.creation;


import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.VirtualColumnInfo;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;


abstract class AbstractNetworkFromSelectionTask extends AbstractCreationTask {
	private final UndoSupport undoSupport;
	protected final CyRootNetworkManager rootNetworkManager;
	protected final CyNetworkViewFactory viewFactory;
	protected final VisualMappingManager vmm;
	protected final CyNetworkNaming cyNetworkNaming;
	protected final CyApplicationManager appManager;
	private final CyEventHelper eventHelper;

	public AbstractNetworkFromSelectionTask(final UndoSupport undoSupport,
	                                        final CyNetwork parentNetwork,
	                                        final CyRootNetworkManager rootNetworkManager,
	                                        final CyNetworkViewFactory viewFactory,
	                                        final CyNetworkManager netmgr,
	                                        final CyNetworkViewManager networkViewManager,
	                                        final CyNetworkNaming cyNetworkNaming,
	                                        final VisualMappingManager vmm,
	                                        final CyApplicationManager appManager,
	                                        final CyEventHelper eventHelper)
	{
		super(parentNetwork, netmgr, networkViewManager);

		this.undoSupport        = undoSupport;
		this.rootNetworkManager = rootNetworkManager;
		this.viewFactory        = viewFactory;
		this.cyNetworkNaming    = cyNetworkNaming;
		this.vmm                = vmm;
		this.appManager         = appManager;
		this.eventHelper        = eventHelper;
	}

	abstract Collection<CyEdge> getEdges(CyNetwork netx, List<CyNode> nodes);

	@Override
	public void run(TaskMonitor tm) {
		if (parentNetwork == null)
			throw new NullPointerException("Source network is null.");
		tm.setProgress(0.0);

		final Collection<CyNetworkView> views = networkViewManager.getNetworkViews(parentNetwork);		
		CyNetworkView sourceView = null;
		if(views.size() != 0)
			sourceView = views.iterator().next();
		
		tm.setProgress(0.1);

		// Get the selected nodes, but only create network if nodes are actually
		// selected.
		final List<CyNode> selectedNodes = CyTableUtil.getNodesInState(parentNetwork, CyNetwork.SELECTED, true);
		tm.setProgress(0.2);

		if (selectedNodes.size() <= 0)
			throw new IllegalArgumentException("No nodes are selected!");

		// create subnetwork and add selected nodes and appropriate edges
		final CySubNetwork newNet = rootNetworkManager.getRootNetwork(parentNetwork).addSubNetwork();

		addColumns(parentNetwork.getDefaultNodeTable(), newNet.getDefaultNodeTable() );
		addColumns(parentNetwork.getDefaultEdgeTable(), newNet.getDefaultEdgeTable() );
		addColumns(parentNetwork.getDefaultNetworkTable(), newNet.getDefaultNetworkTable());

		tm.setProgress(0.3);
		
		for (final CyNode node : selectedNodes){
			newNet.addNode(node);
			cloneRow(parentNetwork.getRow(node), newNet.getRow(node));
		}

		tm.setProgress(0.4);
		for (final CyEdge edge : getEdges(parentNetwork, selectedNodes)){
			newNet.addEdge(edge);
			cloneRow(parentNetwork.getRow(edge), newNet.getRow(edge));
		}
		tm.setProgress(0.5);

		
		newNet.getRow(newNet).set(CyNetwork.NAME, cyNetworkNaming.getSuggestedSubnetworkTitle(parentNetwork));

		networkManager.addNetwork(newNet);
		tm.setProgress(0.6);

		// create the view in a separate task
		final Set<CyNetwork> networks = new HashSet<CyNetwork>();
		networks.add(newNet);
		final Task createViewTask = new CreateNetworkViewTask(undoSupport, networks, viewFactory, networkViewManager,
				null, eventHelper, vmm, sourceView);
		insertTasksAfterCurrentTask(createViewTask);

		tm.setProgress(1.0);
	}

	private void addColumns(CyTable parentTable, CyTable subTable) {

		for (CyColumn col:  parentTable.getColumns()){
			VirtualColumnInfo colInfo = col.getVirtualColumnInfo();
			if (colInfo.isVirtual())
				addVirtualColumn(col, subTable);
			else
				if (subTable.getColumn(col.getName()) == null)		
					copyColumn(col, subTable);
		}
	}

	private void addVirtualColumn (CyColumn col, CyTable subTable){
		VirtualColumnInfo colInfo = col.getVirtualColumnInfo();
		CyColumn checkCol= subTable.getColumn(col.getName());
		if(checkCol == null)
			subTable.addVirtualColumn(col.getName(), colInfo.getSourceColumn(), colInfo.getSourceTable(), colInfo.getTargetJoinKey(), true);

		else
			if(!checkCol.getVirtualColumnInfo().isVirtual() ||
					!checkCol.getVirtualColumnInfo().getSourceTable().equals(colInfo.getSourceTable()) ||
					!checkCol.getVirtualColumnInfo().getSourceColumn().equals(colInfo.getSourceColumn()))
				subTable.addVirtualColumn(col.getName(), colInfo.getSourceColumn(), colInfo.getSourceTable(), colInfo.getTargetJoinKey(), true);
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
