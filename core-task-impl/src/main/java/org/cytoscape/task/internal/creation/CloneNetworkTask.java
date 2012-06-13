/*
  File: CloneNetworkTask.java

  Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.VirtualColumnInfo;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloneNetworkTask extends AbstractCreationTask {
	
	private static final Logger logger = LoggerFactory.getLogger(CloneNetworkTask.class);

	private Map<CyNode, CyNode> orig2NewNodeMap;
	private Map<CyNode, CyNode> new2OrigNodeMap;

	private final VisualMappingManager vmm;
	private final CyNetworkFactory netFactory;
	private final CyNetworkViewFactory netViewFactory;
	private final CyNetworkNaming naming;
	private final CyApplicationManager appMgr;

	public CloneNetworkTask(final CyNetwork net, final CyNetworkManager netmgr,
			final CyNetworkViewManager networkViewManager, final VisualMappingManager vmm,
			final CyNetworkFactory netFactory, final CyNetworkViewFactory netViewFactory, final CyNetworkNaming naming,
			final CyApplicationManager appMgr) {
		super(net, netmgr, networkViewManager);

		this.vmm = vmm;
		this.netFactory = netFactory;
		this.netViewFactory = netViewFactory;
		this.naming = naming;
		this.appMgr = appMgr;
	}

	public void run(TaskMonitor tm) {
		tm.setProgress(0.0);
		final long start = System.currentTimeMillis();
		logger.debug("Clone Network Task start");
		
		// Create copied network model
		final CyNetwork newNet = cloneNetwork(parentNetwork);
		tm.setProgress(0.4);
		final Collection<CyNetworkView> views = networkViewManager.getNetworkViews(parentNetwork);
		CyNetworkView origView = null;
		if (views.size() != 0)
			origView = views.iterator().next(); 
		networkManager.addNetwork(newNet);
		tm.setProgress(0.6);

		if (origView != null) {
	        final CyNetworkView newView = netViewFactory.createNetworkView(newNet);
			networkViewManager.addNetworkView(newView);
			insertTasksAfterCurrentTask(new CopyExistingViewTask(vmm, newView, origView, new2OrigNodeMap));
		}

		tm.setProgress(1.0);
	}

	private CyNetwork cloneNetwork(CyNetwork origNet) {
		final CyNetwork newNet = netFactory.createNetwork();

		final CyTable nodeTable = newNet.getDefaultNodeTable();
		final CyTable edgeTable = newNet.getDefaultEdgeTable();
		final CyTable networkTable = newNet.getDefaultNetworkTable();
		// copy default columns
		addColumns(origNet.getDefaultNodeTable(), nodeTable);
		addColumns(origNet.getDefaultEdgeTable(), edgeTable);
		addColumns(origNet.getDefaultNetworkTable(), networkTable);

			cloneNodes(origNet, newNet);
			cloneEdges(origNet, newNet);

			newNet.getRow(newNet).set(CyNetwork.NAME,
					naming.getSuggestedNetworkTitle(origNet.getRow(origNet).get(CyNetwork.NAME, String.class)));
		return newNet;
	}
	
	private void cloneNodes(CyNetwork origNet, CyNetwork newNet) {
		orig2NewNodeMap = new WeakHashMap<CyNode, CyNode>();
		new2OrigNodeMap = new WeakHashMap<CyNode, CyNode>();
		for (final CyNode origNode : origNet.getNodeList()) {
			final CyNode newNode = newNet.addNode();
			orig2NewNodeMap.put(origNode, newNode);
			new2OrigNodeMap.put(newNode, origNode);
			cloneRow(origNet.getRow(origNode), newNet.getRow(newNode));
		}
	}

	private void cloneEdges(CyNetwork origNet, CyNetwork newNet) {
		for (final CyEdge origEdge : origNet.getEdgeList()) {
			final CyNode newSource = orig2NewNodeMap.get(origEdge.getSource());
			final CyNode newTarget = orig2NewNodeMap.get(origEdge.getTarget());
			final boolean newDirected = origEdge.isDirected();
			final CyEdge newEdge = newNet.addEdge(newSource, newTarget, newDirected);
			cloneRow(origNet.getRow(origEdge), newNet.getRow(newEdge));
		}
	}

	private void addColumns(CyTable parentTable, CyTable subTable) {
		for (CyColumn col:  parentTable.getColumns()){
			if (subTable.getColumn(col.getName()) == null){
				VirtualColumnInfo colInfo = col.getVirtualColumnInfo();
				if (colInfo.isVirtual())
					addVirtualColumn(col, subTable);
				else
					copyColumn(col, subTable);
			}
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
