/*
 Copyright (c) 2010-2011, The Cytoscape Consortium (www.cytoscape.org)

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
	public synchronized void reset() {
		tables.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void addTable(final CyTable t) {
		if (t == null)
			throw new NullPointerException("added table is null");

		tables.put(t.getSUID(), t);
		eventHelper.fireEvent(new TableAddedEvent(this, t));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized Set<CyTable> getAllTables(final boolean includePrivate) {
		final Set<CyTable> res = new HashSet<CyTable>();

		for (final Long key : tables.keySet()) {
			if (includePrivate || tables.get(key).isPublic())
				res.add(tables.get(key));
		}

		return res;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized CyTable getTable(final long suid) {
		return tables.get(suid);
	}

	void deleteTableInternal(final long suid, boolean force) {
		CyTableImpl table;

		synchronized (this) {
			table = (CyTableImpl) tables.get(suid);

			if (table == null) {
				return;
			}
		}

		eventHelper.fireEvent(new TableAboutToBeDeletedEvent(this, table));

		synchronized (this) {
			table = (CyTableImpl) tables.get(suid);

			if (table == null) {
				return;
			}

			if (!force && (table.getMutability() != Mutability.MUTABLE)) {
				throw new IllegalArgumentException("can't delete an immutable table!");
			}

			table.removeAllVirtColumns();
			tables.remove(suid);
		}

		eventHelper.fireEvent(new TableDeletedEvent(this));

		logger.debug("CyTable removed: table ID = " + table.getSUID());
		table = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteTable(long suid) {
		deleteTableInternal(suid, false);
	}
	
	@Override
	public void handleEvent(NetworkAboutToBeDestroyedEvent e) {
		// Collect set of tables to dispose
		CyNetwork network = e.getNetwork();
		Set<CyTable> tablesToDispose = new HashSet<CyTable>();
		for (Class<? extends CyIdentifiable> type : COMPATIBLE_TYPES)
			tablesToDispose.addAll(networkTableManager.getTables(network, type).values());
		
		// Exclude tables that are being referenced by other networks
		for (CyNetwork otherNetwork : networkManager.getNetworkSet()) {
			if (otherNetwork.getSUID() == network.getSUID())
				continue;
			
			for (Class<? extends CyIdentifiable> type : COMPATIBLE_TYPES)
				tablesToDispose.removeAll(networkTableManager.getTables(otherNetwork, type).values());
		}
		
		for (CyTable table : tablesToDispose)
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
