package org.cytoscape.model.internal;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.events.AboutToRemoveEdgesEvent;
import org.cytoscape.model.events.AboutToRemoveNodesEvent;
import org.cytoscape.model.events.AddedEdgesEvent;
import org.cytoscape.model.events.AddedNodesEvent;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.RemovedEdgesEvent;
import org.cytoscape.model.events.RemovedNodesEvent;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;


/**
 * A full implementation of CySubNetwork that adds the addNode/addEdge
 * support that is missing from SimpleNetwork.  These methods interact
 * with the CyRootNetworkImpl parent to use the proper CyNode/CyEdge
 * and then call the appropriate internal methods.  A few other methods
 * wrap those in SimpleNetwork to fire appropriate events.
 */
public final class CySubNetworkImpl extends DefaultTablesNetwork implements CySubNetwork, NetworkAddedListener {

	private SavePolicy savePolicy;
	
	private final CyRootNetworkImpl parent;
	private boolean fireAddedNodesAndEdgesEvents;
	private final CyTableManager tableMgr;
	
	private final CyNetworkTableManager networkTableMgr;

	CySubNetworkImpl(final CyRootNetworkImpl par, 
	                 final long suid,
	                 final CyEventHelper eventHelper, 
	                 final CyTableManager tableMgr,
	                 final CyNetworkTableManager netTableMgr, 
	                 final CyTableFactory tableFactory, 
	                 boolean publicTables,
	                 int tableSizeDeterminer,
	                 final SavePolicy savePolicy) {
		super(suid, netTableMgr, tableFactory,publicTables,tableSizeDeterminer,eventHelper);

		assert(par != null);
		assert(savePolicy != null);
		
		this.parent = par;
		this.tableMgr = tableMgr;
		this.networkTableMgr = netTableMgr;
		this.savePolicy = savePolicy;
		
		initTables(this, 
		           (SharedTableFacade)(networkTableMgr.getTable(parent, CyNetwork.class, CyRootNetwork.SHARED_DEFAULT_ATTRS)),
		           (SharedTableFacade)(networkTableMgr.getTable(parent, CyNode.class, CyRootNetwork.SHARED_DEFAULT_ATTRS)),
				   (SharedTableFacade)(networkTableMgr.getTable(parent, CyEdge.class, CyRootNetwork.SHARED_DEFAULT_ATTRS)) );

		fireAddedNodesAndEdgesEvents = false;		
	}

	@Override
	public CyRootNetwork getRootNetwork() {
		return parent;
	}
	
	@Override
	public CyNode addNode() {
		final CyNode ret;
		synchronized (this) {
			// first add a node to the root network
			ret = parent.addNode();
			// then add the resulting CyNode to this network
			addNodeInternal(ret);
			getRow(ret).set(SELECTED, false);
		}

		if (fireAddedNodesAndEdgesEvents)
			eventHelper.addEventPayload((CyNetwork)this, ret, AddedNodesEvent.class);

		return ret;
	}

	@Override
	public boolean addNode(final CyNode node) {
		if (node == null)
			throw new NullPointerException("node is null");

		synchronized (this) {
			if (containsNode(node))
				return false;

			if (!parent.containsNode(node) && !parent.cachedNode(node))
				throw new IllegalArgumentException("node is not contained in parent network.");

			addNodeInternal(node);
			
			if(parent.cachedNode(node)) {
				parent.restoreNode(node);
			}
			copyTableData(node);
		}

		if (fireAddedNodesAndEdgesEvents)
			eventHelper.addEventPayload((CyNetwork)this, node, AddedNodesEvent.class);

		return true;
	}

	@Override
	public CyEdge addEdge(final CyNode source, final CyNode target, final boolean isDirected) {
		// important that it's edgeAdd and not addEdge
		final CyEdge ret;
			
		synchronized (this) {
			// first add an edge to the root network
			ret = parent.addEdge(source, target, isDirected);
			// then add the resulting CyEdge to this network
			addEdgeInternal(source,target,isDirected,ret);
			getRow(ret).set(CyNetwork.SELECTED, false);
		}

		if (fireAddedNodesAndEdgesEvents)
			eventHelper.addEventPayload((CyNetwork)this, ret, AddedEdgesEvent.class);

		return ret;
	}

