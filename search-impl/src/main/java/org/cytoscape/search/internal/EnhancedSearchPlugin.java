package org.cytoscape.search.internal;

/*
 * #%L
 * Cytoscape Search Impl (search-impl)
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

import java.awt.Component;
import java.util.Collection;
import java.util.Iterator;

import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.AbstractToolBarComponent;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.RemovedEdgesEvent;
import org.cytoscape.model.events.RemovedEdgesListener;
import org.cytoscape.model.events.RemovedNodesEvent;
import org.cytoscape.model.events.RemovedNodesListener;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.search.internal.ui.EnhancedSearchPanel;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;


public class EnhancedSearchPlugin extends AbstractToolBarComponent
	implements SetCurrentNetworkViewListener, NetworkAboutToBeDestroyedListener,
	           SessionLoadedListener,RowsSetListener, ColumnDeletedListener, RemovedNodesListener,
	           RemovedEdgesListener {
	
	private final EnhancedSearchManager searchMgr;
	private final EnhancedSearchPanel searchPnl; 
	
	public static boolean attributeChanged;
	
	public EnhancedSearchPlugin(final CyServiceRegistrar serviceRegistrar) {
		searchMgr = new EnhancedSearchManager();
		
		// Add a text-field and a search button on tool-bar
		searchPnl = new EnhancedSearchPanel(searchMgr, serviceRegistrar);
		setToolBarGravity(9.8f);
	}

	@Override
	public Component getComponent() {
		return searchPnl;
	}

	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		//
	}

	@Override
	public void handleEvent(SessionLoadedEvent e) {
		// reset the state of the search-manager
		if (searchMgr != null) {
			searchMgr.clear();
		}
	}

	@Override
	public void handleEvent(NetworkAboutToBeDestroyedEvent e) {
		// remove the index of network to be destroyed
		if (searchMgr != null) {
			CyNetwork network = e.getNetwork();
			searchMgr.removeNetworkIndex(network);
		}
	}

	@Override
	public void handleEvent(ColumnDeletedEvent e) {
		attributeChanged = true;
	}
	
	@Override
	public void handleEvent(RowsSetEvent e) {
		Collection<RowSetRecord> records = e.getPayloadCollection();
		Iterator<RowSetRecord> it = records.iterator();
		
		while (it.hasNext()) {
			// Ignore the change of selection attribute
			if (!it.next().getColumn().equalsIgnoreCase("selected")) {
				attributeChanged = true;
				break;
			}
		}		
	}

	@Override
	public void handleEvent(RemovedNodesEvent e) {
		attributeChanged = true;
	}

	@Override
	public void handleEvent(RemovedEdgesEvent e) {
		attributeChanged = true;
	}
}
