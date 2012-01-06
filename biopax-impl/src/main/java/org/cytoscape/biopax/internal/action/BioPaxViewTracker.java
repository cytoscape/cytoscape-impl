// $Id: BioPaxViewTracker.java,v 1.13 2006/06/15 22:02:52 grossb Exp $
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

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.biopax.internal.BioPaxMapper;
import org.cytoscape.biopax.internal.view.BioPaxContainer;
import org.cytoscape.biopax.internal.util.BioPaxUtil;
import org.cytoscape.biopax.internal.view.BioPaxDetailsPanel;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.view.model.CyNetworkView;
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
public class BioPaxViewTracker implements NetworkViewAddedListener,
	NetworkViewAboutToBeDestroyedListener, SetCurrentNetworkViewListener, RowsSetListener {
	
	private final BioPaxDetailsPanel bpPanel;
	private final BioPaxContainer bpContainer;
	private final CyApplicationManager cyApplicationManager;

	/**
	 * Constructor.
	 *
	 * @param bpPanel BioPaxDetails Panel Object.
	 */
	public BioPaxViewTracker(BioPaxDetailsPanel bpPanel, BioPaxContainer bpContainer, CyApplicationManager cyApplicationManager) 
	{
		this.bpPanel = bpPanel;
		this.bpContainer = bpContainer;
		this.cyApplicationManager = cyApplicationManager;
	}


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
		CyNetworkView view = e.getNetworkView();
		
		// update bpPanel accordingly
       	if (view != null && BioPaxUtil.isBioPAXNetwork(view.getModel())) {
            bpPanel.resetText();
        }
	}


	@Override
	public void handleEvent(NetworkViewAboutToBeDestroyedEvent e) {
		if (BioPaxUtil.isBioPAXNetwork(e.getNetworkView().getModel())) {
			//TODO nothing?
		}
	}


	@Override
	public void handleEvent(RowsSetEvent e) {
		CyNetworkView view = cyApplicationManager.getCurrentNetworkView();
		if(view == null) return;
		
		CyNetwork network = view.getModel();
		if (BioPaxUtil.isBioPAXNetwork(network)) {

			if (!network.getDefaultNodeTable().equals(e.getSource()))
				return;

			try {
				CyNode selected = null;
				for (CyNode node : network.getNodeList()) {
					if (network.getRow(node).get(CyNetwork.SELECTED, Boolean.class)) {
						selected = node;
						break;
					}
				}

				if (selected != null) {
					// Show the details
					bpPanel.showDetails(network, selected);
					// If legend is showing, show details
					bpContainer.showDetails();
				}
			} finally {
				// update custom nodes
				BioPaxMapper.customNodes(view);
			}
		}
	}

}
