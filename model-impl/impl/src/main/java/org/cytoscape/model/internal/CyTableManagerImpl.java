package org.cytoscape.model.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.equations.EquationUtil;
import org.cytoscape.equations.event.EquationFunctionAddedEvent;
import org.cytoscape.equations.event.EquationFunctionAddedListener;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTable.Mutability;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.TableAboutToBeDeletedEvent;
import org.cytoscape.model.events.TableAddedEvent;
import org.cytoscape.model.events.TableDeletedEvent;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

/**
 * An interface describing a factory used for managing {@link CyTable} objects.
 * This class will be provided as a service through Spring/OSGi.
 */
public class CyTableManagerImpl implements CyTableManager, NetworkAboutToBeDestroyedListener, EquationFunctionAddedListener {
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	@SuppressWarnings("unchecked")
	private static final Class<? extends CyIdentifiable>[] COMPATIBLE_TYPES = new Class[] { CyNetwork.class,
			CyNode.class, CyEdge.class };

	private final CyNetworkTableManager networkTableManager;
	private final CyNetworkManager networkManager;
	private final CyServiceRegistrar serviceRegistrar;
	
	private final Map<Long, CyTable> tables;

	private final Object lock = new Object();

	public CyTableManagerImpl(
			CyNetworkTableManager networkTableManager,
			CyNetworkManager networkManager,
			CyServiceRegistrar serviceRegistrar
	) {
		if (networkTableManager == null)
			throw new IllegalArgumentException("networkTableManager must not be null.");
		if (networkManager == null)
			throw new IllegalArgumentException("networkManager must not be null.");
		if (serviceRegistrar == null)
			throw new IllegalArgumentException("serviceRegistrar must not be null.");

		this.networkTableManager = networkTableManager;
		this.networkManager = networkManager;
		this.serviceRegistrar = serviceRegistrar;

		tables = new HashMap<>();
	}

	@Override
	public void reset() {
		Collection<CyTable> values;

		synchronized (lock) {
			values = tables.values();
		}
		
		var eventHelper = serviceRegistrar.getService(CyEventHelper.class);

		for (var table : values) {
			eventHelper.fireEvent(new TableAboutToBeDeletedEvent(this, table));
		}

		synchronized (lock) {
			tables.clear();
		}
	}

	@Override
	public void addTable(CyTable t) {
		boolean fireEvent = false;
		
		synchronized (lock) {
			if (t == null)
				throw new NullPointerException("added table is null");

			var suid = t.getSUID();

			if (tables.get(suid) == null) {
				tables.put(suid, t);
				fireEvent = true;
			}
		}
		
		if (fireEvent) {
			var eventHelper = serviceRegistrar.getService(CyEventHelper.class);
			eventHelper.fireEvent(new TableAddedEvent(this, t));
		}
	}

	@Override
	public Set<CyTable> getAllTables(boolean includePrivate) {
		synchronized (lock) {
			var res = new HashSet<CyTable>();
	
			for (var key : tables.keySet()) {
				if (includePrivate || tables.get(key).isPublic())
					res.add(tables.get(key));
			}
	
			return res;
		}
	}

	@Override
	public CyTable getTable(long suid) {
		synchronized (lock) {
			return tables.get(suid);
		}
	}

	void deleteTableInternal(long suid, boolean force) {
		CyTable table = null;

		synchronized (lock) {
			table = tables.get(suid);

			if (table == null) {
				return;
			}
		}

		var eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		eventHelper.fireEvent(new TableAboutToBeDeletedEvent(this, table));

		synchronized (lock) {
			table = tables.get(suid);

			if (table == null) {
				return;
			}

			if (!force && (table.getMutability() != Mutability.MUTABLE)) {
				throw new IllegalArgumentException("can't delete an immutable table.");
			}

			if (table instanceof CyTableImpl)
				((CyTableImpl) table).removeAllVirtColumns();

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
		var network = e.getNetwork();
		
		for (var type : COMPATIBLE_TYPES)
			for (var table : networkTableManager.getTables(network, type).values())
				deleteTableInternal(table.getSUID(), true);
	}
	
	@Override
	public void handleEvent(EquationFunctionAddedEvent e) {
		refreshTableEquations();
	}

	@Override
	public Set<CyTable> getGlobalTables() {
		var nonGlobalTables = new HashSet<CyTable>();
		var globalTables = new HashSet<CyTable>();
		var networks = networkTableManager.getNetworkSet();

		for (var network : networks) {
			for (var type : COMPATIBLE_TYPES) {
				var objTables = networkTableManager.getTables(network, type);
				nonGlobalTables.addAll(objTables.values());
			}
		}

		synchronized (lock) {
			for (var table : tables.values())
				if (nonGlobalTables.contains(table) == false)
					globalTables.add(table);
		}
		
		return globalTables;
	}

	@Override
	public Set<CyTable> getLocalTables(Class<? extends CyIdentifiable> type) {
		var localTables = new HashSet<CyTable>();
		var networks = networkManager.getNetworkSet();

		for (var network : networks) {
			var objTables = networkTableManager.getTables(network, type);
			
			if (network instanceof CySubNetwork) {
				var shared = networkTableManager.getTable(((CySubNetwork) network).getRootNetwork(), type,
						CyRootNetwork.SHARED_ATTRS);
				localTables.add(shared);
			}
			
			localTables.addAll(objTables.values());
		}

		return localTables;
	}

	private void refreshTableEquations() {
		var compiler = serviceRegistrar.getService(EquationCompiler.class);
		var tables = getAllTables(true);

		for (var table : tables) {
			EquationUtil.refreshEquations(table, compiler);
		}
	}
}
