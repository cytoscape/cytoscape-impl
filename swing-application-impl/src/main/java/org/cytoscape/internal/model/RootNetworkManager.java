package org.cytoscape.internal.model;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeLoadedEvent;
import org.cytoscape.session.events.SessionAboutToBeLoadedListener;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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
 * This manager provides {@link CyRootNetwork} instances that are currently selected to other
 * classes in this module. Cytoscape does not keep a global reference to the current or selected root-networks 
 * (see {@link CyApplicationManager}), since a {@link CyRootNetwork} is just a "meta-network"
 * (or a "Collection" of sub-networks), and is not visualized by end users.
 */
public class RootNetworkManager implements NetworkAboutToBeDestroyedListener, SessionAboutToBeLoadedListener {

	private final Collection<CyRootNetwork> selectedRootNetworks = new LinkedHashSet<>();
	
	private final Object lock = new Object();
	private final CyServiceRegistrar serviceRegistrar;
	
	public RootNetworkManager(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	public Collection<CyRootNetwork> getSelectedRootNetworks() {
		return new LinkedHashSet<>(selectedRootNetworks);
	}
	
	public void setSelectedRootNetworks(Collection<CyRootNetwork> rootNetworks) {
		synchronized (lock) {
			selectedRootNetworks.clear();
			
			if (rootNetworks != null && !rootNetworks.isEmpty())
				selectedRootNetworks.addAll(rootNetworks);
		}
	}

	@Override
	public void handleEvent(NetworkAboutToBeDestroyedEvent evt) {
		CyNetwork net = evt.getNetwork();
		CyRootNetwork rootNet = serviceRegistrar.getService(CyRootNetworkManager.class).getRootNetwork(net);
		
		synchronized (lock) {
			if (!selectedRootNetworks.contains(rootNet))
				return;
		}
		
		CyNetworkManager netManager = serviceRegistrar.getService(CyNetworkManager.class);
		
		for (CySubNetwork sn : rootNet.getSubNetworkList()) {
			if (!sn.getSUID().equals(net.getSUID()) && netManager.networkExists(sn.getSUID()))
				return; // This root-network still has another registered subnetwork!
		}
		
		synchronized (lock) {
			// We can now remove this root-network from the selection set
			// because it will have no other subnetworks after the subnetwork from the event is destroyed,
			// which means the root-network itself will eventually be disposed as well...
			selectedRootNetworks.remove(rootNet);
		}
	}

	@Override
	public void handleEvent(SessionAboutToBeLoadedEvent evt) {
		synchronized (lock) {
			selectedRootNetworks.clear();
		}
	}
}
