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

package de.mpg.mpi_inf.bioinf.netanalyzer.data;

/**
 * Enumeration on possible errors which can occur on a single network analysis during batch processing.
 * 
 * @author Nadezhda Doncheva
 */
public enum AnalysisError {

	/**
	 * Output (.netstats) file could not be created.
	 */
	OUTPUT_NOT_CREATED,

	/**
	 * I/O error has occurred while writing to the netstats file.
	 */
	OUTPUT_IO_ERROR,

	/**
	 * Exception has occurred during computation of topological parameters.
	 */
	INTERNAL_ERROR,

	/**
	 * Network with no nodes loaded.
	 */
	NETWORK_EMPTY,
	
	/**
	 * Network file is invalid.
	 */
	NETWORK_FILE_INVALID,

	/**
	 * Network file could not be opened.
	 */
	NETWORK_NOT_OPENED;

	/**
	 * Gets the message explaining the occurred <code>aError</code> to the user. 
	 * 
	 * @param aError Error occurred during batch analysis.
	 * @return Message for the user explaining the occurred error.
	 */
	public static String getMessage(AnalysisError aError) {
		switch (aError) {
		case OUTPUT_NOT_CREATED:
			return Messages.SM_OUTPUTNOTCREATED;
		case OUTPUT_IO_ERROR:
			return Messages.SM_OUTPUTIOERROR;
		case INTERNAL_ERROR:
			return Messages.SM_INTERNALERROR;
		case NETWORK_EMPTY:
			return Messages.SM_NETWORKEMPTY;
		case NETWORK_FILE_INVALID:
			return Messages.SM_NETWORKFILEINVALID;
		case NETWORK_NOT_OPENED:
			return Messages.SM_NETWORKNOTOPENED;
		default:
			return Messages.SM_UNKNOWNERROR;
		}
	}
}
