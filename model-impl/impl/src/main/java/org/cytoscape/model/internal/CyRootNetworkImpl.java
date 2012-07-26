
/*
 Copyright (c) 2008, 2010-2011, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.model.internal;


import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.CyTableFactory.InitialTableSize;
import org.cytoscape.model.SUIDFactory;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;


/**
 * A full implementation of CyRootNetwork. This implementation adds the support
 * for addNodes/addEdges that is missing from SimpleNetwork and provides support 
 * for subnetworks.
 */
public final class CyRootNetworkImpl extends DefaultTablesNetwork implements CyRootNetwork {

	private final long suid;
	private SavePolicy savePolicy;
	
	private final CyEventHelper eventHelper;
	private final List<CySubNetwork> subNetworks;
	private final CySubNetwork base;
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


	private int nextNodeIndex;
	private int nextEdgeIndex;

	public CyRootNetworkImpl(final CyEventHelper eh, 
	                         final CyTableManagerImpl tableMgr,
	                         final CyNetworkTableManager networkTableMgr,
	                         final CyTableFactory tableFactory,
	                         final CyServiceRegistrar serviceRegistrar, 
	                         final boolean publicTables,
	                         final SavePolicy savePolicy)
	{
		super(SUIDFactory.getNextSUID(), networkTableMgr, tableFactory,publicTables,0);
		
		assert(savePolicy != null);
		
		this.eventHelper = eh;
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
		
		initTables(this);
		updateRootNetworkTables();

		columnAdder = new VirtualColumnAdder();
		serviceRegistrar.registerService(columnAdder, ColumnCreatedListener.class, new Properties());
		nameSetListener = new NameSetListener();
		serviceRegistrar.registerService(nameSetListener, RowsSetListener.class, new Properties());
		interactionSetListener = new InteractionSetListener();
		serviceRegistrar.registerService(interactionSetListener, RowsSetListener.class, new Properties());
		networkAddedListenerDelegator = new NetworkAddedListenerDelegator();
		serviceRegistrar.registerService(networkAddedListenerDelegator, NetworkAddedListener.class, new Properties());

		networkNameSetListener = new NetworkNameSetListener(this);
		serviceRegistrar.registerService(networkNameSetListener, RowsSetListener.class, new Properties());		
		serviceRegistrar.registerService(networkNameSetListener, NetworkAddedListener.class, new Properties());		

		registerAllTables(networkTableMgr.getTables(this, CyNetwork.class).values());
		registerAllTables(networkTableMgr.getTables(this, CyNode.class).values());
		registerAllTables(networkTableMgr.getTables(this, CyEdge.class).values());
		
		base = addSubNetwork();
	}

	@Override
	public void dispose() {
		serviceRegistrar.unregisterAllServices(columnAdder);
		serviceRegistrar.unregisterAllServices(nameSetListener);
		serviceRegistrar.unregisterAllServices(interactionSetListener);
		serviceRegistrar.unregisterAllServices(networkAddedListenerDelegator);
		serviceRegistrar.unregisterAllServices(networkNameSetListener);
		
		for (CySubNetwork network : subNetworks) {
			network.dispose();
		}
		networkTableMgr.removeAllTables(this);
	}
	
	// Simply register all tables to the table manager
	private void registerAllTables(Collection<CyTable> tables) {
		for (final CyTable table : tables)
			tableMgr.addTable(table);
	}

	private void updateRootNetworkTables() {
		
		final CyTable edgeSharedTable = tableFactory.createTable(suid + " shared edge", CyIdentifiable.SUID, Long.class,
				publicTables, false, getInitialTableSize(subNetworks.size()));
		networkTableMgr.setTable(this, CyEdge.class, CyRootNetwork.SHARED_ATTRS, edgeSharedTable);
		
		edgeSharedTable.createColumn(CyRootNetwork.SHARED_NAME, String.class, true);
		edgeSharedTable.createColumn(CyRootNetwork.SHARED_INTERACTION, String.class, true);
		
		final CyTable networkSharedTable = tableFactory.createTable(suid
				+ " shared network", CyIdentifiable.SUID, Long.class, publicTables, false, InitialTableSize.SMALL);
		networkTableMgr.setTable(this, CyNetwork.class, CyRootNetwork.SHARED_ATTRS, networkSharedTable);
		
		networkSharedTable.createColumn(CyRootNetwork.SHARED_NAME, String.class, true);
		
		final CyTable nodeSharedTable = tableFactory.createTable(suid + " shared node", CyIdentifiable.SUID, Long.class,
				publicTables, false, getInitialTableSize(subNetworks.size()));
		networkTableMgr.setTable(this, CyNode.class, CyRootNetwork.SHARED_ATTRS, nodeSharedTable);
		
		nodeSharedTable.createColumn(CyRootNetwork.SHARED_NAME, String.class, true);
		
		getRow(this).set(CyNetwork.NAME, "");
		
	}

