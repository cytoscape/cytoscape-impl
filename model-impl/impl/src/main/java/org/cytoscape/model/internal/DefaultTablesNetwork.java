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


import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableFactory.InitialTableSize;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;


/**
 * A SimpleNetwork but with default table support added. 
 */
class DefaultTablesNetwork extends SimpleNetwork {

	protected final Map<String,CyTable> netTables;
	protected final Map<String,CyTable> nodeTables;
	protected final Map<String,CyTable> edgeTables;

	DefaultTablesNetwork(final long suid, 
	                     final CyTableFactory tableFactory, 
	                     final boolean publicTables, 
	                     final int tableSizeDeterminer) {	
		super(suid);
		netTables = createNetworkTables(suid, tableFactory, publicTables /* table size is always small */);
		nodeTables = createNodeTables(suid, tableFactory, publicTables, tableSizeDeterminer);
		edgeTables = createEdgeTables(suid, tableFactory, publicTables, tableSizeDeterminer);
	}

	public CyTable getDefaultNetworkTable() {
		return netTables.get(CyNetwork.DEFAULT_ATTRS); 
	}

	public CyTable getDefaultNodeTable() {
		return nodeTables.get(CyNetwork.DEFAULT_ATTRS); 
	}

	public CyTable getDefaultEdgeTable() {
		return edgeTables.get(CyNetwork.DEFAULT_ATTRS); 
	}

	public CyRow getRow(final CyTableEntry entry) {
		return getRow(entry, CyNetwork.DEFAULT_ATTRS);
	}

	public CyRow getRow(CyTableEntry entry, String tableName) {
		if ( entry == null )
			throw new NullPointerException("null entry");

		if ( tableName == null )
			throw new NullPointerException("null table name");

		CyTable table;

		synchronized (this) {
			if ( entry instanceof CyNode && containsNode((CyNode)entry) )
				table = nodeTables.get(tableName);
			else if ( entry instanceof CyEdge && containsEdge((CyEdge)entry) )
				table = edgeTables.get(tableName);
			else if ( entry instanceof CyNetwork && entry.equals(this) )
				table = netTables.get(tableName);
			else
				throw new IllegalArgumentException("unrecognized (table entry): " + entry.toString() + 
				                                   "  (table name): " + tableName);
		}

		return table.getRow(entry.getSUID());
	}


	private Map<String,CyTable> createNetworkTables(long suidx, CyTableFactory tableFactory, boolean pubTables) {
		Map<String,CyTable> netT = new HashMap<String, CyTable>();

		netT.put(CyNetwork.DEFAULT_ATTRS, 
		         tableFactory.createTable(suidx + " default network", CyTableEntry.SUID, 
		                                  Long.class, pubTables, false, InitialTableSize.SMALL));
		netT.put(CyNetwork.HIDDEN_ATTRS, 
		         tableFactory.createTable(suidx + " hidden network", CyTableEntry.SUID, 
		                                  Long.class, false, false, InitialTableSize.SMALL));

        netT.get(CyNetwork.DEFAULT_ATTRS).createColumn(CyTableEntry.NAME, String.class, true);

		return netT;
	}

	private Map<String,CyTable> createNodeTables(long suidx, CyTableFactory tableFactory, boolean pubTables, int num) {
		Map<String,CyTable> nodeT = new HashMap<String, CyTable>();

		nodeT.put(CyNetwork.DEFAULT_ATTRS, 
		          tableFactory.createTable(suidx + " default node", CyTableEntry.SUID, 
		                                   Long.class, pubTables, false, getInitialTableSize(num)));
		nodeT.put(CyNetwork.HIDDEN_ATTRS, 
		          tableFactory.createTable(suidx + " hidden node", CyTableEntry.SUID, 
			                               Long.class, false, false, getInitialTableSize(num)));

		nodeT.get(CyNetwork.DEFAULT_ATTRS).createColumn(CyTableEntry.NAME, String.class, true);
		nodeT.get(CyNetwork.DEFAULT_ATTRS).createColumn(CyNetwork.SELECTED, Boolean.class, true, Boolean.FALSE);

		return nodeT;

	}

	private Map<String,CyTable> createEdgeTables(long suidx, CyTableFactory tableFactory, boolean pubTables, int num) {
		Map<String,CyTable> edgeT = new HashMap<String, CyTable>();

		edgeT.put(CyNetwork.DEFAULT_ATTRS, 
		          tableFactory.createTable(suidx + " default edge", CyTableEntry.SUID, 
			                               Long.class, pubTables, false, getInitialTableSize(num)));
		edgeT.put(CyNetwork.HIDDEN_ATTRS, 
		          tableFactory.createTable(suidx + " hidden edge", CyTableEntry.SUID, 
			                               Long.class, false, false, getInitialTableSize(num)));

		edgeT.get(CyNetwork.DEFAULT_ATTRS).createColumn(CyTableEntry.NAME, String.class, true);
		edgeT.get(CyNetwork.DEFAULT_ATTRS).createColumn(CyNetwork.SELECTED, Boolean.class, true, Boolean.FALSE);
		edgeT.get(CyNetwork.DEFAULT_ATTRS).createColumn(CyEdge.INTERACTION, String.class, true);

		return edgeT;
	}

    protected static final InitialTableSize getInitialTableSize(int num) {
        if ( num < 5 )
            return InitialTableSize.LARGE;
        else if ( num < 15 )
            return InitialTableSize.MEDIUM;
        else
            return InitialTableSize.SMALL;
    }


}
