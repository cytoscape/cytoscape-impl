package org.cytoscape.group.internal;

import java.util.List;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

/*
 * #%L
 * Cytoscape Groups Impl (group-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2016 The Cytoscape Consortium
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

public class CyGroupFactoryImpl implements CyGroupFactory {
	
	private final CyGroupManagerImpl mgr;
	private final LockedVisualPropertiesManager lvpMgr;

	public CyGroupFactoryImpl(final CyGroupManagerImpl mgr, final LockedVisualPropertiesManager lvpMgr) {
		if (mgr == null)
			throw new NullPointerException("CyGroupManager is null.");
		if (lvpMgr == null)
			throw new NullPointerException("LockedVisualPropertiesManager is null.");

		this.mgr = mgr;
		this.lvpMgr = lvpMgr;
	}

	@Override
	public CyGroup createGroup(CyNetwork network, boolean register) {
		return createGroup(network, null, null, null, register);
	}

	@Override
	public CyGroup createGroup(CyNetwork network, List<CyNode> nodes, 
	                           List<CyEdge> edges, boolean register) {
		return createGroup(network, null, nodes, edges, register);
	}

	@Override
	public CyGroup createGroup(CyNetwork network, CyNode node, 
	                           List<CyNode> nodes, List<CyEdge> edges, boolean register) {
		CyGroup group = new CyGroupImpl(mgr.getService(CyEventHelper.class), mgr, lvpMgr, network, node, nodes, edges);
		if (register)
			mgr.addGroup(group);
		return group;
	}

	@Override
	public CyGroup createGroup(CyNetwork network, CyNode node, boolean register) {
		CyGroup group = new CyGroupImpl(mgr.getService(CyEventHelper.class), mgr, lvpMgr, network, node, null, null);
		if (register)
			mgr.addGroup(group);
		return group;
	}
}
