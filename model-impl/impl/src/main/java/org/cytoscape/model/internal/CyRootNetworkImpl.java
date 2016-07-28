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


import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableFactory.InitialTableSize;
import org.cytoscape.model.SUIDFactory;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;


/**
 * A full implementation of CyRootNetwork. This implementation adds the support
 * for addNodes/addEdges that is missing from SimpleNetwork and provides support 
 * for subnetworks.
 */
public final class CyRootNetworkImpl extends DefaultTablesNetwork implements CyRootNetwork, NetworkAddedListener, SessionLoadedListener {
	
	private final long suid;
	private SavePolicy savePolicy;
	
	private final List<CySubNetwork> subNetworks;
	private CySubNetwork base;
	private final CyTableManagerImpl tableMgr;
	private final CyNetworkTableManager networkTableMgr; 
	private final CyTableFactory tableFactory;
	private final boolean publicTables;
	private final VirtualColumnAdder columnAdder;
	private final NameSetListener nameSetListener; 
	private final InteractionSetListener interactionSetListener;
	private final NetworkAddedListenerDelegator networkAddedListenerDelegator;
	private final NetworkNameSetListener networkNameSetListener;
	private final CyServiceRegistrar serviceRegistrar;
	private final Object lock = new Object();

	private int nextNodeIndex;
	private int nextEdgeIndex;
	
	private final RemovedAttributesCache removedAttributesCache;
	

	public CyRootNetworkImpl(final CyEventHelper eh, 
	                         final CyTableManagerImpl tableMgr,
	                         final CyNetworkTableManager networkTableMgr,
	                         final CyTableFactory tableFactory,
	                         final CyServiceRegistrar serviceRegistrar, 
	                         final boolean publicTables,
	                         final SavePolicy savePolicy)
	{
		super(SUIDFactory.getNextSUID(), networkTableMgr, tableFactory,publicTables,0,eh);
		
		assert(savePolicy != null);
		
		this.tableMgr = tableMgr;
		this.networkTableMgr = networkTableMgr;
		this.tableFactory = tableFactory;
		this.serviceRegistrar = serviceRegistrar;
		this.publicTables = publicTables;
		this.savePolicy = savePolicy;
		suid = super.getSUID(); 
		subNetworks = new ArrayList<CySubNetwork>();
		nextNodeIndex = 0;
		nextEdgeIndex = 0;
		
		createRootNetworkTables();
		initTables(this, 
		           (SharedTableFacade)(networkTableMgr.getTable(this, CyNetwork.class, CyRootNetwork.SHARED_DEFAULT_ATTRS)),
		           (SharedTableFacade)(networkTableMgr.getTable(this, CyNode.class, CyRootNetwork.SHARED_DEFAULT_ATTRS)),
				   (SharedTableFacade)(networkTableMgr.getTable(this, CyEdge.class, CyRootNetwork.SHARED_DEFAULT_ATTRS)) );
		setRootNetworkTablePrivacy();
		
		getRow(this).set(CyNetwork.NAME, "");

		removedAttributesCache = new RemovedAttributesCache(networkTableMgr, this);
		
		columnAdder = new VirtualColumnAdder();
		serviceRegistrar.registerService(columnAdder, ColumnCreatedListener.class, new Properties());
		nameSetListener = new NameSetListener();
		serviceRegistrar.registerService(nameSetListener, RowsSetListener.class, new Properties());
		interactionSetListener = new InteractionSetListener();
		serviceRegistrar.registerService(interactionSetListener, RowsSetListener.class, new Properties());
		networkAddedListenerDelegator = new NetworkAddedListenerDelegator();
		networkAddedListenerDelegator.addListener(this);
		serviceRegistrar.registerService(networkAddedListenerDelegator, NetworkAddedListener.class, new Properties());
		
		serviceRegistrar.registerService(this, SessionLoadedListener.class, new Properties());

		networkNameSetListener = new NetworkNameSetListener(this);
		serviceRegistrar.registerService(networkNameSetListener, RowsSetListener.class, new Properties());		
		serviceRegistrar.registerService(networkNameSetListener, NetworkAddedListener.class, new Properties());		
		
		base = addSubNetwork();
	}

