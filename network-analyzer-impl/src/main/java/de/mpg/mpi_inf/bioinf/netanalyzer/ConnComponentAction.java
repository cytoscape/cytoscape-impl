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

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.task.create.NewNetworkSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.work.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.CCInfo;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.CCInfoInvComparator;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.ConnComponentsDialog;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.Utils;

/**
 * Action handler for the menu item &quot;Connected Components&quot;.
 * 
 * @author Yassen Assenov
 */
public class ConnComponentAction extends NetAnalyzerAction {

	private static final Logger logger = LoggerFactory.getLogger(ConnComponentAction.class);

	private final NewNetworkSelectedNodesAndEdgesTaskFactory tf;
	private final TaskManager<?, ?> tm;
	
	/**
	 * Initializes a new instance of <code>ConnComponentAction</code>.
	 */
	public ConnComponentAction(CyApplicationManager appMgr,CySwingApplication swingApp, final NewNetworkSelectedNodesAndEdgesTaskFactory tf, final TaskManager<?, ?> tm) {
		super(Messages.AC_CONNCOMP,appMgr,swingApp);
		this.tf = tf;
		this.tm = tm;
		
		setPreferredMenu(NetworkAnalyzer.PARENT_MENU + Messages.AC_MENU_MODIFICATION);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			if (!selectNetwork()) {
				return;
			}

			ConnComponentAnalyzer an = new ConnComponentAnalyzer(network);

			Set<CCInfo> compsSet = an.findComponents();
			final int compsCount = compsSet.size();
			CCInfo[] comps = new CCInfo[compsCount];
			compsSet.toArray(comps);

			if (compsCount == 1) {
				final String msg = "<html><b>" + network.getRow(network).get("name",String.class) + "</b>" + Messages.SM_CONNECTED;
				Utils.showInfoBox(swingApp.getJFrame(),Messages.DT_CONNCOMP, msg);
			} else {
				Arrays.sort(comps, new CCInfoInvComparator());
				ConnComponentsDialog d = new ConnComponentsDialog(swingApp.getJFrame(), network, comps, tf, tm);
				d.setVisible(true);
			}
		} catch (InnerException ex) {
			// NetworkAnalyzer internal error
			logger.error(Messages.SM_LOGERROR, ex);
		}
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = -7465036491341908005L;
}
