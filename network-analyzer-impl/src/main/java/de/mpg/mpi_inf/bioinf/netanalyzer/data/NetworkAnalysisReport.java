package de.mpg.mpi_inf.bioinf.netanalyzer.data;

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

import java.io.File;

/**
 * Storage class for the success or failure of the analysis of a single network.
 * 
 * @author Yassen Assenov
 */
public class NetworkAnalysisReport {

	/**
	 * Initializes a new instance of <code>NetworkAnalysisReport</code> for a successful analysis.
	 * 
	 * @param aNetwork
	 *            File from which the network was loaded.
	 * @param aInterpr
	 *            Network interpretation applied.
	 * @param aResultFile
	 *            File in which the analysis results were stored.
	 */
	public NetworkAnalysisReport(File aNetwork, NetworkInterpretation aInterpr, File aResultFile) {
		error = null;
		network = aNetwork;
		interpretation = aInterpr;
		resultFile = aResultFile;
	}

	/**
	 * Initializes a new instance of <code>NetworkAnalysisReport</code> for failed analysis.
	 * 
	 * @param aNetwork
	 *            File from which the network was loaded (or attempted to be loaded).
	 * @param aInterpr
	 *            Network interpretation applied. Set this to <code>null</code> if the network could not be
	 *            loaded.
	 * @param aError
	 *            Reason for the failure of the analysis.
	 */
	public NetworkAnalysisReport(File aNetwork, NetworkInterpretation aInterpr, AnalysisError aError) {
		error = aError;
		network = aNetwork;
		interpretation = aInterpr;
		resultFile = null;
	}

	/**
	 * Gets the error that has occurred during the analysis.
	 * 
	 * @return Reason for the failure of the analysis; <code>null</code> if the analysis has completed
	 *         successfully.
	 */
	public AnalysisError getError() {
		return error;
	}

	/**
	 * Gets the network file.
	 * 
	 * @return File from which the network was loaded (or attempted to be loaded).
	 */
	public File getNetwork() {
		return network;
	}

	/**
	 * Gets the network interpretation.
	 * 
	 * @return Network interpretation applied; <code>null</code> if the network could not be loaded.
	 */
	public NetworkInterpretation getInterpretation() {
		return interpretation;
	}

	/**
	 * Gets the analysis results file.
	 * 
	 * @return File in which the analysis results were stored; <code>null</code> if the analysis has not
	 *         completed successfully.
	 */
	public File getResultFile() {
		return resultFile;
	}

	/**
	 * Reason for the failure of the analysis, if such exists.
	 */
	private AnalysisError error;

	/**
	 * File from which the network was loaded (or attempted to be loaded).
	 */
	private File network;

	/**
	 * Network interpretation applied, if any.
	 */
	private NetworkInterpretation interpretation;

	/**
	 * File in which the analysis results were stored, if such exists.
	 */
	private File resultFile;
}
