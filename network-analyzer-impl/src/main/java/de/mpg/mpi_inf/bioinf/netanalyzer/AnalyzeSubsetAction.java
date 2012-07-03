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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.CyNode;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.Utils;

/**
 * Action handler for the menu item &quot;Analyze Subset of Nodes&quot;.
 * 
 * @author Yassen Assenov
 * @author Nadezhda Doncheva
 */
public class AnalyzeSubsetAction extends NetAnalyzerAction {

	private static final Logger logger = LoggerFactory.getLogger(AnalyzeSubsetAction.class);
	
	private final AnalyzeNetworkAction action;

	/**
	 * Initializes a new instance of <code>NetSubsetAction</code>.
	 */
	public AnalyzeSubsetAction(CyApplicationManager appMgr, CySwingApplication swingApp, final AnalyzeNetworkAction action) {
		super(Messages.AC_ANALYZE_SUBSET,appMgr,swingApp);
		this.action = action;
		
		setPreferredMenu(NetworkAnalyzer.PARENT_MENU + Messages.AC_MENU_ANALYSIS);
		selected = null;
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
				final AnalysisExecutor exec = action.initAnalysisExecuter(network, selected,swingApp);
				if (exec != null) {
					exec.start();
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
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.NetAnalyzerAction#selectNetwork()
	 */
	@Override
	protected boolean selectNetwork() {
		if (super.selectNetwork()) {
			final List<CyNode> nodes = CyTableUtil.getNodesInState(network,"selected",true);
			if (nodes.isEmpty()) {
				Utils.showErrorBox(swingApp.getJFrame(),Messages.DT_WRONGDATA, Messages.SM_SELECTNODES);
				return false;
			}
			selected = new HashSet<CyNode>();
			for (final CyNode node : nodes) {
				selected.add(node);
			}
			return true;
		}
		return false;
	}

	/**
	 * Set of nodes in the networks that were selected before the user clicked on the item.
	 */
	protected Set<CyNode> selected;

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = 4670655302334870699L;
}
