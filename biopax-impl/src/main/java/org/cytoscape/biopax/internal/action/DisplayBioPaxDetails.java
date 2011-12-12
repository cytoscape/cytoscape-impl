// $Id: DisplayBioPaxDetails.java,v 1.10 2006/07/21 17:05:50 grossb Exp $
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


import org.cytoscape.biopax.BioPaxContainer;
import org.cytoscape.biopax.MapBioPaxToCytoscape;
import org.cytoscape.biopax.internal.view.BioPaxDetailsPanel;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.view.model.CyNetworkView;


/**
 * Displays BioPAX Details when user clicks on a Node.
 * <p/>
 * When a user selects a node, the specified BioPaxDetailsPanel Object
 * will display the node details.  Additionally, if the BioPaxDetailsPanel
 * Object is embedded inside a CytoPanel, its tab is automatically
 * made active.
 *
 * @author Ethan Cerami
 */
public class DisplayBioPaxDetails implements RowsSetListener {
	private BioPaxDetailsPanel bpPanel;
	private BioPaxContainer bpContainer;
	private CyNetworkView view;
	private MapBioPaxToCytoscape mapBioPaxToCytoscape;

	/**
	 * Constructor.
	 *
	 * @param bpPanel BioPaxDetailsPanel Object that will actually display
	 *                the BioPax details.
	 */
	public DisplayBioPaxDetails(CyNetworkView view, BioPaxDetailsPanel bpPanel, BioPaxContainer bpContainer, MapBioPaxToCytoscape mapBioPaxToCytoscape) {
		this.bpPanel = bpPanel;
		this.bpContainer = bpContainer;
		this.view = view;
		this.mapBioPaxToCytoscape = mapBioPaxToCytoscape;
	}

	@Override
	public void handleEvent(RowsSetEvent e) {
		
		if(!view.getModel().getDefaultNodeTable().equals(e.getSource()))
			return;
		
		try {
			CyNode selected = null;
			CyNetwork network = view.getModel();
			for (CyNode node : network.getNodeList()) {
				if (network.getRow(node).get(CyNetwork.SELECTED, Boolean.class)) {
					selected = node;
					break;
				}
			}
			
			if (selected != null) {
				//  Show the details
				bpPanel.showDetails(network, selected);
            	//  If legend is showing, show details
            	bpContainer.showDetails();
			}
		} finally {
			// update custom nodes
			mapBioPaxToCytoscape.customNodes(view);
		}
	}

}
