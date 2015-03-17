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

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.PluginSettingsDialog;

/**
 * Action handler for the menu item &quot;NetworkAnalyzer Settings&quot;.
 * 
 * @author Yassen Assenov
 */
public class SettingsAction extends NetAnalyzerAction {

	private static final long serialVersionUID = 7321507757114057304L;
	private static final Logger logger = LoggerFactory.getLogger(SettingsAction.class);

	/**
	 * Initializes a new instance of <code>SettingsAction</code>.
	 */
	public SettingsAction(CyApplicationManager appMgr,CySwingApplication swingApp) {
		super(Messages.AC_SETTINGS, appMgr, swingApp);
		setPreferredMenu(NetworkAnalyzer.PARENT_MENU + Messages.AC_MENU_ANALYSIS);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			// Display settings dialog
			final PluginSettingsDialog d = new PluginSettingsDialog(swingApp.getJFrame());
			d.setModalityType(ModalityType.APPLICATION_MODAL);
			d.setVisible(true);
		} catch (InnerException ex) {
			// NetworkAnalyzer internal error
			logger.error(Messages.SM_LOGERROR, ex);
		}
	}
}
