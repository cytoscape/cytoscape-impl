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
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkInspection;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkInterpretation;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkStatus;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.InterpretationDialog;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.Utils;

/**
 * Action handler for the menu item &quot;Analyze Network&quot;.
 * 
 * @author Yassen Assenov
 */
public class AnalyzeNetworkAction extends NetAnalyzerAction {

	private static final Logger logger = LoggerFactory.getLogger(AnalyzeNetworkAction.class);

	/**
	 * Initializes a new instance of <code>AnalyzeNetworkAction</code>.
	 */
	public AnalyzeNetworkAction(CyApplicationManager appMgr,CySwingApplication swingApp) {
		super(Messages.AC_ANALYZE,appMgr,swingApp);
		setPreferredMenu("Plugins." + Messages.AC_MENU_ANALYSIS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cytoscape.util.CytoscapeAction#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			if (selectNetwork()) {
				final AnalysisExecutor exec = initAnalysisExecuter(network, null, swingApp);
				if (exec != null) {
					exec.start();
				}
			}
		} catch (InnerException ex) {
			// NetworkAnalyzer internal error
			logger.error(Messages.SM_LOGERROR, ex);
		}
	}

	/**
	 * Asks the user for interpretation and initializes the analysis executor class to perform the requested
	 * topological analysis.
	 * 
	 * @param aNetwork
	 *            Network to be analyzed.
	 * @param aNodeSet
	 *            Subset of nodes in <code>aNetwork</code>, for which topological parameters are to be
	 *            calculated. Set this to <code>null</code> if parameters must be calculated for all nodes in
	 *            the network.
	 * @return Newly initialized analysis executor; <code>null</code> if the user has decided to cancel the
	 *         operation.
	 */
	public static AnalysisExecutor initAnalysisExecuter(CyNetwork aNetwork, Set<CyNode> aNodeSet, CySwingApplication swingApp) {
		// Ask the user for an interpretation of the network edges
		try {
			final NetworkInspection status = CyNetworkUtils.inspectNetwork(aNetwork);
			NetworkInterpretation interpr = interpretNetwork(swingApp, status);
			if (interpr == null) {
				return null;
			}

			NetworkAnalyzer analyzer = null;
			if (interpr.isDirected()) {
				analyzer = new DirNetworkAnalyzer(aNetwork, aNodeSet, interpr);
			} else {
				analyzer = new UndirNetworkAnalyzer(aNetwork, aNodeSet, interpr);
			}
			return new AnalysisExecutor(swingApp.getJFrame(), analyzer);
		} catch (IllegalArgumentException ex) {
			Utils.showInfoBox(swingApp.getJFrame(),Messages.DT_INFO, Messages.SM_NETWORKEMPTY);
			return null;
		}
	}

	/**
	 * Attempts to find an interpretation for network's edges.
	 * <p>
	 * This method displays a dialog to the user. If the network status leads to a unique interpretation, the
	 * dialog informs the user about it. In case multiple interpretations are possible, the dialog asks the
	 * user to choose one.
	 * </p>
	 * 
	 * @param aInsp
	 *            Results of inspection on the edges of a network.
	 * @return Interpretation instance containing the directions for interpretation of network's edges;
	 *         <code>null</code> if the user has decided to cancel the operation.
	 * 
	 * @see InterpretationDialog
	 */
	private static NetworkInterpretation interpretNetwork(CySwingApplication swingApp, NetworkInspection aInsp) {
		final NetworkStatus status = NetworkStatus.getStatus( aInsp);
		final InterpretationDialog dialog = new InterpretationDialog(swingApp.getJFrame(),status);
		dialog.setVisible(true);

		if (dialog.pressedOK()) {
			return status.getInterpretations()[dialog.getUserChoice()];
		}
		return null;
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = 1079760835761343070L;
}
