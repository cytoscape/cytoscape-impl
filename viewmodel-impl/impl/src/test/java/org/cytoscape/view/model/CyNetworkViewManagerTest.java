package org.cytoscape.view.model;

/*
 * #%L
 * Cytoscape View Model Impl (viewmodel-impl)
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

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.view.model.internal.CyNetworkViewManagerImpl;
import org.junit.After;


public class CyNetworkViewManagerTest extends AbstractCyNetworkViewManagerTest {
	
	protected NetworkTestSupport netTestSupport;
	protected CyNetworkViewManager viewManager;
	
	
	public CyNetworkViewManagerTest() {
		netTestSupport = new NetworkTestSupport();
	}

	@After
	public void tearDown() throws Exception {
		viewManager = null;
	}

	@Override
	protected CyNetwork newNetwork(boolean registered) {
		CyNetwork net = netTestSupport.getNetwork();
		
		if (registered)
			resgisterNetwork(net);
		
		return net;
	}

	@Override
	protected CyNetworkViewManager getViewManager() {
		if (viewManager == null)
			viewManager = new CyNetworkViewManagerImpl(eventHelper, netManager);
		
		return viewManager;
	}
}
