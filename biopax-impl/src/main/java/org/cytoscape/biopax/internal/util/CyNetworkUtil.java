package org.cytoscape.biopax.internal.util;

/*
 * #%L
 * Cytoscape BioPAX Impl (biopax-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013
 *   Memorial Sloan-Kettering Cancer Center
 *   The Cytoscape Consortium
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
