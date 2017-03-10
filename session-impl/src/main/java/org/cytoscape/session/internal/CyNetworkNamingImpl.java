package org.cytoscape.session.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Session Impl (session-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class CyNetworkNamingImpl implements CyNetworkNaming {
	
	private static final Logger logger = LoggerFactory.getLogger(CyNetworkNamingImpl.class);
	
	private static final String DEF_NETWORK_NAME_PREFIX = "Network";
	
	private final CyServiceRegistrar serviceRegistrar;

	public CyNetworkNamingImpl(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public String getSuggestedSubnetworkTitle(final CyNetwork parentNetwork) {
		String parentName = parentNetwork.getRow(parentNetwork).get(CyNetwork.NAME, String.class);

		Pattern p = Pattern.compile(".*\\((\\d*)\\)$"); // capture just the digits
		Matcher m = p.matcher(parentName);
		int start = 0;

		if (m.matches()) {
			parentName = parentName.substring(0, m.start(1) - 1);
			start = Integer.decode(m.group(1));
		}
		
		final CyNetworkManager netManager = serviceRegistrar.getService(CyNetworkManager.class);

		for (int i = start; true; i++) {
			final String nameCandidate = parentName + "(" + (i + 1) + ")";

			if (!isNetworkTitleTaken(nameCandidate, netManager))
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

		if (m.matches()) {
			desiredTitle = desiredTitle.substring(0, m.start(1) - 1);
			String gr = m.group(1); // happens to be "" (empty str.) because of \\d*
			start = (gr.isEmpty()) ? 1 : Integer.decode(gr) + 1;
		}
		
		final CyNetworkManager netManager = serviceRegistrar.getService(CyNetworkManager.class);

		for (int i = start; true; i++) {
			final String titleCandidate = desiredTitle + ((i == 0) ? "" : ("_" + i));

			if (!isNetworkTitleTaken(titleCandidate, netManager))
				return titleCandidate;
		}
	}

	private static boolean isNetworkTitleTaken(final String titleCandidate, final CyNetworkManager netManager) {
		for (CyNetwork existingNetwork : netManager.getNetworkSet()) {
			final String name = existingNetwork.getRow(existingNetwork).get(CyNetwork.NAME, String.class);

			if (name != null) {
				if (name.equals(titleCandidate))
					return true;
			} else {
				logger.error("isNetworkTitleTaken: CyNetwork " + existingNetwork.getSUID() + " 'name' is NULL.");
			}
		}

		return false;
	}
}
