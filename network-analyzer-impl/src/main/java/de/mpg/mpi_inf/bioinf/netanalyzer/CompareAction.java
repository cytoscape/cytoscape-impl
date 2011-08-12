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

import java.awt.event.ActionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetworkManager;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.CompareDialog;

/**
 * Action handler for the menu item &quot;Compare Two Networks&quot;.
 * 
 * @author Yassen Assenov
 */
public class CompareAction extends NetAnalyzerAction {

	private static final Logger logger = LoggerFactory.getLogger(CompareAction.class);
	private final CyNetworkManager netMgr;

	/**
	 * Initializes a new instance of <code>GOPTRunAlgorithm</code>.
	 */
	public CompareAction(CyApplicationManager appMgr,CySwingApplication swingApp, CyNetworkManager netMgr) {
		super(Messages.AC_COMPARE,appMgr,swingApp);
		setPreferredMenu("Plugins." + Messages.AC_MENU_MODIFICATION);
		this.netMgr = netMgr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		try {
			CompareDialog d = new CompareDialog(swingApp.getJFrame(), netMgr);
			d.setVisible(true);
		} catch (InnerException ex) {
			// NetworkAnalyzer internal error
			logger.error(Messages.SM_LOGERROR, ex);
		}
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = -8249265620304925132L;
}
