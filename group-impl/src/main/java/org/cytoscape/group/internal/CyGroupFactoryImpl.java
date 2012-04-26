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
package org.cytoscape.group.internal;

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
			throw new NullPointerException("CyEventHelper is null!");

		if (mgr == null)
			throw new NullPointerException("CyGroupManager is null!");

		if (serviceRegistrar == null)
			throw new NullPointerException("CyServiceRegistrar is null!");

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
