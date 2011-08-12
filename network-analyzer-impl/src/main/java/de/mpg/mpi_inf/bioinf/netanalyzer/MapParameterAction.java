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

import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.io.SettingsSerializer;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.PluginSettings;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.MapParameterDialog;

/**
 * Action handler for the menu item &quot;Visualize Parameters&quot;.
 * 
 * @author Nadezhda Doncheva
 * @author Yassen Assenov
 */
public class MapParameterAction extends NetAnalyzerAction implements AnalysisListener {

	private static final Logger logger = LoggerFactory.getLogger(MapParameterAction.class);
	/**
	 * Initializes a new instance of <code>MapParameterAction</code>.
	 */
	public MapParameterAction(CyApplicationManager appMgr,CySwingApplication swingApp) {
		super(Messages.AC_MAPPARAM,appMgr,swingApp);
		setPreferredMenu("Plugins."+Messages.AC_MENU_ANALYSIS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cytoscape.util.CytoscapeAction#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			// Verify there is a network loaded in Cytoscape and selected
			if (!selectNetwork()) {
				return;
			}
			// Display a dialog for mapping the computed attributes to the network
			// only if NetworkAnalyzer has computed any parameters and
			// and theses are stored in the node/edge attributes
			nodeAttr = CyNetworkUtils.getComputedNodeAttributes(network);
			edgeAttr = CyNetworkUtils.getComputedEdgeAttributes(network);
			final PluginSettings settings = SettingsSerializer.getPluginSettings();
			if ((nodeAttr[0].length > 0) || (nodeAttr[1].length > 0) || (edgeAttr[0].length > 0)
					|| (edgeAttr[1].length > 0)) {
				openMapParameterDialog();
			} else if (!settings.getUseNodeAttributes() && !settings.getUseEdgeAttributes()) {
				// Network does not contain computed parameters stored as attributes
				if (JOptionPane.showConfirmDialog(swingApp.getJFrame(), Messages.SM_LOADPARAMETERS,
						Messages.DT_ANALYSISNEEDED, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					settings.setUseNodeAttributes(true);
					settings.setUseEdgeAttributes(true);
					runNetworkAnalyzer();
				}
			} else {
				// Network does not contain computed parameters stored as attributes
				if (JOptionPane.showConfirmDialog(swingApp.getJFrame(), Messages.SM_RUNNETWORKANALYZER,
						Messages.DT_ANALYSISNEEDED, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					runNetworkAnalyzer();
				}
			}
		} catch (InnerException ex) {
			// NetworkAnalyzer internal error
			logger.error(Messages.SM_LOGERROR, ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.AnalysisListener#analysisCancelled()
	 */
	public void analysisCancelled() {
		// No specific action is required
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.AnalysisListener#analysisCompleted(de.mpg.mpi_inf.bioinf
	 * .netanalyzer.NetworkAnalyzer)
	 */
	public void analysisCompleted(NetworkAnalyzer analyzer) {
		openMapParameterDialog();
	}

	/**
	 * Runs the NetworkAnalyzer analysis on all nodes so that the network parameters are computed and can be
	 * mapped to visual styles afterwards.
	 */
	private void runNetworkAnalyzer() {
		final AnalysisExecutor exec = AnalyzeNetworkAction.initAnalysisExecuter(network, null, swingApp);
		if (exec != null) {
			exec.setShowDialog(false);
			exec.addAnalysisListener(this);
			exec.start();
		}
	}

	/**
	 * Opens the &quot;Map Parameters&quot; dialog.
	 */
	private void openMapParameterDialog() {
		nodeAttr = CyNetworkUtils.getComputedNodeAttributes(network);
		edgeAttr = CyNetworkUtils.getComputedEdgeAttributes(network);
		final MapParameterDialog d = new MapParameterDialog(swingApp.getJFrame(), network, nodeAttr,
				edgeAttr);
		d.setVisible(true);
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = -1321805300911486774L;

	/**
	 * Array with node attributes to be mapped to visual styles.
	 */
	private String[][] nodeAttr;

	/**
	 * Array with edge attributes to be mapped to visual styles.
	 */
	private String[][] edgeAttr;
}
