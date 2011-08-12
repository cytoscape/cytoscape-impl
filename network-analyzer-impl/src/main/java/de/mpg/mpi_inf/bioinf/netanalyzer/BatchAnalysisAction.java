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
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.model.CyNetworkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.Interpretations;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.BatchAnalysisDialog;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.BatchResultsDialog;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.BatchSettingsDialog;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.Utils;

/**
 * Action handler for the menu item &quot;Batch Analysis&quot;.
 * 
 * @author Yassen Assenov
 * @author Nadezhda Doncheva
 */
public class BatchAnalysisAction extends NetAnalyzerAction {

	private static final Logger logger = LoggerFactory.getLogger(BatchAnalysisAction.class);
	
	private final CyNetworkReaderManager cyNetworkViewReaderMgr;
	private final CyNetworkManager netMgr;

	/**
	 * Constructs a new batch analysis action.
	 */
	protected BatchAnalysisAction(CyApplicationManager appMgr,CySwingApplication swingApp, CyNetworkManager netMgr, CyNetworkReaderManager cyNetworkViewReaderMgr) {
		super(Messages.AC_BATCH_ANALYSIS,appMgr,swingApp);
		this.netMgr = netMgr;
		this.cyNetworkViewReaderMgr = cyNetworkViewReaderMgr;
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
			final Frame desktop = swingApp.getJFrame();
			// Step 1 - Adjust settings
			BatchSettingsDialog d1 = new BatchSettingsDialog(desktop);
			d1.setVisible(true);
			final File[] inOutDirs = d1.getInOutDirs();

			// Step 2 - Run the analysis
			if (inOutDirs != null) {
				final List<File> files = getInputFiles(inOutDirs[0]);
				if (files.size() > 0) {
					final Interpretations ins = d1.getInterpretations();
					final BatchNetworkAnalyzer analyzer = new BatchNetworkAnalyzer(inOutDirs[1], files, ins,netMgr,cyNetworkViewReaderMgr );
					final BatchAnalysisDialog d2 = new BatchAnalysisDialog(desktop, analyzer);
					d2.setVisible(true);
					if (d2.resultsPressed()) {
						// Step 3 - Show results
						BatchResultsDialog d3 = new BatchResultsDialog(swingApp.getJFrame(),analyzer.getReports(),cyNetworkViewReaderMgr);
						d3.setVisible(true);
					}
				} else {
					Utils.showInfoBox(swingApp.getJFrame(),Messages.DT_INFO, Messages.SM_NOINPUTFILES);
				}
			}
		} catch (InnerException ex) {
			// NetworkAnalyzer internal error
			logger.error(Messages.SM_LOGERROR, ex);
		}
	}

	/**
	 * Get all readable Network files from the input directory. These are all SIF, GML and XGMML files.
	 * <p>
	 * This method is called upon initialization only.
	 * </p>
	 * 
	 * @param inputDir
	 *            Input directory as selected by the user.
	 * @return All readable Network files in the input directory, as a list of <code>File</code> instances.
	 */
	private List<File> getInputFiles(File inputDir) {
		final FileFilter inputFileFilter = new FileFilter() {

			public boolean accept(File aPathname) {
				if (aPathname.isFile() && aPathname.canRead()) {
					final String name = aPathname.getAbsolutePath();
					CyNetworkReader reader =  cyNetworkViewReaderMgr.getReader(aPathname.toURI(), name); 
					if (reader != null) {
						return true;
					}
				}
				return false;
			}
		};

		final List<File> inputFiles = Arrays.asList(inputDir.listFiles(inputFileFilter));
		Collections.sort(inputFiles);
		return inputFiles;
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = -1228030064334629585L;
}
