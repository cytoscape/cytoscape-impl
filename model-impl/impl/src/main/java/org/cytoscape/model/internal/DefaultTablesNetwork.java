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


import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
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
	protected final CyEventHelper eventHelper;

	DefaultTablesNetwork(final long suid, final CyNetworkTableManager tableManager, final CyTableFactory tableFactory,
			final boolean publicTables, final int tableSizeDeterminer, final CyEventHelper eventHelper) {
		super(suid);
		this.networkTableManager = tableManager;
		this.publicTables = publicTables;
		this.tableFactory = tableFactory;
		this.tableSizeDeterminer = tableSizeDeterminer;
		this.eventHelper = eventHelper;
	}
	
	protected void initTables(final CyNetwork network, final SharedTableFacade sharedNetworkTable, 
	                          final SharedTableFacade sharedNodeTable, final SharedTableFacade sharedEdgeTable) {
		this.networkRef = new WeakReference<CyNetwork>(network);
		
		createNetworkTables(super.getSUID(), tableFactory, publicTables /* table size is always small */, sharedNetworkTable);
		createNodeTables(super.getSUID(), tableFactory, publicTables, tableSizeDeterminer, sharedNodeTable);
		createEdgeTables(super.getSUID(), tableFactory, publicTables, tableSizeDeterminer, sharedEdgeTable);
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

	public CyTable getTable(Class<? extends CyIdentifiable> type, String namespace) {
		return networkTableManager.getTable(networkRef.get(), type, namespace); 
	}

	public CyRow getRow(final CyIdentifiable entry) {
		return getRow(entry, CyNetwork.DEFAULT_ATTRS);
	}

	public CyRow getRow(final CyIdentifiable entry, final String tableName) {
		if ( entry == null )
			throw new NullPointerException("null entry");

		if ( tableName == null )
			throw new NullPointerException("null table name");

		CyTable table;

		// The table returned should be immutable.
		if (entry instanceof CyNode && containsNode((CyNode) entry))
			table = networkTableManager.getTable(networkRef.get(), CyNode.class, tableName);
		else if (entry instanceof CyEdge && containsEdge((CyEdge) entry))
			table = networkTableManager.getTable(networkRef.get(), CyEdge.class, tableName);
		else if (entry instanceof CyNetwork && entry.equals(this)) {
			if (networkRef == null)
				throw new IllegalArgumentException("Network reference is null.  This should not be null.");
			final CyNetwork n = networkRef.get();
			table = networkTableManager.getTable(n, CyNetwork.class, tableName);
		} else
			throw new IllegalArgumentException("unrecognized (table entry): " + entry.toString() + "  (table name): "
					+ tableName);

		if(table == null)
			throw new NullPointerException("Table does not exist: " + tableName);
		
		return table.getRow(entry.getSUID());
	}

	
	private void createNetworkTables(long suidx, CyTableFactory tableFactory, boolean pubTables, SharedTableFacade sharedNetworkTable) {		
		final CyTable defTable = tableFactory.createTable(suidx
				+ " default network", CyIdentifiable.SUID, Long.class, false /* all local tables are private*/, false, InitialTableSize.SMALL);
		
		networkTableManager.setTable(networkRef.get(), CyNetwork.class, CyNetwork.LOCAL_ATTRS, defTable);
		LocalTableFacade localTable = new LocalTableFacade(defTable,sharedNetworkTable, eventHelper);
		localTable.setPublic(pubTables); //Set the privacy of facade tables based on the given flag
		networkTableManager.setTable(networkRef.get(), CyNetwork.class, CyNetwork.DEFAULT_ATTRS, localTable);
		if ( eventHelper instanceof TableEventHelperFacade )
			((TableEventHelperFacade)eventHelper).registerFacade(localTable);		
		
		final CyTable hiddenTable = tableFactory.createTable(suidx
				+ " hidden network", CyIdentifiable.SUID, Long.class, false, false, InitialTableSize.SMALL);
		networkTableManager.setTable(networkRef.get(), CyNetwork.class, CyNetwork.HIDDEN_ATTRS, hiddenTable);
		// Add default network columns.
		defTable.createColumn(CyNetwork.NAME, String.class, true);
		defTable.createColumn(CyNetwork.SELECTED, Boolean.class, true, Boolean.FALSE);
	}

	private void createNodeTables(long suidx, CyTableFactory tableFactory, boolean pubTables, int num, SharedTableFacade sharedNodeTable) {
		final CyTable defTable = tableFactory.createTable(suidx
				+ " default node", CyIdentifiable.SUID, Long.class, false /* all local tables are private*/, false, InitialTableSize.SMALL);
		
		networkTableManager.setTable(networkRef.get(), CyNode.class, CyNetwork.LOCAL_ATTRS, defTable);
		LocalTableFacade localTable = new LocalTableFacade(defTable,sharedNodeTable, eventHelper);
		localTable.setPublic(pubTables); //Set the privacy of facade tables based on the given flag

		networkTableManager.setTable(networkRef.get(), CyNode.class, CyNetwork.DEFAULT_ATTRS, localTable);
		if ( eventHelper instanceof TableEventHelperFacade )
			((TableEventHelperFacade)eventHelper).registerFacade(localTable);
		
		final CyTable hiddenTable = tableFactory.createTable(suidx
				+ " hidden node", CyIdentifiable.SUID, Long.class, false, false, InitialTableSize.SMALL);
		networkTableManager.setTable(networkRef.get(), CyNode.class, CyNetwork.HIDDEN_ATTRS, hiddenTable);
		
		defTable.createColumn(CyNetwork.NAME, String.class, true);
		defTable.createColumn(CyNetwork.SELECTED, Boolean.class, true, Boolean.FALSE);		
	}

	private void createEdgeTables(long suidx, CyTableFactory tableFactory, boolean pubTables, int num, SharedTableFacade sharedEdgeTable) {
		final CyTable defTable = tableFactory.createTable(suidx + " default edge", CyIdentifiable.SUID, Long.class,
				false /* all local tables are private*/, false, InitialTableSize.SMALL);
		networkTableManager.setTable(networkRef.get(), CyEdge.class, CyNetwork.LOCAL_ATTRS, defTable);

		LocalTableFacade localTable = new LocalTableFacade(defTable,sharedEdgeTable, eventHelper);
		localTable.setPublic(pubTables); //Set the privacy of facade tables based on the given flag

		networkTableManager.setTable(networkRef.get(), CyEdge.class, CyNetwork.DEFAULT_ATTRS, localTable);
		if ( eventHelper instanceof TableEventHelperFacade )
			((TableEventHelperFacade)eventHelper).registerFacade(localTable);
		
		final CyTable hiddenTable = tableFactory.createTable(suidx
				+ " hidden edge", CyIdentifiable.SUID, Long.class, false, false, InitialTableSize.SMALL);
		networkTableManager.setTable(networkRef.get(), CyEdge.class, CyNetwork.HIDDEN_ATTRS, hiddenTable);
		
		defTable.createColumn(CyNetwork.NAME, String.class, true);
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

	// This doesn't remove the SHARED_ATTRS rows?
	protected <T extends CyIdentifiable> void removeRows(Collection<T> items, Class<? extends T> type) {
		Collection<Long> primaryKeys = new ArrayList<Long>();
		for (T item : items) {
			primaryKeys.add(item.getSUID());
		}
		
		for (CyTable table : networkTableManager.getTables(networkRef.get(), type).values()) {
			table.deleteRows(primaryKeys);
		}
	}
}