	@Override
	public boolean addEdge(final CyEdge edge) {
		if (edge == null)
			throw new NullPointerException("edge is null");

		synchronized (this) {
			if (containsEdge(edge))
				return false;

			if (!parent.containsEdge(edge) && !parent.cachedEdge(edge))
				throw new IllegalArgumentException("edge is not contained in parent network.");

			// This will:
			// -- add the node if it doesn't already exist
			// -- do nothing if the node does exist
			// -- throw an exception if the node isn't part of the root network
			addNode(edge.getSource());
			addNode(edge.getTarget());

			// add edge
			addEdgeInternal(edge.getSource(),edge.getTarget(),edge.isDirected(),edge);

			if(parent.cachedEdge(edge)) {
				parent.restoreEdge(edge);
			}
			copyTableData(edge);
		}

		if (fireAddedNodesAndEdgesEvents)
			eventHelper.addEventPayload((CyNetwork)this, edge, AddedEdgesEvent.class);

		return true;
	}

	/**
	 * This method is called when an edge or a node is added to the networks
	 * which is indeed a copy of another edge/node. Hence, it copies all of the
	 * default attributes from the shared table referenced to that edge/node to
	 * the new edge/node.
	 * @param graphObject
	 */
	private void copyTableData(final CyIdentifiable graphObject) {
		final String name = parent.getRow(graphObject).get(NAME, String.class);
		final CyRow sharedTableRow = parent.getRow(graphObject, CyRootNetwork.SHARED_ATTRS);
		final CyRow defaultTableRow = parent.getRow(graphObject);
		
//		final String name = parent.getCachedAttributes(graphObject).get(NAME, String.class);
//		final CyRow sharedTableRow = parent.getCachedAttributes(graphObject, CyRootNetwork.SHARED_ATTRS);
//		final CyRow defaultTableRow = parent.getCachedAttributes(graphObject);
		
		final CyRow targetRow = this.getRow(graphObject);
		// Step 1: Copy shared name as name of this new node
		final String sharedName = sharedTableRow.get(CyRootNetwork.SHARED_NAME, String.class);
		
		if(sharedName != null)
			targetRow.set(CyNetwork.NAME, sharedName);
		else
			targetRow.set(CyNetwork.NAME, name);
		
		// Step 2: Copy selection state
		targetRow.set(CyNetwork.SELECTED, defaultTableRow.get(CyNetwork.SELECTED, Boolean.class));
		
		// Step 3: Copy Interaction if edge
		if(graphObject instanceof CyEdge) {
			final String interaction = sharedTableRow.get(CyRootNetwork.SHARED_INTERACTION, String.class);
			targetRow.set(CyEdge.INTERACTION, interaction);
		}
	}


	@Override
	public boolean removeNodes(final Collection<CyNode> nodes) {
		if ( nodes == null || nodes.isEmpty() )
			return false;
		eventHelper.fireEvent(new AboutToRemoveNodesEvent(this, nodes));

		CyTable nodeHiddenTable  = getTable(CyNode.class, CyNetwork.HIDDEN_ATTRS);
		CyTable nodeDefaultTable = getTable(CyNode.class, CyNetwork.DEFAULT_ATTRS);
		CyTable edgeHiddenTable  = getTable(CyEdge.class, CyNetwork.HIDDEN_ATTRS);
		CyTable edgeDefaultTable = getTable(CyEdge.class, CyNetwork.DEFAULT_ATTRS);
		
		List<Long> nodeSuids = new ArrayList<>();
		Set<Long> edgeSuids = new HashSet<>();
		Set<CyEdge> edges = new HashSet<>();

		for(CyNode node: nodes) {
			if (this.containsNode(node)) {
				Long nodeSuid = node.getSUID();
				if (nodeDefaultTable.rowExists(nodeSuid))
					nodeSuids.add(nodeSuid);
				
				for(CyEdge edge : getAdjacentEdgeIterable(node, Type.ANY)) {
					Long edgeSuid = edge.getSUID();
					if (edgeDefaultTable.rowExists(edgeSuid)) {
						edgeSuids.add(edgeSuid);
						edges.add(edge);
					}
				}
			}
		}

		boolean ret = removeNodesInternal(nodes);

		nodeHiddenTable.deleteRows(nodeSuids);
		nodeDefaultTable.deleteRows(nodeSuids);
		edgeHiddenTable.deleteRows(edgeSuids);
		edgeDefaultTable.deleteRows(edgeSuids);
		
		if(ret) {
			// must call subnetworkEdgesRemoved() first
			parent.subnetworkEdgesRemoved(edges);
			parent.subnetworkNodesRemoved(nodes);
		}
		if(ret) {
			eventHelper.fireEvent(new RemovedNodesEvent(this));
		}

		return ret;
	}

