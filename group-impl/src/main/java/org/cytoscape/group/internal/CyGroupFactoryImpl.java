package org.cytoscape.group.internal;

/*
 * #%L
 * Cytoscape Groups Impl (group-impl)
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

import java.util.List;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CyGroupFactoryImpl implements CyGroupFactory {
	private final CyEventHelper help;
	private final CyGroupManagerImpl mgr;
	private final CyServiceRegistrar serviceRegistrar;

	/**
	 * Creates a new CyNetworkFactoryImpl object.
	 *
	 * @param help An instance of CyEventHelper. 
	 */
	public CyGroupFactoryImpl(final CyEventHelper help, final CyGroupManagerImpl mgr,
				    final CyServiceRegistrar serviceRegistrar)
	{
		if (help == null)
			throw new NullPointerException("CyEventHelper is null.");

		if (mgr == null)
			throw new NullPointerException("CyGroupManager is null.");

		if (serviceRegistrar == null)
			throw new NullPointerException("CyServiceRegistrar is null.");

		this.help             = help;
		this.mgr              = mgr;
		this.serviceRegistrar = serviceRegistrar;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CyGroup createGroup(CyNetwork network, boolean register) {
		return createGroup(network, null, null, null, register);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CyGroup createGroup(CyNetwork network, List<CyNode> nodes, 
	                           List<CyEdge> edges, boolean register) {
		return createGroup(network, null, nodes, edges, register);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CyGroup createGroup(CyNetwork network, CyNode node, 
	                           List<CyNode> nodes, List<CyEdge> edges, boolean register) {
		CyGroup group = new CyGroupImpl(help, mgr, network, node, nodes, edges);
		if (register)
			mgr.addGroup(group);
		return group;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CyGroup createGroup(CyNetwork network, CyNode node, boolean register) {
		CyGroup group = new CyGroupImpl(help, mgr, network, node, null, null);
		if (register)
			mgr.addGroup(group);
		return group;
	}
}
