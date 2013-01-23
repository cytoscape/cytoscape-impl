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


import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.service.util.CyServiceRegistrar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CyNetworkFactoryImpl implements CyNetworkFactory {
	private static final Logger logger = LoggerFactory.getLogger(CyNetworkFactoryImpl.class);
	
	private final CyEventHelper help;
	private final CyTableManagerImpl mgr;
	private final CyNetworkTableManager networkTableMgr;
	private final CyTableFactory tableFactory;
	private final CyServiceRegistrar serviceRegistrar;

	/**
	 * Creates a new CyNetworkFactoryImpl object.
	 *
	 * @param help An instance of CyEventHelper. 
	 */
	public CyNetworkFactoryImpl(final CyEventHelper help,
								final CyTableManagerImpl mgr,
								final CyNetworkTableManager networkTableMgr,
								final CyTableFactory tableFactory,
								final CyServiceRegistrar serviceRegistrar) {
		if (help == null)
			throw new NullPointerException("CyEventHelper is null.");

		if (mgr == null)
			throw new NullPointerException("CyTableManager is null.");

		if (tableFactory == null)
			throw new NullPointerException("CyTableFactory is null.");

		if (serviceRegistrar == null)
			throw new NullPointerException("CyServiceRegistrar is null.");

		this.help             = help;
		this.mgr              = mgr;
		this.networkTableMgr  = networkTableMgr;
		this.tableFactory     = tableFactory;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public CyNetwork createNetwork() {
		return createNetwork(SavePolicy.SESSION_FILE); 
	}
	
	@Override
	public CyNetwork createNetwork(final SavePolicy policy) {
		final CyRootNetworkImpl net = new CyRootNetworkImpl(help, mgr, networkTableMgr, tableFactory, serviceRegistrar,
				true, policy);
		logger.info("CyNetwork w/ public tables created: ID = " +  net.getSUID());
		logger.info("CyNetwork w/ public tables created: Base Graph ID = " +  net.getBaseNetwork().getSUID());
		
		return net.getBaseNetwork(); 
	}

	@Override
	public CyNetwork createNetworkWithPrivateTables() {
		return createNetworkWithPrivateTables(SavePolicy.SESSION_FILE); 
	}

	@Override
	public CyNetwork createNetworkWithPrivateTables(final SavePolicy policy) {
		CyRootNetworkImpl net = new CyRootNetworkImpl(help, mgr, networkTableMgr, tableFactory, serviceRegistrar,
				false, policy);
		logger.info("CyNetwork w/ private tables created: ID = " + net.getSUID());
		logger.info("CyNetwork w/ private tables created: Base Graph ID = " + net.getBaseNetwork().getSUID());

		return net.getBaseNetwork();
	}
}