	private void linkDefaultTables(CyTable sharedTable, CyTable localTable) {
		// Add all columns from source table as virtual columns in target table.
		localTable.addVirtualColumns(sharedTable, CyIdentifiable.SUID, true);

		// Now add a listener for column created events to add
		// virtual columns to any subsequent source columns added.
		columnAdder.addInterestedTables(sharedTable,localTable);

		// Another listener tracks changes to the NAME column in local tables
		nameSetListener.addInterestedTables(localTable, sharedTable);
				
	}

	@Override
	public CyNode addNode() {

		final CyNode node; 

		synchronized (this) {
			node = new CyNodeImpl( SUIDFactory.getNextSUID(), getNextNodeIndex(), eventHelper );
			addNodeInternal( node );
		}

		return node; 
	}

	@Override
	public synchronized boolean removeNodes(final Collection<CyNode> nodes) {
		for ( CySubNetwork sub : subNetworks ) {
			sub.removeNodes(nodes);
			if (nodes != null && sub instanceof CySubNetworkImpl)
				((CySubNetworkImpl) sub).removeRows(nodes, CyNode.class);
		}
		
		return removeNodesInternal(nodes);
	}

	@Override
	public CyEdge addEdge(final CyNode s, final CyNode t, final boolean directed) {

		final CyEdge edge;

		synchronized (this) {
			edge = new CyEdgeImpl(SUIDFactory.getNextSUID(), s, t, directed, getNextEdgeIndex());
			addEdgeInternal(s,t, directed, edge);
		}

		return edge; 
	}

	@Override
	public synchronized boolean removeEdges(final Collection<CyEdge> edges) {
		for ( CySubNetwork sub : subNetworks ) {
			sub.removeEdges(edges);
			if (edges != null && sub instanceof CySubNetworkImpl)
				((CySubNetworkImpl) sub).removeRows(edges, CyEdge.class);
		}
		return removeEdgesInternal(edges);
	}

	@Override
	public CySubNetwork addSubNetwork(final Iterable<CyNode> nodes, final Iterable<CyEdge> edges) {
		return addSubNetwork(nodes, edges, savePolicy);
	}
	
	@Override
	public CySubNetwork addSubNetwork(final Iterable<CyNode> nodes, final Iterable<CyEdge> edges,
			final SavePolicy policy) {
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
	public synchronized CySubNetwork addSubNetwork() {
		return addSubNetwork(savePolicy);
	}
	
	@Override
	public synchronized CySubNetwork addSubNetwork(SavePolicy policy) {
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
		
		CyTable networkTable = networkTableMgr.getTable(this, CyNetwork.class, CyRootNetwork.SHARED_ATTRS);
		CyTable nodeTable = networkTableMgr.getTable(this, CyNode.class, CyRootNetwork.SHARED_ATTRS);
		CyTable edgeTable = networkTableMgr.getTable(this, CyEdge.class, CyRootNetwork.SHARED_ATTRS);
		
		linkDefaultTables(networkTable, sub.getDefaultNetworkTable());
		linkDefaultTables(nodeTable, sub.getDefaultNodeTable());
		linkDefaultTables(edgeTable, sub.getDefaultEdgeTable());
		// Another listener tracks changes to the interaction column in local tables
		interactionSetListener.addInterestedTables(sub.getDefaultEdgeTable(), edgeTable);
		subNetworks.add(sub);
		
		return sub;
	}

	@Override
	public synchronized void removeSubNetwork(final CySubNetwork sub) {
		if ( sub == null )
			return;

		if ( sub.equals(base) )
			throw new IllegalArgumentException("Can't remove base network from RootNetwork");

		if ( !subNetworks.contains(sub) )
			throw new IllegalArgumentException("Subnetwork not a member of this RootNetwork " + sub);

		// clean up pointers for nodes in subnetwork
		sub.removeNodes(sub.getNodeList());

		subNetworks.remove( sub );
		sub.dispose();
	}

	@Override
	public List<CySubNetwork> getSubNetworkList() {
		return Collections.synchronizedList(subNetworks);
	}

	@Override
	public CySubNetwork getBaseNetwork() {
		return base;
	}

	@Override
	public CyTable getSharedNetworkTable() {
		return networkTableMgr.getTable(this, CyNetwork.class, CyRootNetwork.SHARED_ATTRS); 
	}

	@Override
	public CyTable getSharedNodeTable() {
		return networkTableMgr.getTable(this, CyNode.class, CyRootNetwork.SHARED_ATTRS); 
	}

	@Override
	public CyTable getSharedEdgeTable() {
		return networkTableMgr.getTable(this, CyEdge.class, CyRootNetwork.SHARED_ATTRS); 
	}

	@Override
	public synchronized boolean containsNetwork(final CyNetwork net) {
		return subNetworks.contains(net);
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
		return "CyRootNetwork: " + suid + " name: " + getRow(this).get("name", String.class); 
	}

	private synchronized int getNextNodeIndex() {
		return nextNodeIndex++;
	}

	private synchronized int getNextEdgeIndex() {
		return nextEdgeIndex++;
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
}
