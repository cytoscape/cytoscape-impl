// $Id: NetworkListener.java,v 1.13 2006/06/15 22:02:52 grossb Exp $
//------------------------------------------------------------------------------
/** Copyright (c) 2006 Memorial Sloan-Kettering Cancer Center.
 **
 ** Code written by: Ethan Cerami
 ** Authors: Ethan Cerami, Gary Bader, Chris Sander
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/
package org.cytoscape.biopax.internal.action;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.biopax.BioPaxContainer;
import org.cytoscape.biopax.MapBioPaxToCytoscape;
import org.cytoscape.biopax.MapBioPaxToCytoscapeFactory;
import org.cytoscape.biopax.NetworkListener;
import org.cytoscape.biopax.internal.view.BioPaxDetailsPanel;
import org.cytoscape.biopax.util.BioPaxUtil;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;


/**
 * Listens for Network Events, and takes appropriate Actions.
 * May be subclassed.
 *
 * @author Ethan Cerami / Benjamin Gross / Igor Rodchenkov.
 */
public class NetworkListenerImpl implements NetworkListener, NetworkViewAddedListener,
	NetworkViewAboutToBeDestroyedListener, SetCurrentNetworkViewListener, RowsSetListener {
	
	private final BioPaxDetailsPanel bpPanel;
	private final BioPaxContainer bpContainer;
	private final MapBioPaxToCytoscape mapBioPaxToCytoscape;
	private final CyNetworkViewManager viewManager;
	private final Map<CyNetworkView, RowsSetListener> listeners;

	/**
	 * Constructor.
	 *
	 * @param bpPanel BioPaxDetails Panel Object.
	 */
	public NetworkListenerImpl(BioPaxDetailsPanel bpPanel, BioPaxContainer bpContainer, 
			MapBioPaxToCytoscapeFactory mapBioPaxToCytoscapeFactory, CyNetworkViewManager viewManager) 
	{
		this.bpPanel = bpPanel;
		this.bpContainer = bpContainer;
		this.mapBioPaxToCytoscape = mapBioPaxToCytoscapeFactory.getInstance(null, null, null);
		this.viewManager = viewManager;
		this.listeners = new HashMap<CyNetworkView, RowsSetListener>();
	}

	/**
	 * Registers a newly created Network.
	 *
	 * @param cyNetwork Object.
	 */
	@Override
	public void registerNetwork(CyNetworkView view) {
		if (BioPaxUtil.isBioPAXNetwork(view.getModel())) {
			registerNodeSelectionEvents(view);
		}
	}

	/**
	 * Register to Listen for Node Selection Events
	 *
	 * @param cyNetwork CyNetwork Object.
	 */
	private void registerNodeSelectionEvents(CyNetworkView view) {
		DisplayBioPaxDetails listener = new DisplayBioPaxDetails(view, bpPanel, bpContainer, mapBioPaxToCytoscape);
		listeners.put(view, listener);
		bpPanel.resetText();
	}

	/**
	 * Property change listener - to get network/network view destroy events.
	 *
	 * @param event PropertyChangeEvent
	 */
	// TODO: Port/detangle this
//	public void propertyChange(PropertyChangeEvent event) {
//		boolean relevantEventFlag = false;
//
//		// network destroyed, we may have to remove it from our list
//		if (event.getPropertyName() == Cytoscape.NETWORK_CREATED) {
//			networkCreatedEvent(event);
//		} else if (event.getPropertyName() == Cytoscape.NETWORK_DESTROYED) {
//			networkDestroyed((String) event.getNewValue());
//			relevantEventFlag = true;
//		} else if (event.getPropertyName() == CytoscapeDesktop.NETWORK_VIEW_DESTROYED) {
//			relevantEventFlag = true;
//		} else if (event.getPropertyName() == CytoscapeDesktop.NETWORK_VIEW_CREATED) {
//			networkFocusEvent(event, false);
//		} else if (event.getPropertyName() == CytoscapeDesktop.NETWORK_VIEW_FOCUSED) {
//			networkFocusEvent(event, false);
//		} else if (event.getPropertyName() == Cytoscape.SESSION_LOADED) {
//            CySessionUtil.setSessionReadingInProgress(false);
//			networkCreatedEvent(event);
//			networkFocusEvent(event, true);
//        } else if (event.getPropertyName().equals(Integer.toString(Cytoscape.SESSION_OPENED))) {
//            CySessionUtil.setSessionReadingInProgress(true);
//        }
//
//		if (relevantEventFlag && !networkViewsRemain()) {
//			onZeroNetworkViewsRemain();
//		}
//	}

	/**
	 * Network Created Event
	 */
	@Override
	public void handleEvent(NetworkViewAddedEvent e) {	
		if(BioPaxUtil.isBioPAXNetwork(e.getNetworkView().getModel())) {
			bpContainer.showLegend();
			bpPanel.resetText();
		}
	}

	/**
	 * Network Focus Event.
	 */
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		// update bpPanel accordingly
       	if (BioPaxUtil.isBioPAXNetwork(e.getNetworkView().getModel())) {
            bpPanel.resetText();
        }
	}

	/*
	* Removes CyNetwork from our list if it has just been destroyed.
	*
	* @param networkID the ID of the CyNetwork just destroyed.
	*/
	@Override
	public void handleEvent(NetworkViewAboutToBeDestroyedEvent e) {
		if (BioPaxUtil.isBioPAXNetwork(e.getNetworkView().getModel())) {
			CyNetworkView view = e.getNetworkView();
			listeners.remove(view);
		}
	}


	@Override
	public void handleEvent(RowsSetEvent e) {
		for (RowsSetListener listener : listeners.values()) {
			listener.handleEvent(e);
		}
	}

}
