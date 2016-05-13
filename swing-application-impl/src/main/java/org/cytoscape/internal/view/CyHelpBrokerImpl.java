package org.cytoscape.internal.view;

import javax.help.HelpBroker;
import javax.help.HelpSet;

import org.cytoscape.application.swing.CyHelpBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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


/**
 * @deprecated JavaHelp no longer used in Cytoscape as of 3.4.
 * This class creates the Cytoscape Help Broker for managing the JavaHelp system
 * and help set access
 */
@Deprecated
public class CyHelpBrokerImpl implements CyHelpBroker {
	private HelpBroker hb;
	private HelpSet hs;
	private static final Logger logger = LoggerFactory.getLogger(CyHelpBrokerImpl.class);

	/**
	 * Creates a new CyHelpBroker object.
	 */
	public CyHelpBrokerImpl() {
		hb = null;
		hs = null;


		try {
			hs = new HelpSet();
			hb = hs.createHelpBroker();
		} catch (Exception e) {
			logger.warn("HelpSet not found.",e);
		}
	}

	/**
	 * @deprecated JavaHelp no longer used in Cytoscape as of 3.4.
	 * Returns the HelpBroker. 
	 * @return the HelpBroker. 
	 */
	@Deprecated
	public HelpBroker getHelpBroker() {
		return hb;
	}

	/**
	 * @deprecated JavaHelp no longer used in Cytoscape as of 3.4.
	 * Returns the HelpSet. 
	 * @return the HelpSet. 
	 */
	@Deprecated
	public HelpSet getHelpSet() {
		return hs;
	}
}
