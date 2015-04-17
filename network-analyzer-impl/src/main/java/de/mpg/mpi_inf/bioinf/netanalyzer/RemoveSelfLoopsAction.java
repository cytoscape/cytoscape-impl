package de.mpg.mpi_inf.bioinf.netanalyzer;

/*
 * #%L
 * Cytoscape NetworkAnalyzer Impl (network-analyzer-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013
 *   Max Planck Institute for Informatics, Saarbruecken, Germany
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

import java.awt.Frame;
import java.awt.event.ActionEvent;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.sconnect.HelpConnector;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.NetModificationDialog;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.Utils;

/**
 * Action handler for the menu item &quot;Remove Self-loops&quot;.
 * 
 * @author Yassen Assenov
 * @author Sven-Eric Schelhorn
 */
public class RemoveSelfLoopsAction extends NetAnalyzerAction {

	private static final long serialVersionUID = -7465036491341908005L;
	private static final Logger logger = LoggerFactory.getLogger(RemoveSelfLoopsAction.class);
	
	private final CyNetworkManager netMgr;

	/**
	 * Initializes a new instance of <code>RemoveSelfLoopsAction</code>.
	 */
	public RemoveSelfLoopsAction(CyApplicationManager appMgr,CySwingApplication swingApp, CyNetworkManager netMgr) {
		super(Messages.AC_REMSELFLOOPS,appMgr,swingApp);
		setPreferredMenu("Edit");
		setMenuGravity(4.2f);
		this.netMgr = netMgr;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			if (!selectNetwork()) {
				return;
			}

			final Frame desktop = swingApp.getJFrame();
			final String helpURL = HelpConnector.getRemSelfloopsURL();
			final NetModificationDialog d = new NetModificationDialog(desktop, Messages.DT_REMSELFLOOPS,
					Messages.DI_REMOVESL, helpURL, netMgr);
			d.setVisible(true);

			// Remove the self-loops from all networks selected by the user
			final CyNetwork[] networks = d.getSelectedNetworks();
			
			if (networks != null) {
				final int size = networks.length;
				int[] removedLoops = new int[size];
				String[] networkNames = new String[size];
				
				for (int i = 0; i < size; ++i) {
					final CyNetwork currentNet = networks[i];
					networkNames[i] = currentNet.getRow(currentNet).get("name",String.class);
					removedLoops[i] = CyNetworkUtils.removeSelfLoops(currentNet);
				}

				final String r = Messages.constructReport(removedLoops, Messages.SM_REMSELFLOOPS, networkNames);
				Utils.showInfoBox(desktop, Messages.DT_REMSELFLOOPS, r);
			}
		} catch (InnerException ex) {
			// NetworkAnalyzer internal error
			logger.error(Messages.SM_LOGERROR, ex);
		}
	}
}
