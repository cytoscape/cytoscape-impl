package org.cytoscape.group;

/*
 * #%L
 * Cytoscape Groups Impl (group-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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
import org.cytoscape.group.internal.CyGroupFactoryImpl;
import org.cytoscape.group.internal.CyGroupManagerImpl;
import org.cytoscape.group.internal.LockedVisualPropertiesManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;

import static org.mockito.Mockito.*;

public class GroupTestSupport {

	protected CyGroupFactory groupFactory;
	protected CyGroupManagerImpl groupManager;
	
	public GroupTestSupport() {
		final CyEventHelper help = mock(CyEventHelper.class);
		
		final CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		final VisualMappingManager vmMgr = mock(VisualMappingManager.class);
		when(serviceRegistrar.getService(VisualMappingManager.class)).thenReturn(vmMgr);
		final CyNetworkViewManager netViewMgr = mock(CyNetworkViewManager.class);
		when(serviceRegistrar.getService(CyNetworkViewManager.class)).thenReturn(netViewMgr);
		
		final LockedVisualPropertiesManager lvpMgr = new LockedVisualPropertiesManager(serviceRegistrar);
		
		groupManager = new CyGroupManagerImpl(serviceRegistrar, help);
		groupFactory = new CyGroupFactoryImpl(groupManager, lvpMgr, help);
	}

	public CyGroupFactory getGroupFactory() {
		return groupFactory;
	}

	public CyGroupManagerImpl getGroupManager() {
		return groupManager;
	}
}
