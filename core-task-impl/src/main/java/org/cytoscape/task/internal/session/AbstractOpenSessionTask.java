package org.cytoscape.task.internal.session;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.group.CyGroupManager;
import org.cytoscape.io.read.CySessionReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySession;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

public abstract class AbstractOpenSessionTask extends AbstractTask {

	protected CySessionReader reader;
	protected final CyServiceRegistrar serviceRegistrar;
	
	protected AbstractOpenSessionTask(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public void cancel() {
		super.cancel();
			
		if (reader != null)
			reader.cancel(); // Remember to cancel the Session Reader!
	}
	
	protected void disposeCancelledSession(Exception e, CySessionManager sessionManager) {
		if (reader == null)
			return;
		
		final CySession newSession = reader.getSession();
		
		if (newSession != null) {
			// Destroy any new views and networks
			for (CyNetworkView view : newSession.getNetworkViews())
				view.dispose();
			
			CyRootNetworkManager rootNetManager = serviceRegistrar.getService(CyRootNetworkManager.class);
			final Set<CyRootNetwork> rootNetworks = new HashSet<>();
			
			for (CyNetwork net : newSession.getNetworks())
				rootNetworks.add(rootNetManager.getRootNetwork(net));
			
			for (CyRootNetwork rootNet : rootNetworks)
				rootNet.dispose();
		}
		
		// Reset the Group Manager, because groups can be registered by the reader
		// TODO Remove this after groups no longer registered inside the IO bundle.
		serviceRegistrar.getService(CyGroupManager.class).reset();
		
		// Destroy any global tables registered by the reader
		serviceRegistrar.getService(CyTableManager.class).reset();
		
		// Set a new, empty session
		sessionManager.setCurrentSession(null, null);
	}
}