	@Override
	public void dispose() {
		Map<String, CyTable> tableMap;
		serviceRegistrar.unregisterAllServices(columnAdder);
		serviceRegistrar.unregisterAllServices(nameSetListener);
		serviceRegistrar.unregisterAllServices(interactionSetListener);
		serviceRegistrar.unregisterAllServices(networkAddedListenerDelegator);
		serviceRegistrar.unregisterAllServices(networkNameSetListener);
		serviceRegistrar.unregisterService(this, SessionLoadedListener.class);
		
		for (CySubNetwork network : subNetworks) {
			network.dispose();
		}
		tableMap = networkTableMgr.getTables(this, CyNetwork.class);
		for (CyTable table : tableMap.values()) {
			tableMgr.deleteTableInternal(table.getSUID(), true);
		}
		tableMap = networkTableMgr.getTables(this, CyNode.class);
		for (CyTable table : tableMap.values()) {
			tableMgr.deleteTableInternal(table.getSUID(), true);
		}
		tableMap = networkTableMgr.getTables(this, CyEdge.class);
		for (CyTable table : tableMap.values()) {
			tableMgr.deleteTableInternal(table.getSUID(), true);
		}
		networkTableMgr.removeAllTables(this);
		removedAttributesCache.dispose();
	}
	
	// Simply register all tables to the table manager
	private void registerAllTables(Collection<CyTable> tables) {
		for (final CyTable table : tables)
			tableMgr.addTable(table);
	}
	private void setRootNetworkTablePrivacy(){
		this.getDefaultEdgeTable().setPublic(false);
		this.getDefaultNetworkTable().setPublic(false);
		this.getDefaultNodeTable().setPublic(false);
		
	}
	private void createRootNetworkTables() {
		final CyTable rawEdgeSharedTable = tableFactory.createTable(suid + " shared edge", CyIdentifiable.SUID, Long.class, false /*all root tables are private*/, false, getInitialTableSize(subNetworks.size()));
		
		final CyTable edgeSharedTable = new SharedTableFacade(rawEdgeSharedTable,this,CyEdge.class,networkTableMgr, eventHelper);
		edgeSharedTable.setPublic(false /*all root tables are private*/);
		
		networkTableMgr.setTable(this, CyEdge.class, CyRootNetwork.SHARED_ATTRS, rawEdgeSharedTable);
		networkTableMgr.setTable(this, CyEdge.class, CyRootNetwork.SHARED_DEFAULT_ATTRS, edgeSharedTable);

		edgeSharedTable.createColumn(CyRootNetwork.SHARED_NAME, String.class, true);
		edgeSharedTable.createColumn(CyRootNetwork.SHARED_INTERACTION, String.class, true);
		
		final CyTable rawNetworkSharedTable = tableFactory.createTable(suid
				+ " shared network", CyIdentifiable.SUID, Long.class, false /*all root tables are private*/, false, InitialTableSize.SMALL);
		
		final CyTable networkSharedTable = new SharedTableFacade(rawNetworkSharedTable,this,CyNetwork.class,networkTableMgr, eventHelper);
		networkSharedTable.setPublic(false /*all root tables are private*/);
		
		networkTableMgr.setTable(this, CyNetwork.class, CyRootNetwork.SHARED_ATTRS, rawNetworkSharedTable);
		networkTableMgr.setTable(this, CyNetwork.class, CyRootNetwork.SHARED_DEFAULT_ATTRS, networkSharedTable);
		
		networkSharedTable.createColumn(CyRootNetwork.SHARED_NAME, String.class, true);
		
		final CyTable rawNodeSharedTable = tableFactory.createTable(suid + " shared node", CyIdentifiable.SUID, Long.class, false /*all root tables are private*/, false, getInitialTableSize(subNetworks.size()));
		
		final CyTable nodeSharedTable = new SharedTableFacade(rawNodeSharedTable,this,CyNode.class,networkTableMgr, eventHelper);
		nodeSharedTable.setPublic(false /*all root tables are private*/);
		
		networkTableMgr.setTable(this, CyNode.class, CyRootNetwork.SHARED_ATTRS, rawNodeSharedTable);
		networkTableMgr.setTable(this, CyNode.class, CyRootNetwork.SHARED_DEFAULT_ATTRS, nodeSharedTable);
		
		nodeSharedTable.createColumn(CyRootNetwork.SHARED_NAME, String.class, true);
	}

	private void linkDefaultTables(CyTable sharedTable, CyTable localTable) {
		// Add all columns from source table as virtual columns in target table.
//		localTable.addVirtualColumns(sharedTable, CyIdentifiable.SUID, true);

		// Now add a listener for column created events to add
		// virtual columns to any subsequent source columns added.
//		columnAdder.addInterestedTables(sharedTable,localTable);

		// Another listener tracks changes to the NAME column in local tables
		nameSetListener.addInterestedTables(localTable, sharedTable);
				
	}