	@Override
	public boolean removeEdges(final Collection<CyEdge> edges) {
		if ( edges == null || edges.isEmpty() )
			return false;

		// Possible error if one of the edges isn't contained in subnetwork, but
		// since this is only a notification, maybe that's OK.
		eventHelper.fireEvent(new AboutToRemoveEdgesEvent(this, edges));

		CyTable hiddenTable = getTable(CyEdge.class, CyNetwork.HIDDEN_ATTRS);
		CyTable localTable = getTable(CyEdge.class, CyNetwork.LOCAL_ATTRS);
		CyTable defaultTable = getTable(CyEdge.class, CyNetwork.DEFAULT_ATTRS);
		List<Long> suids = new ArrayList<>();

		for(CyEdge edge: edges)
			if (this.containsEdge(edge)) {
				// getRow(edge).set(CyNetwork.SELECTED, false);
				suids.add(edge.getSUID());
			}

		boolean ret = removeEdgesInternal(edges);

		hiddenTable.deleteRows(suids);
		defaultTable.deleteRows(suids);
		// Shouldn't be needed since the default table is a facade on the local table
		// localTable.deleteRows(suids);

		if(ret) {
			parent.subnetworkEdgesRemoved(edges);
		}
		
		if(ret) {
			eventHelper.fireEvent(new RemovedEdgesEvent(this));
		}

		return ret;
	}

	@Override
	public SavePolicy getSavePolicy() {
		return savePolicy;
	}
	
	@Override
	public void handleEvent(final NetworkAddedEvent e) {
		if (e.getNetwork() == this) {
			registerSubnetworkTables();
			fireAddedNodesAndEdgesEvents = true;
		}
	}

	// We want to register the subnetwork tables AFTER we've
	// finished creating the network so that we don't fire
	// lots of table related events prematurely.
	private void registerSubnetworkTables() {
		for (final CyTable table : networkTableMgr.getTables(this, CyNetwork.class).values())
			tableMgr.addTable(table);
		for (final CyTable table : networkTableMgr.getTables(this, CyNode.class).values())
			tableMgr.addTable(table);
		for (final CyTable table : networkTableMgr.getTables(this, CyEdge.class).values())
			tableMgr.addTable(table);

		updateSharedNames( getDefaultNodeTable(), parent.getSharedNodeTable() );
		updateSharedNames( getDefaultEdgeTable(), parent.getSharedEdgeTable() );
		updateSharedInteractions(getDefaultEdgeTable(), parent.getSharedEdgeTable() );
		updateSharedNames( getDefaultNetworkTable(), parent.getSharedNetworkTable() );
	}

	private void updateSharedNames(CyTable src, CyTable tgt) {
		for (CyRow sr : src.getAllRows()) {
			CyRow tr = tgt.getRow(sr.get(CyIdentifiable.SUID, Long.class));

			if (tr.get(CyRootNetwork.SHARED_NAME, String.class) == null)
				tr.set(CyRootNetwork.SHARED_NAME, sr.get(CyNetwork.NAME, String.class));
		}
	}

	private void updateSharedInteractions(CyTable src, CyTable tgt) {
		for (CyRow sr : src.getAllRows()) {
			CyRow tr = tgt.getRow(sr.get(CyIdentifiable.SUID, Long.class));
			
			if (tr.get(CyRootNetwork.SHARED_INTERACTION, String.class) == null)
				tr.set(CyRootNetwork.SHARED_INTERACTION, sr.get(CyEdge.INTERACTION, String.class));
		}
	}
	
	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof CySubNetworkImpl))
			return false;

		return super.equals(o); 
	}

	@Override
	public String toString() {
		String name;
		try {
			name = getRow(this).get(NAME, String.class);
		} catch (NullPointerException e) {
			name = "(unavailable)";
		}
		return name; 
	}
	
	@Override
	public void dispose() {
		networkTableMgr.removeAllTables(this);
	}
}
