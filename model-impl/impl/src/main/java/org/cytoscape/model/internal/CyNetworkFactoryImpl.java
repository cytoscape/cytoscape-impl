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


import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableFactory;
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
	public CyNetworkFactoryImpl(final CyEventHelper help, final CyTableManagerImpl mgr,
					final CyNetworkTableManager networkTableMgr,
				    final CyTableFactory tableFactory,
				    final CyServiceRegistrar serviceRegistrar)
	{
		if (help == null)
			throw new NullPointerException("CyEventHelper is null!");

		if (mgr == null)
			throw new NullPointerException("CyTableManager is null!");

		if (tableFactory == null)
			throw new NullPointerException("CyTableFactory is null!");

		if (serviceRegistrar == null)
			throw new NullPointerException("CyServiceRegistrar is null!");

		this.help             = help;
		this.mgr              = mgr;
		this.networkTableMgr  = networkTableMgr;
		this.tableFactory     = tableFactory;
		this.serviceRegistrar = serviceRegistrar;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CyNetwork createNetwork() {
		final CyRootNetworkImpl net = new CyRootNetworkImpl(help, mgr, networkTableMgr, tableFactory, serviceRegistrar, true);
		logger.info("CyNetwork w/ public tables created: ID = " +  net.getSUID());
		logger.info("CyNetwork w/ public tables created: Base Graph ID = " +  net.getBaseNetwork().getSUID());
		return net.getBaseNetwork(); 
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CyNetwork createNetworkWithPrivateTables() {
		CyRootNetworkImpl net = new CyRootNetworkImpl(help, mgr, networkTableMgr, tableFactory, serviceRegistrar, false);
		logger.info("CyNetwork w/ private tables created: ID = " +  net.getSUID());
		logger.info("CyNetwork w/ private tables created: Base Graph ID = " +  net.getBaseNetwork().getSUID());
		return net.getBaseNetwork(); 
	}
}