	@Override
	public CyNode addNode() {

		final CyNode node; 

		synchronized (lock) {
			node = new CyNodeImpl( SUIDFactory.getNextSUID(), getNextNodeIndex(), eventHelper );
			addNodeInternal( node );
		}

		return node; 
	}

	@Override
	public boolean removeNodes(final Collection<CyNode> nodes) {
		synchronized (lock) {
			boolean removed = false;
			
			for ( CySubNetwork sub : subNetworks ) {
				removed |= sub.removeNodes(nodes);
				if (nodes != null && sub instanceof CySubNetworkImpl)
					((CySubNetworkImpl) sub).removeRows(nodes, CyNode.class);
			}
	
			// Explicitly removing from the root network removes from the cache
			if(nodes != null) {
				for(CyNode node : nodes) {
					removedAttributesCache.evict(node);
				}
			}
			
			// Do we want to do this????? (MK: yes I think so)
			this.removeRows(nodes, CyNode.class);
			
			removed |= removeNodesInternal(nodes);
			
			return removed;
		}
	}
	
	// Check if the nodes have been removed from all subnetworks, 
	// if so move the root instanece of the node to the recycle bin.
	void subnetworkNodesRemoved(Collection<CyNode> nodes) {
		List<CyNode> nodesToCache = nodes.stream()   // cache the nodes if they are:
			.filter(n -> containsNode(n))            // - contained in this root network
			.filter(n -> !anySubnetworkContains(n))  // - not contained in any subnetwork
			.collect(Collectors.toList());
		
		removedAttributesCache.cache(nodesToCache);
		removeRows(nodesToCache, CyNode.class);
		removeNodesInternal(nodesToCache);
	}
	
	void subnetworkEdgesRemoved(Collection<CyEdge> edges) {
		List<CyEdge> edgesToCache = edges.stream()  // cache the edges if they are:
			.filter(e -> containsEdge(e))           // - contained in this root network
			.filter(e -> !anySubnetworkContains(e)) // - not contained in any subnetwork
			.collect(Collectors.toList());
		
		removedAttributesCache.cache(edgesToCache);
		removeRows(edgesToCache, CyEdge.class);
		removeEdgesInternal(edgesToCache);
		
	}
	
	void garbageCollect() {
		List<CyNode> garbageNodes = 
			getNodeList().stream()                   // garbage nodes are:
			.filter(n -> !anySubnetworkContains(n))  // - not contained in any subnetwork
			.collect(Collectors.toList());
		List<CyEdge> garbageEdges =
			getEdgeList().stream()                   // garbage edges are:
			.filter(e -> !anySubnetworkContains(e))  // - not contained in any subnetwork
			.collect(Collectors.toList());

		removedAttributesCache.cache(garbageNodes);
		removedAttributesCache.cache(garbageEdges);
		removeRows(garbageNodes, CyNode.class);
		removeRows(garbageEdges, CyEdge.class);
		removeNodesInternal(garbageNodes);
		removeEdgesInternal(garbageEdges);
	}
	
	
	CyRow getCachedAttributes(CyIdentifiable element) {
		return getCachedAttributes(element, CyNetwork.DEFAULT_ATTRS);
	}
	
	CyRow getCachedAttributes(CyIdentifiable element, String namespace) {
		if(element instanceof CyNode && super.containsNode((CyNode)element)) {
			return this.getRow(element, namespace);
		}
		if(element instanceof CyEdge && super.containsEdge((CyEdge)element)) {
			return this.getRow(element, namespace);
		}
		if(removedAttributesCache.contains(element)) {
			return removedAttributesCache.getAttributes(element, namespace);
		}
		return null;
	}
	
	
	boolean cachedEdge(CyEdge edge) {
		return removedAttributesCache.contains(edge);
	}
	
	boolean cachedNode(CyNode node) {
		return removedAttributesCache.contains(node);
	}
	
	
	private boolean anySubnetworkContains(CyNode node) {
		return subNetworks.stream().anyMatch(sub -> sub.containsNode(node));
	}
	
	private boolean anySubnetworkContains(CyEdge edge) {
		return subNetworks.stream().anyMatch(sub -> sub.containsEdge(edge));
	}
	
	
	@Override
	public void restoreNode(CyNode node) {
		if(containsNode(node))
			return;
		if(removedAttributesCache.contains(node)) {
			removedAttributesCache.restore(node);
			addNodeInternal(node);
		}
	}
	
