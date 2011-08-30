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

import org.cytoscape.event.CyEventHelper;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTable.Mutability;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyTableMetadata;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.TableAboutToBeDeletedEvent;
import org.cytoscape.model.events.TableAddedEvent;
import org.cytoscape.model.events.TableDeletedEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * An interface describing a factory used for managing {@link CyTable} objects.
 * This class will be provided as a service through Spring/OSGi.
 */
public class CyTableManagerImpl implements CyTableManager, NetworkAboutToBeDestroyedListener {
	private static final Logger logger = LoggerFactory.getLogger(CyTableManagerImpl.class);
	private final CyEventHelper eventHelper;
	private final Map<Class<?>, Map<CyNetwork, Map<String, CyTable>>> networkTableMap;
	private final Map<Long, CyTable> tables;

	/**
	 * Creates a new CyTableManagerImpl object.
	 *
	 * @param eventHelper  DOCUMENT ME!
	 */
	public CyTableManagerImpl(final CyEventHelper eventHelper) {
		this.eventHelper = eventHelper;

		networkTableMap = new HashMap<Class<?>, Map<CyNetwork, Map<String, CyTable>>>();
		networkTableMap.put(CyNetwork.class, new HashMap<CyNetwork, Map<String, CyTable>>());
		networkTableMap.put(CyNode.class, new HashMap<CyNetwork, Map<String, CyTable>>());
		networkTableMap.put(CyEdge.class, new HashMap<CyNetwork, Map<String, CyTable>>());

		tables = new HashMap<Long, CyTable>();
	}

	/**
	 *  DOCUMENT ME!
	 */
	@Override
	public synchronized void reset() {
		networkTableMap.clear();
		tables.clear();
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param graphObjectType DOCUMENT ME!
	 * @param network DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	@Override
	public synchronized Map<String, CyTable> getTableMap(final Class<?> graphObjectType,
	                                                     final CyNetwork network) {
		if ((network == null) || (graphObjectType == null)) {
			return null;
		}

		Map<CyNetwork, Map<String, CyTable>> tmap = networkTableMap.get(graphObjectType);

		if (tmap == null) {
			throw new IllegalArgumentException("no data tables of type: " + graphObjectType
			                                   + " exist");
		}

		return tmap.get(network);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param graphObjectType DOCUMENT ME!
	 * @param network DOCUMENT ME!
	 * @param table DOCUMENT ME!
	 */
	public void addNetworkTable(Class<?> graphObjectType, final CyNetwork network,
	                            final CyTable table) {
		if (network == null) {
			throw new NullPointerException("CyNetwork is null");
		}

		if (graphObjectType == null) {
			throw new NullPointerException("Type is null");
		}

		if (table == null) {
			throw new NullPointerException("Table is null");
		}

		if (!networkTableMap.containsKey(graphObjectType)) {
			networkTableMap.put(graphObjectType, new HashMap<CyNetwork, Map<String, CyTable>>());
		}

		Map<CyNetwork, Map<String, CyTable>> tmap = networkTableMap.get(graphObjectType);
		Map<String, CyTable> tm = new HashMap<String, CyTable>();
		tm.put(CyNetwork.DEFAULT_ATTRS, table);

		tmap.put(network, tm);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param graphObjectType DOCUMENT ME!
	 * @param network DOCUMENT ME!
	 * @param tm DOCUMENT ME!
	 */
	public synchronized void setTableMap(final Class<?> graphObjectType, final CyNetwork network,
	                                     final Map<String, CyTable> tm) {
		if (network == null) {
			throw new NullPointerException("CyNetwork is null");
		}

		if (graphObjectType == null) {
			throw new NullPointerException("Type is null");
		}

		if (!networkTableMap.containsKey(graphObjectType)) {
			networkTableMap.put(graphObjectType, new HashMap<CyNetwork, Map<String, CyTable>>());
		}

		Map<CyNetwork, Map<String, CyTable>> tmap = networkTableMap.get(graphObjectType);

		if (tm == null) {
			tmap.remove(network);
		} else {
			tmap.put(network, tm);
		}
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param t DOCUMENT ME!
	 */
	public synchronized void addTable(final CyTable t) {
		if (t == null) {
			throw new NullPointerException("added table is null");
		}

		tables.put(t.getSUID(), t);
		eventHelper.fireEvent(new TableAddedEvent(this, t));
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param includePrivate DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	@Override
	public synchronized Set<CyTableMetadata> getAllTables(final boolean includePrivate) {
		Set<CyTableMetadata> res = new HashSet<CyTableMetadata>();

		for (Long key : tables.keySet()) {
			if (includePrivate || tables.get(key).isPublic()) {
				res.add(createMetadata(tables.get(key)));
			}
		}

		return res;
	}

	private CyTableMetadata createMetadata(CyTable cyTable) {
		Class<?> entryType = null;
		String entryNamespace = null;
		Set<CyNetwork> networks = new HashSet<CyNetwork>();

		for (Entry<Class<?>, Map<CyNetwork, Map<String, CyTable>>> mapEntry : networkTableMap
		                                                                                                                                                                                                                 .entrySet()) {
			Class<?> type = mapEntry.getKey();
			Map<CyNetwork, Map<String, CyTable>> networkMap = mapEntry.getValue();

			for (Entry<CyNetwork, Map<String, CyTable>> entry : networkMap.entrySet()) {
				CyNetwork network = entry.getKey();

				for (Entry<String, CyTable> tableEntry : entry.getValue().entrySet()) {
					String namespace = tableEntry.getKey();
					CyTable table = tableEntry.getValue();

					if (table.getSUID() == cyTable.getSUID()) {
						entryType = type;
						entryNamespace = namespace;
						networks.add(network);
					}
				}
			}
		}

		if (networks.size() > 0) {
			return new CyTableMetadataImpl(entryType, cyTable, networks, entryNamespace);
		}

		return new CyTableMetadataImpl(null, cyTable, networks, null);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param suid DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
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

	private static final Class[] tableTypes = new Class[] {
	                                              CyNetwork.class, CyNode.class, CyEdge.class
	                                          };

	/**
	 *  DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	@Override
	public void handleEvent(NetworkAboutToBeDestroyedEvent e) {
		CyNetwork network = e.getNetwork();

		for (Class<?> type : tableTypes) {
			for (CyTable table : getTableMap(type, network).values()) {
				deleteTableInternal(table.getSUID(), true);
			}
		}
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param suid DOCUMENT ME!
	 */
	@Override
	public void deleteTable(long suid) {
		deleteTableInternal(suid, false);
	}
}
