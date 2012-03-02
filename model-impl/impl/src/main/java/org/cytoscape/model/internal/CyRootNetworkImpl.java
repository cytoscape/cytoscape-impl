
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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.lang.ref.WeakReference;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableFactory.InitialTableSize;
import org.cytoscape.model.SUIDFactory;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.NetworkAddedEvent;
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
	private final CyEventHelper eventHelper;
	private final List<CySubNetwork> subNetworks;
	private final CySubNetwork base;
	private final CyTableManagerImpl tableMgr;
	private final CyNetworkTableManagerImpl networkTableMgr; 
	private final CyTableFactory tableFactory;
	private final CyServiceRegistrar serviceRegistrar;
	private final boolean publicTables;
	private final VirtualColumnAdder columnAdder;
	private final NameSetListener nameSetListener; 
	private final NetworkAddedListenerDelegator networkAddedListenerDelegator; 

	private int nextNodeIndex;
	private int nextEdgeIndex;

	public CyRootNetworkImpl(final CyEventHelper eh, 
	                         final CyTableManagerImpl tableMgr,
	                         final CyNetworkTableManagerImpl networkTableMgr,
	                         final CyTableFactory tableFactory,
	                         final CyServiceRegistrar serviceRegistrar, 
	                         final boolean publicTables)
	{
		super(SUIDFactory.getNextSUID(),tableFactory,publicTables,0);
		this.eventHelper = eh;
		this.tableMgr = tableMgr;
		this.networkTableMgr = networkTableMgr;
		this.tableFactory = tableFactory;
		this.publicTables = publicTables;
		this.serviceRegistrar = serviceRegistrar;
		suid = super.getSUID(); 
		subNetworks = new ArrayList<CySubNetwork>();
		nextNodeIndex = 0;
		nextEdgeIndex = 0;

		updateRootNetworkTables();

		networkTableMgr.setTableMap(CyNetwork.class, this, netTables);
		networkTableMgr.setTableMap(CyNode.class, this, nodeTables);
		networkTableMgr.setTableMap(CyEdge.class, this, edgeTables);

		columnAdder = new VirtualColumnAdder();
		serviceRegistrar.registerService(columnAdder, ColumnCreatedListener.class, new Properties());
		nameSetListener = new NameSetListener();
		serviceRegistrar.registerService(nameSetListener, RowsSetListener.class, new Properties());
		networkAddedListenerDelegator = new NetworkAddedListenerDelegator();
		serviceRegistrar.registerService(networkAddedListenerDelegator, NetworkAddedListener.class, new Properties());

		base = addSubNetwork(); 

		registerAllTables(netTables.values());
		registerAllTables(nodeTables.values());
		registerAllTables(edgeTables.values());
	}

	private void registerAllTables(Collection<CyTable> tables) {
		for (CyTable table : tables) {
			tableMgr.addTable(table);
		}
	}

	private void updateRootNetworkTables() {
		getRow(this).set(CyTableEntry.NAME, "");

		netTables.put(CyRootNetwork.SHARED_ATTRS, 
		              tableFactory.createTable(suid + " shared network", CyTableEntry.SUID, 
		                                       Long.class, publicTables, false, InitialTableSize.SMALL));
		netTables.get(CyRootNetwork.SHARED_ATTRS).createColumn(CyRootNetwork.SHARED_NAME, String.class, true);

		nodeTables.put(CyRootNetwork.SHARED_ATTRS, 
		               tableFactory.createTable(suid + " shared node", CyTableEntry.SUID, 
		                                        Long.class, publicTables, false, 
		                                        getInitialTableSize(subNetworks.size())));
		nodeTables.get(CyRootNetwork.SHARED_ATTRS).createColumn(CyRootNetwork.SHARED_NAME, String.class, true);

		edgeTables.put(CyRootNetwork.SHARED_ATTRS, 
		               tableFactory.createTable(suid + " shared edge", CyTableEntry.SUID, 
		                                        Long.class, publicTables, false, 
		                                        getInitialTableSize(subNetworks.size())));
        edgeTables.get(CyRootNetwork.SHARED_ATTRS).createColumn(CyRootNetwork.SHARED_NAME, String.class, true);
	}

	private void linkDefaultTables(CyTable srcTable, CyTable tgtTable) {
		// Add all columns from source table as virtual columns in target table.
		tgtTable.addVirtualColumns(srcTable,CyTableEntry.SUID,true);

		// Now add a listener for column created events to add
		// virtual columns to any subsequent source columns added.
		columnAdder.addInterestedTables(srcTable,tgtTable);

		// Another listener tracks changes to the NAME column in local tables
		nameSetListener.addInterestedTables(srcTable,tgtTable);
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
		for ( CySubNetwork sub : subNetworks )
			sub.removeNodes(nodes);
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
		for ( CySubNetwork sub : subNetworks )
			sub.removeEdges(edges);
		return removeEdgesInternal(edges);
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof CyRootNetworkImpl))
			return false;

		return super.equals(o); 
	}

	@Override
	public CySubNetwork addSubNetwork(final Iterable<CyNode> nodes, final Iterable<CyEdge> edges) {
		// Only addSubNetwork() modifies the internal state of CyRootNetworkImpl (this object), 
		// so because it's synchronized, we don't need to synchronize this method.
		final CySubNetwork sub = addSubNetwork();
		if ( nodes != null ) 
			for ( CyNode n : nodes )
				sub.addNode(n);
		if ( edges != null ) 
			for ( CyEdge e : edges )
				sub.addEdge(e);
		return sub;
	}

	@Override
	public synchronized CySubNetwork addSubNetwork() {
		final long newSUID = SUIDFactory.getNextSUID();
		final CySubNetworkImpl sub = new CySubNetworkImpl(this,newSUID,eventHelper,tableMgr,networkTableMgr,
		                                                  tableFactory,publicTables,subNetworks.size());
		networkAddedListenerDelegator.addListener(sub);

		linkDefaultTables( netTables.get(CyRootNetwork.SHARED_ATTRS), sub.getDefaultNetworkTable() );
		linkDefaultTables( nodeTables.get(CyRootNetwork.SHARED_ATTRS), sub.getDefaultNodeTable() );
		linkDefaultTables( edgeTables.get(CyRootNetwork.SHARED_ATTRS), sub.getDefaultEdgeTable() );
		subNetworks.add(sub);
		return sub;
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
		return netTables.get(CyRootNetwork.SHARED_ATTRS); 
	}

	@Override
	public CyTable getSharedNodeTable() {
		return nodeTables.get(CyRootNetwork.SHARED_ATTRS); 
	}

	@Override
	public CyTable getSharedEdgeTable() {
		return edgeTables.get(CyRootNetwork.SHARED_ATTRS); 
	}

	@Override
	public synchronized boolean containsNetwork(final CyNetwork net) {
		return subNetworks.contains(net);
	}

	@Override
	public String toString() {
		return "CyNetwork: " + suid + " name: " + getRow(this).get("name", String.class); 
	}

	private synchronized int getNextNodeIndex() {
		return nextNodeIndex++;
	}

	private synchronized int getNextEdgeIndex() {
		return nextEdgeIndex++;
	}
}
