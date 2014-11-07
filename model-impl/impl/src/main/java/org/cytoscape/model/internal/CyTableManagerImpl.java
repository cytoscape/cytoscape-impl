package org.cytoscape.model.internal;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2013 The Cytoscape Consortium
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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTable.Mutability;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.TableAboutToBeDeletedEvent;
import org.cytoscape.model.events.TableAddedEvent;
import org.cytoscape.model.events.TableDeletedEvent;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An interface describing a factory used for managing {@link CyTable} objects.
 * This class will be provided as a service through Spring/OSGi.
 */
public class CyTableManagerImpl implements CyTableManager, NetworkAboutToBeDestroyedListener {
	
	private static final Logger logger = LoggerFactory.getLogger(CyTableManagerImpl.class);
	
	@SuppressWarnings("unchecked")
	private static final Class<? extends CyIdentifiable>[] COMPATIBLE_TYPES = new Class[] { CyNetwork.class,
			CyNode.class, CyEdge.class };

	private final CyEventHelper eventHelper;
	private final CyNetworkTableManager networkTableManager;
	private final CyNetworkManager networkManager;
	
	private final Map<Long, CyTable> tables;

	private final Object lock = new Object();

	public CyTableManagerImpl(final CyEventHelper eventHelper, final CyNetworkTableManager networkTableManager,
			final CyNetworkManager networkManager) {
		if(eventHelper == null)
			throw new IllegalArgumentException("eventHelper is null.");
		if(networkTableManager == null)
			throw new IllegalArgumentException("networkTableManager is null.");
		if(networkManager == null)
			throw new IllegalArgumentException("networkManager is null.");
		
		this.eventHelper = eventHelper;
		this.networkTableManager = networkTableManager;
		this.networkManager = networkManager;

		tables = new HashMap<Long, CyTable>();
	}


	@Override
	public void reset() {
		Collection<CyTable> values;
		synchronized (lock) {
			values = tables.values();
		}
		for (CyTable table : values){
			eventHelper.fireEvent(new TableAboutToBeDeletedEvent(this, table));
		}
		synchronized (lock) {
			tables.clear();
		}
	}

	@Override
	public void addTable(final CyTable t) {
		boolean fireEvent = false;
		synchronized (lock) {
			if (t == null)
				throw new NullPointerException("added table is null");
	
			final Long suid = t.getSUID();
	
			if (tables.get(suid) == null) {
				tables.put(suid, t);
				fireEvent = true;
			}
		}
		if (fireEvent) {
			eventHelper.fireEvent(new TableAddedEvent(this, t));
		}
	}

	@Override
	public Set<CyTable> getAllTables(final boolean includePrivate) {
		synchronized (lock) {
			final Set<CyTable> res = new HashSet<CyTable>();
	
			for (final Long key : tables.keySet()) {
				if (includePrivate || tables.get(key).isPublic())
					res.add(tables.get(key));
			}
	
			return res;
		}
	}

	@Override
	public CyTable getTable(final long suid) {
		synchronized (lock) {
			return tables.get(suid);
		}
	}

	void deleteTableInternal(final long suid, boolean force) {
		CyTable table;

		synchronized (lock) {
			table = tables.get(suid);

			if (table == null) {
				return;
			}
		}

		eventHelper.fireEvent(new TableAboutToBeDeletedEvent(this, table));

		synchronized (lock) {
			table = tables.get(suid);

			if (table == null) {
				return;
			}

			if (!force && (table.getMutability() != Mutability.MUTABLE)) {
				throw new IllegalArgumentException("can't delete an immutable table.");
			}

			if ( table instanceof CyTableImpl ) 
				((CyTableImpl)table).removeAllVirtColumns();
			tables.remove(suid);
		}

		eventHelper.fireEvent(new TableDeletedEvent(this));

		logger.debug("CyTable removed: table ID = " + table.getSUID());
		table = null;
	}

	@Override
	public void deleteTable(long suid) {
		deleteTableInternal(suid, false);
	}
	
	@Override
	public void handleEvent(NetworkAboutToBeDestroyedEvent e) {
		CyNetwork network = e.getNetwork();
		for (Class<? extends CyIdentifiable> type : COMPATIBLE_TYPES)
			for (CyTable table : networkTableManager.getTables(network, type).values())
				deleteTableInternal(table.getSUID(), true);
	}

	@Override
	public Set<CyTable> getGlobalTables() {
		final Set<CyTable> nonGlobalTables = new HashSet<CyTable>();
		final Set<CyTable> globalTables = new HashSet<CyTable>();
		final Set<CyNetwork> networks = networkTableManager.getNetworkSet();

		for (final CyNetwork network : networks) {
			for (final Class<? extends CyIdentifiable> type : COMPATIBLE_TYPES) {
				final Map<String, CyTable> objTables = networkTableManager.getTables(network,type);
				nonGlobalTables.addAll(objTables.values());
			}
		}
		for(final CyTable table: tables.values())
			if(nonGlobalTables.contains(table) == false)
				globalTables.add(table);
		
		return globalTables;
	}

	@Override
	public Set<CyTable> getLocalTables(final Class<? extends CyIdentifiable> type) {
		final Set<CyTable> localTables = new HashSet<CyTable>();

		final Set<CyNetwork> networks = networkManager.getNetworkSet();

		for (final CyNetwork network : networks) {
			final Map<String, CyTable> objTables = networkTableManager.getTables(network, type);
			if (network instanceof CySubNetwork) {
				final CyTable shared = networkTableManager.getTable(((CySubNetwork) network).getRootNetwork(), type,
						CyRootNetwork.SHARED_ATTRS);
				localTables.add(shared);
			}
			localTables.addAll(objTables.values());
		}

		return localTables;
	}
}
