/*
 File: CyHelpBroker.java

 Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

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
package org.cytoscape.internal.view;

import javax.help.HelpBroker;
import javax.help.HelpSet;
import java.net.URL;

import org.cytoscape.application.swing.CyHelpBroker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class creates the Cytoscape Help Broker for managing the JavaHelp system
 * and help set access
 */
public class CyHelpBrokerImpl implements CyHelpBroker {
	private HelpBroker hb;
	private HelpSet hs;
	private static final String HELP_RESOURCE = "/cytoscape/help/jhelpset.hs";
	private static final Logger logger = LoggerFactory.getLogger(CyHelpBrokerImpl.class);

	/**
	 * Creates a new CyHelpBroker object.
	 */
	public CyHelpBrokerImpl() {
		hb = null;
		hs = null;

		URL hsURL = getClass().getResource(HELP_RESOURCE);

		try {
			hs = new HelpSet(null, hsURL);
			hb = hs.createHelpBroker();
		} catch (Exception e) {
			logger.warn("HelpSet not found!",e);
		}
	}

	/**
	 * Returns the HelpBroker. 
	 *
	 * @return the HelpBroker. 
	 */
	public HelpBroker getHelpBroker() {
		return hb;
	}

	/**
	 * Returns the HelpSet. 
	 *
	 * @return the HelpSet. 
	 */
	public HelpSet getHelpSet() {
		return hs;
	}
}
