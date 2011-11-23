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

import java.awt.Frame;
import java.awt.event.ActionEvent;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.ClearMultEdgesDialog;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.Utils;

/**
 * Action handler for the menu item &quot;Remove Duplicated Edges&quot;.
 * 
 * @author Yassen Assenov
 */
public class RemDupEdgesAction extends NetAnalyzerAction {

	private static final Logger logger = LoggerFactory.getLogger(RemDupEdgesAction.class);
	private final CyNetworkManager netMgr;

	/**
	 * Initializes a new instance of <code>ReDupEdgesAction</code>.
	 */
	public RemDupEdgesAction(CyApplicationManager appMgr,CySwingApplication swingApp,CyNetworkManager netMgr) {
		super(Messages.AC_REMDUPEDGES,appMgr,swingApp);
		setPreferredMenu(NetworkAnalyzer.PARENT_MENU + Messages.AC_MENU_MODIFICATION);
		this.netMgr = netMgr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cytoscape.util.CytoscapeAction#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			if (!selectNetwork()) {
				return;
			}

			final Frame desktop = swingApp.getJFrame();
			final ClearMultEdgesDialog d = new ClearMultEdgesDialog(desktop,netMgr);
			d.setVisible(true);

			// Clear duplicated edges from all networks selected by the user
			final CyNetwork[] networks = d.getSelectedNetworks();
			final boolean ignoreDir = d.getIgnoreDirection();
			final boolean createEdgeAttr = d.getCreateEdgeAttr();
			if (networks != null) {
				final int size = networks.length;
				int[] removedEdges = new int[size];
				String[] networkNames = new String[size];
				for (int i = 0; i < size; ++i) {
					final CyNetwork currentNet = networks[i];
					networkNames[i] = currentNet.getCyRow().get("name",String.class);
					removedEdges[i] = CyNetworkUtils.removeDuplEdges(currentNet, ignoreDir, createEdgeAttr);
				}

				final String r = Messages
						.constructReport(removedEdges, Messages.SM_REMDUPEDGES, networkNames);
				Utils.showInfoBox(desktop, Messages.DT_REMDUPEDGES, r);
			}
		} catch (InnerException ex) {
			// NetworkAnalyzer internal error
			logger.error(Messages.SM_LOGERROR, ex);
		}
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = 3389808503156586737L;
}
