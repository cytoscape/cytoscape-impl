/*
 Copyright (c) 2006, 2007, 2011, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.search.internal;


import java.awt.Component;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.AbstractToolBarComponent;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.search.internal.ui.EnhancedSearchPanel;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.RemovedNodesEvent;
import org.cytoscape.model.events.RemovedNodesListener;
import org.cytoscape.model.events.RemovedEdgesListener;
import org.cytoscape.model.events.RemovedEdgesEvent;
import org.cytoscape.model.events.RowSetRecord;
import java.util.Collection;
import java.util.Iterator;


public class EnhancedSearchPlugin extends AbstractToolBarComponent
	implements SetCurrentNetworkViewListener, NetworkAboutToBeDestroyedListener,
	           SessionLoadedListener,RowsSetListener, ColumnDeletedListener, RemovedNodesListener, RemovedEdgesListener
{
	private final EnhancedSearchManager searchMgr;
	private final EnhancedSearchPanel searchPnl; 
	static CyEventHelper eventHelper;
	public static boolean attributeChanged = false;
	
	public EnhancedSearchPlugin(final CySwingApplication desktopApp,
	                            final CyApplicationManager appManager, 
	                            final DialogTaskManager taskMgr,
	                            final CyEventHelper eventHelper, final CyNetworkViewManager viewManager)
	{
		
		searchMgr = new EnhancedSearchManager();
		// Add a text-field and a search button on tool-bar
		searchPnl = new EnhancedSearchPanel(appManager, viewManager, searchMgr, taskMgr);
		this.setToolBarGravity(9.8f);
		EnhancedSearchPlugin.eventHelper = eventHelper;
	}

	@Override	
	public Component getComponent(){
		return searchPnl;
	}
	

	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		//
	}
	
	@Override
	public void handleEvent(SessionLoadedEvent e) {
		// reset the state of the search-manager
		if (searchMgr != null){
			searchMgr.clear();			
		}	
	}

	@Override
	public void handleEvent(NetworkAboutToBeDestroyedEvent e) {
		// remove the index of network to be destroyed 
		if (searchMgr != null){
			CyNetwork network = e.getNetwork();
			searchMgr.removeNetworkIndex(network);			
		}
	}
	
	@Override	
	public void handleEvent(ColumnDeletedEvent e){
		this.attributeChanged = true;
	}
	
	@Override
	public void handleEvent(RowsSetEvent e) {
		
		Collection<RowSetRecord> records = e.getPayloadCollection();

		Iterator<RowSetRecord> it= records.iterator();
		while (it.hasNext()){
			// Ignore the change of selection attribute 
			if (!it.next().getColumn().equalsIgnoreCase("selected")){
				this.attributeChanged = true;
				break;
			}			
		}		
	}
	
	@Override	
	public void handleEvent(RemovedNodesEvent e){
		this.attributeChanged = true;
	}

	@Override	
	public void handleEvent(RemovedEdgesEvent e){
		this.attributeChanged = true;
	}
}
