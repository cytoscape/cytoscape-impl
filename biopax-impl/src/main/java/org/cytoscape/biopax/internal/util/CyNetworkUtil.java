// $Id: CyNetworkUtil.java,v 1.4 2006/06/15 22:06:02 grossb Exp $
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
package org.cytoscape.biopax.internal.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.cytoscape.model.CyNetwork;



/**
 * Utility for Creating CyNetwork Objects.
 *
 * @author Ethan Cerami
 */
public class CyNetworkUtil {
	/**
	 * Gets Network Stats, for presentation to end-user.
	 *
	 * @param network     CyNetwork Object.
	 * @return Human Readable String.
	 */
	public static String getNetworkStats(CyNetwork network) {
		NumberFormat formatter = new DecimalFormat("#,###,###");
		StringBuffer sb = new StringBuffer();

		sb.append("Successfully loaded pathway.\n\n");
		sb.append("Network contains " + formatter.format(network.getNodeCount()));
		sb.append(" nodes and " + formatter.format(network.getEdgeCount()));
		sb.append(" edges.  ");

		// TODO: Port this
//		int thresh = Integer.parseInt(CytoscapeInit.getProperties().getProperty("viewThreshold"));
//
//		if (network.getNodeCount() > thresh) {
//			sb.append("Network is over " + thresh + " nodes.  A view has not been created."
//			          + "  If you wish to view this network, use "
//			          + "\"Create View\" from the \"Edit\" menu.");
//		}

		sb.append("\n\nWhile importing data, warning messages might have been "
			          + "generated (check Cytoscape logs)\n\n");
		
		return sb.toString();
	}
}
