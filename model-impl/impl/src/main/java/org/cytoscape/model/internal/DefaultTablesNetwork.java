/*
 Copyright (c) 2008, 2011, The Cytoscape Consortium (www.cytoscape.org)

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


import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableFactory.InitialTableSize;


/**
 * A SimpleNetwork but with default table support added. 
 */
abstract class DefaultTablesNetwork extends SimpleNetwork {
	
	private final CyNetworkTableManager networkTableManager;
	
	private Reference<CyNetwork> networkRef;
	
	private final CyTableFactory tableFactory;
	private final boolean publicTables;
	private final int tableSizeDeterminer;

	DefaultTablesNetwork(final long suid, final CyNetworkTableManager tableManager, final CyTableFactory tableFactory,
			final boolean publicTables, final int tableSizeDeterminer) {
		super(suid);
		this.networkTableManager = tableManager;
		this.publicTables = publicTables;
		this.tableFactory = tableFactory;
		this.tableSizeDeterminer = tableSizeDeterminer;
	}
	
	protected void initTables(final CyNetwork network) {
		this.networkRef = new WeakReference<CyNetwork>(network);
		
		createNetworkTables(super.getSUID(), tableFactory, publicTables /* table size is always small */);
		createNodeTables(super.getSUID(), tableFactory, publicTables, tableSizeDeterminer);
		createEdgeTables(super.getSUID(), tableFactory, publicTables, tableSizeDeterminer);
	}

	public CyTable getDefaultNetworkTable() {
		return networkTableManager.getTable(networkRef.get(), CyNetwork.class, CyNetwork.DEFAULT_ATTRS); 
	}

	public CyTable getDefaultNodeTable() {
		return networkTableManager.getTable(networkRef.get(), CyNode.class, CyNetwork.DEFAULT_ATTRS); 
	}

	public CyTable getDefaultEdgeTable() {
		return networkTableManager.getTable(networkRef.get(), CyEdge.class, CyNetwork.DEFAULT_ATTRS); 
	}

	public CyRow getRow(final CyTableEntry entry) {
		return getRow(entry, CyNetwork.DEFAULT_ATTRS);
	}

	public CyRow getRow(final CyTableEntry entry, final String tableName) {
		if ( entry == null )
			throw new NullPointerException("null entry");

		if ( tableName == null )
			throw new NullPointerException("null table name");

		CyTable table;

		synchronized (this) {
			if (entry instanceof CyNode && containsNode((CyNode) entry))
				table = networkTableManager.getTable(networkRef.get(), CyNode.class, tableName);
			else if (entry instanceof CyEdge && containsEdge((CyEdge) entry))
				table = networkTableManager.getTable(networkRef.get(), CyEdge.class, tableName);
			else if (entry instanceof CyNetwork && entry.equals(this))
				table = networkTableManager.getTable(networkRef.get(), CyNetwork.class, tableName);
			else
				throw new IllegalArgumentException("unrecognized (table entry): " + entry.toString()
						+ "  (table name): " + tableName);
		}

		if(table == null)
			throw new NullPointerException("Table does not exist: " + tableName);
		
		return table.getRow(entry.getSUID());
	}


	private void createNetworkTables(long suidx, CyTableFactory tableFactory, boolean pubTables) {		
		final CyTable defTable = tableFactory.createTable(suidx
				+ " default network", CyTableEntry.SUID, Long.class, pubTables, false, InitialTableSize.SMALL);
		networkTableManager.setTable(networkRef.get(), CyNetwork.class, CyNetwork.DEFAULT_ATTRS, defTable);
		
		final CyTable hiddenTable = tableFactory.createTable(suidx
				+ " hidden network", CyTableEntry.SUID, Long.class, false, false, InitialTableSize.SMALL);
		networkTableManager.setTable(networkRef.get(), CyNetwork.class, CyNetwork.HIDDEN_ATTRS, hiddenTable);
		// Add default network columns.
		defTable.createColumn(CyTableEntry.NAME, String.class, true);
		
	}

	private void createNodeTables(long suidx, CyTableFactory tableFactory, boolean pubTables, int num) {
		final CyTable defTable = tableFactory.createTable(suidx
				+ " default node", CyTableEntry.SUID, Long.class, pubTables, false, InitialTableSize.SMALL);
		networkTableManager.setTable(networkRef.get(), CyNode.class, CyNetwork.DEFAULT_ATTRS, defTable);
		
		final CyTable hiddenTable = tableFactory.createTable(suidx
				+ " hidden node", CyTableEntry.SUID, Long.class, false, false, InitialTableSize.SMALL);
		networkTableManager.setTable(networkRef.get(), CyNode.class, CyNetwork.HIDDEN_ATTRS, hiddenTable);
		
		defTable.createColumn(CyTableEntry.NAME, String.class, true);
		defTable.createColumn(CyNetwork.SELECTED, Boolean.class, true, Boolean.FALSE);		
	}

	private void createEdgeTables(long suidx, CyTableFactory tableFactory, boolean pubTables, int num) {
		final CyTable defTable = tableFactory.createTable(suidx + " default edge", CyTableEntry.SUID, Long.class,
				pubTables, false, InitialTableSize.SMALL);
		networkTableManager.setTable(networkRef.get(), CyEdge.class, CyNetwork.DEFAULT_ATTRS, defTable);
		
		final CyTable hiddenTable = tableFactory.createTable(suidx
				+ " hidden edge", CyTableEntry.SUID, Long.class, false, false, InitialTableSize.SMALL);
		networkTableManager.setTable(networkRef.get(), CyEdge.class, CyNetwork.HIDDEN_ATTRS, hiddenTable);
		
		defTable.createColumn(CyTableEntry.NAME, String.class, true);
		defTable.createColumn(CyNetwork.SELECTED, Boolean.class, true, Boolean.FALSE);
		defTable.createColumn(CyEdge.INTERACTION, String.class, true);		
	}

	protected static final InitialTableSize getInitialTableSize(final int num) {
		if (num < 5)
			return InitialTableSize.LARGE;
		else if (num < 15)
			return InitialTableSize.MEDIUM;
		else
			return InitialTableSize.SMALL;
	}


}
