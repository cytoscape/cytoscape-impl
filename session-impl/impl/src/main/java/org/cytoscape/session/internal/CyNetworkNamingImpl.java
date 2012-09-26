/*
  File: CyNetworkNaming.java

  Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.session.internal;


import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.session.CyNetworkNaming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CyNetworkNamingImpl implements CyNetworkNaming {
	
	private static final Logger logger = LoggerFactory.getLogger(CyNetworkNamingImpl.class);
	
	private static final String DEF_NETWORK_NAME_PREFIX = "Network";
	
	private final CyNetworkManager networkManager;

	public CyNetworkNamingImpl(final CyNetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	
	@Override
	public String getSuggestedSubnetworkTitle(final CyNetwork parentNetwork) {
                String parentName = parentNetwork.getRow(parentNetwork).get(CyNetwork.NAME, String.class);

                Pattern p = Pattern.compile(".*\\((\\d*)\\)$"); //capture just the digits
                Matcher m = p.matcher(parentName);
                int start = 0;

                if (m.matches()){
                        parentName = parentName.substring(0, m.start(1)-1);
                        start = Integer.decode(m.group(1));
                }

                for (int i = start; true; i++) {
			final String nameCandidate = parentName + "(" + (i+1) + ")";

			if (!isNetworkTitleTaken(nameCandidate))
				return nameCandidate;
		}
	}

	
	@Override
	public String getSuggestedNetworkTitle(String desiredTitle) {
		if (desiredTitle == null || "".equals(desiredTitle.trim())) {
			desiredTitle = DEF_NETWORK_NAME_PREFIX;
			logger.warn("getSuggestedNetworkTitle: desiredTitle " + "was '" + desiredTitle + "'");
		}
		
		Pattern p = Pattern.compile(".*_(\\d*)$"); //capture just the digits
		Matcher m = p.matcher(desiredTitle);
		int start = 0;

		if (m.matches()){
			desiredTitle = desiredTitle.substring(0, m.start(1)-1);
			start = Integer.decode(m.group(1)) + 1;
		}

		for (int i = start; true; i++) {
			final String titleCandidate = desiredTitle + ((i == 0) ? "" : ("_" + i ));

			if (!isNetworkTitleTaken(titleCandidate))
				return titleCandidate;
		}
	}

	private boolean isNetworkTitleTaken(final String titleCandidate) {
		for (CyNetwork existingNetwork : networkManager.getNetworkSet() ) {
			final String name = existingNetwork.getRow(existingNetwork).get(CyNetwork.NAME, String.class);
			if(name != null) {
				if (name.equals(titleCandidate))
					return true;
			} else {
				logger.error("isNetworkTitleTaken: CyNetwork " 
					+ existingNetwork.getSUID() + " 'name' is NULL.");
			}
		}

		return false;
	}
}