	@Override
	public void restoreEdge(CyEdge edge) {
		if(containsEdge(edge))
			return;
		if(removedAttributesCache.contains(edge)) {
			removedAttributesCache.restore(edge);
			addEdgeInternal(edge.getSource(), edge.getTarget(), edge.isDirected(), edge);
		}
	}
	

	@Override
	public CyEdge addEdge(final CyNode s, final CyNode t, final boolean directed) {

		final CyEdge edge;

		synchronized (lock) {
			edge = new CyEdgeImpl(SUIDFactory.getNextSUID(), s, t, directed, getNextEdgeIndex());
			addEdgeInternal(s,t, directed, edge);
		}

		return edge; 
	}

	@Override
	public boolean removeEdges(final Collection<CyEdge> edges) {
		synchronized (lock) {
			for ( CySubNetwork sub : subNetworks ) {
				sub.removeEdges(edges);
				if (edges != null && sub instanceof CySubNetworkImpl)
					((CySubNetworkImpl) sub).removeRows(edges, CyEdge.class);
			}
			
			// Explicitly removing from the root network removes from the cache
			if(edges != null) {
				for(CyEdge edge : edges) {
					removedAttributesCache.evict(edge);
				}
			}
						
			return removeEdgesInternal(edges);
		}
	}
	
	@Override
	public CySubNetwork addSubNetwork(final Iterable<CyNode> nodes, final Iterable<CyEdge> edges) {
		return addSubNetwork(nodes, edges, savePolicy);
	}
	
	@Override
	public CySubNetwork addSubNetwork(final Iterable<CyNode> nodes, final Iterable<CyEdge> edges, final SavePolicy policy) {
		// Only addSubNetwork() modifies the internal state of CyRootNetworkImpl (this object), 
		// so because it's synchronized, we don't need to synchronize this method.
		final CySubNetwork sub = addSubNetwork(policy);
		
		if (nodes != null)
			for (CyNode n : nodes)
				sub.addNode(n);
		if (edges != null)
			for (CyEdge e : edges)
				sub.addEdge(e);
		
		return sub;
	}

	@Override
	public CySubNetwork addSubNetwork() {
		synchronized (lock) {
			return addSubNetwork(savePolicy);
		}
	}
	
	@Override
	public CySubNetwork addSubNetwork(SavePolicy policy) {
		synchronized (lock) {
			if (policy == null)
				policy = savePolicy;
			
			if (savePolicy == SavePolicy.DO_NOT_SAVE && policy != savePolicy)
				throw new IllegalArgumentException("Cannot create subnetwork with \"" + policy
						+ "\" save policy, because this root network's policy is \"DO_NOT_SAVE\".");
			
			// Subnetwork's ID
			final long newSUID = SUIDFactory.getNextSUID();
			
			final CySubNetworkImpl sub = new CySubNetworkImpl(this, newSUID, eventHelper, tableMgr, networkTableMgr,
					tableFactory, publicTables, subNetworks.size(), policy);	
			networkAddedListenerDelegator.addListener(sub);
			
			subNetworks.add(sub);
			nameSetListener.addInterestedTables(sub.getDefaultNetworkTable(),networkTableMgr.getTable(this, CyNetwork.class, CyRootNetwork.SHARED_DEFAULT_ATTRS));
			nameSetListener.addInterestedTables(sub.getDefaultNodeTable(),networkTableMgr.getTable(this, CyNode.class, CyRootNetwork.SHARED_DEFAULT_ATTRS));
			nameSetListener.addInterestedTables(sub.getDefaultEdgeTable(),networkTableMgr.getTable(this, CyEdge.class, CyRootNetwork.SHARED_DEFAULT_ATTRS));
			interactionSetListener.addInterestedTables(sub.getDefaultEdgeTable(), networkTableMgr.getTable(this, CyEdge.class, CyRootNetwork.SHARED_DEFAULT_ATTRS));
	
			
			return sub;
		}
	}

