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

/**
 * Listener interface for analysis results listeners.
 * 
 * @author Yassen Assenov
 */
public interface AnalysisListener {

	/**
	 * Invoked when analysis is cancelled by the user.
	 */
	public void analysisCancelled();

	/**
	 * Invoked when analysis
	 * 
	 * @param aAnalyzer
	 *            Analyzer instance which has successfully completed the analysis of a network.
	 */
	public void analysisCompleted(NetworkAnalyzer aAnalyzer);
}
