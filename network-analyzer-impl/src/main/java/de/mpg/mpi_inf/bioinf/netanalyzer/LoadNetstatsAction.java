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

import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkStats;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.io.StatsSerializer;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.AnalysisResultPanel;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.ResultPanelFactory;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.Utils;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.VisualStyleBuilder;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.events.CytoPanelStateChangedListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Action handler for the menu item &quot;Load Network Statistics&quot;.
 * 
 * @author Yassen Assenov
 */
public class LoadNetstatsAction extends NetAnalyzerAction {

	private static final Logger logger = LoggerFactory.getLogger(LoadNetstatsAction.class);
	
	private final CyNetworkViewManager viewManager;
	private final VisualMappingManager vmm;
	private final VisualStyleBuilder vsBuilder;
	private final ResultPanelFactory resultPanelFactory;
	private CyServiceRegistrar registrar;

	/**
	 * Initializes a new instance of <code>LoadNetstatsAction</code>.
	 */
	public LoadNetstatsAction(CyApplicationManager appMgr, CySwingApplication swingApp, final CyNetworkViewManager viewManager, final VisualStyleBuilder vsBuilder,
							  final VisualMappingManager vmm, final ResultPanelFactory resultPanelFactory, CyServiceRegistrar registrar) {
		super(Messages.AC_LOAD,appMgr,swingApp);
		this.viewManager = viewManager;
		this.vmm = vmm;
		this.vsBuilder = vsBuilder;
		this.resultPanelFactory = resultPanelFactory;
		this.registrar = registrar;
		setPreferredMenu(NetworkAnalyzer.PARENT_MENU + Messages.AC_MENU_ANALYSIS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			final Frame desktop = swingApp.getJFrame();
			final JFileChooser dialog = AnalysisResultPanel.netstatsDialog;
			final int openIt = dialog.showOpenDialog(desktop);
			if (openIt == JFileChooser.APPROVE_OPTION) {
				openNetstats(desktop, dialog.getSelectedFile());
				Utils.removeSelectedFile(dialog);
			}
			if (openIt == JFileChooser.ERROR_OPTION) {
				Utils.showErrorBox(desktop, Messages.DT_GUIERROR, Messages.SM_GUIERROR);
			}
		} catch (InnerException ex) {
			// NetworkAnalyzer internal error
			logger.error(Messages.SM_LOGERROR, ex);
		}
	}

	/**
	 * Opens the given network statistics file and visualizes its contents in an analysis dialog.
	 * <p>
	 * In case the file could not be opened, or if it contains invalid data, an informative error box is
	 * displayed.
	 * </p>
	 * 
	 * @param aFile
	 *            Network statistics file to be open.
	 */
	public void openNetstats(Window owner, File aFile) {
		try {
			final NetworkStats stats = StatsSerializer.load(aFile);
			final AnalysisResultPanel d = new AnalysisResultPanel(swingApp, owner, resultPanelFactory, stats, null, viewManager, vsBuilder, vmm);
			registrar.registerService(d, CytoPanelStateChangedListener.class, new Properties());
			d.setVisible(true);
		} catch (IOException ex) {
			// FileNotFoundException, IOException
			Utils.showErrorBox(owner, Messages.DT_IOERROR, Messages.SM_IERROR);
		} catch (NullPointerException ex) {
			Utils.showErrorBox(owner, Messages.DT_WRONGDATA, Messages.SM_WRONGDATAFILE);
		}
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = 5924903386374164549L;
}