	@Override
	public void removeSubNetwork(final CySubNetwork sub) {
		synchronized (lock) {
			if (sub == null)
				return;
	
			if (!subNetworks.contains(sub))
				throw new IllegalArgumentException("Subnetwork not a member of this RootNetwork " + sub);
	
			if (sub.equals(base)) {
				if (subNetworks.size() == 1)
					throw new IllegalArgumentException(
							"Can't remove base network from RootNetwork because it's the only subnetwork");
	
				// Chose another base network
				CySubNetwork oldBase = base;
				
				for (CySubNetwork n : subNetworks) {
					if (!n.equals(oldBase)) {
						base = n;
						
						// Better if the new base network is one that can be saved
						if (n.getSavePolicy() == SavePolicy.SESSION_FILE)
							break;
					}
				}
			}
			
			// clean up pointers for nodes in subnetwork
			sub.removeNodes(sub.getNodeList());
			Map<String, CyTable> tableMap;
	
			tableMap = networkTableMgr.getTables(sub, CyNetwork.class);
			for (CyTable table : tableMap.values()) {
				tableMgr.deleteTableInternal(table.getSUID(), true);
			}
	
			tableMap = networkTableMgr.getTables(sub, CyNode.class);
			for (CyTable table : tableMap.values()) {
				tableMgr.deleteTableInternal(table.getSUID(), true);
			}
	
			tableMap = networkTableMgr.getTables(sub, CyEdge.class);
			for (CyTable table : tableMap.values()) {
				tableMgr.deleteTableInternal(table.getSUID(), true);
			}
	
			subNetworks.remove(sub);
			sub.dispose();
			
			garbageCollect();
		}
	}

	@Override
	public List<CySubNetwork> getSubNetworkList() {
		return new ArrayList<CySubNetwork>(subNetworks);
	}

	@Override
	public CySubNetwork getBaseNetwork() {
		return base;
	}

	@Override
	public CyTable getSharedNetworkTable() {
		return networkTableMgr.getTable(this, CyNetwork.class, CyRootNetwork.SHARED_DEFAULT_ATTRS); 
	}

	@Override
	public CyTable getSharedNodeTable() {
		return networkTableMgr.getTable(this, CyNode.class, CyRootNetwork.SHARED_DEFAULT_ATTRS); 
	}

	@Override
	public CyTable getSharedEdgeTable() {
		return networkTableMgr.getTable(this, CyEdge.class, CyRootNetwork.SHARED_DEFAULT_ATTRS); 
	}

	@Override
	public boolean containsNetwork(final CyNetwork net) {
		synchronized (lock) {
			return subNetworks.contains(net);
		}
	}
	
	@Override
	public SavePolicy getSavePolicy() {
		return savePolicy;
	}
	
	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof CyRootNetworkImpl))
			return false;

		return super.equals(o); 
	}
	
	@Override
	public String toString() {
		String name = null;
		try {
			name = getRow(this).get(NAME, String.class);
		} catch (NullPointerException e) {
			name = "(unavailable)";
		}
		
//		return "CyRootNetwork[name=" + name + " ,nodes=" + getNodeCount() + " ,edges=" + getEdgeCount() + ", " + removedAttributesCache.toString() + "]";
		return name; 
	}

	private int getNextNodeIndex() {
		synchronized (lock) {
			return nextNodeIndex++;
		}
	}

	private int getNextEdgeIndex() {
		synchronized (lock) {
			return nextEdgeIndex++;
		}
	}
	
	private class NetworkAddedListenerDelegator implements NetworkAddedListener {
		List<WeakReference<NetworkAddedListener>> listeners = new ArrayList<WeakReference<NetworkAddedListener>>();
		public void addListener(NetworkAddedListener l) {
			listeners.add(new WeakReference<NetworkAddedListener>(l));
		}
		public void handleEvent(NetworkAddedEvent e) {
			for (WeakReference<NetworkAddedListener> ref : listeners) {
				final NetworkAddedListener l = ref.get();
				if ( l != null )
					l.handleEvent(e);
			}
		}
	}

	@Override
	public void handleEvent(NetworkAddedEvent e) {
		if(e.getNetwork() == this || subNetworks.contains(e.getNetwork())) {
			// Check if another network from this root was already registered - return if so
			CyNetworkManager netManager = e.getSource();
			List<CyNetwork> networks = new ArrayList<CyNetwork>();
			networks.add(this);
			networks.addAll(subNetworks);
			networks.remove(e.getNetwork());
			for(CyNetwork network: networks) {
				if(netManager.networkExists(network.getSUID())) 
					return;
			}
			
			registerAllTables(networkTableMgr.getTables(this, CyNetwork.class).values());
			registerAllTables(networkTableMgr.getTables(this, CyNode.class).values());
			registerAllTables(networkTableMgr.getTables(this, CyEdge.class).values());
		}
	}
	
	@Override
	public void handleEvent(SessionLoadedEvent e) {
		// remove any unreachable nodes left over from older versions of cytoscape
		garbageCollect();
	}
}
