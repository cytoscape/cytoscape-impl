/*
 * Copyright (c) 2006, 2007, 2008, 2010, Max Planck Institute for Informatics, Saarbruecken, Germany.
 *
 * This file is part of NetworkAnalyzer.
 * 
 * NetworkAnalyzer is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 * 
 * NetworkAnalyzer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with NetworkAnalyzer. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package de.mpg.mpi_inf.bioinf.netanalyzer;


import org.cytoscape.model.CyNetwork;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.Utils;

/**
 * Base class for all NetworkAnalyzer actions which operate on a single network.
 * 
 * @author Yassen Assenov
 */
public abstract class NetAnalyzerAction extends AbstractCyAction {

	protected final CySwingApplication swingApp;
	
	/**
	 * Target network for the action.
	 */
	protected CyNetwork network;

	/**
	 * Constructs an action with the given name.
	 * 
	 * @param aName
	 *            Name of the action as it will appear in a menu.
	 */
	protected NetAnalyzerAction(final String aName, final CyApplicationManager appMgr, final CySwingApplication swingApp) {
		super(aName,appMgr);
		this.swingApp = swingApp;
		network = null;
	}

	/**
	 * Finds the network of interest to the user.
	 * <p>
	 * In case a network has been identified, the value of the field {@link #network} is updated, otherwise
	 * the value of <code>network</code> is set to the empty network or <code>null</code>. There are
	 * three possible reasons for the inability to choose a network - (1) no network is loaded; (2) there are
	 * two or more networks loaded and none selected; and (3) there is more than one network selected. For
	 * each of the two cases above, the method displays an appropriate message dialog before exiting and
	 * returning <code>false</code>.
	 * </p>
	 * 
	 * @return <code>true</code> if a network targeting analysis has been identified, <code>false</code>
	 *         otherwise.
	 */
	@SuppressWarnings("fallthrough")
	protected boolean selectNetwork() {
		network = applicationManager.getCurrentNetwork();
		if ( network == null ) {
			Utils.showErrorBox(swingApp.getJFrame(),Messages.DT_WRONGDATA, Messages.SM_LOADNET);
			return false;
		}
		return true;
	}



	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = -6263068520728141892L;
}
