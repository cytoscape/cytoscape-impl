package de.mpg.mpi_inf.bioinf.netanalyzer.ui.dec;

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

import java.awt.geom.Point2D;

/**
 * Data on a fitted function which is presented to the user.
 * 
 * @author Yassen Assenov
 */
public class FitData {

	/**
	 * Initializes a new instance of <code>FitData</code> without additional note.
	 * 
	 * @param aMessage Message indicating the fitting function that was used.
	 * @param aCoefs Coefficients of the fitted function.
	 * @param aCorrelation Correlation between the original and fitted data points. Set this
	 *        parameter to <code>null</code> if correlation is not computed.
	 * @param aRSquared Coefficient of determination (R-squared value) of the fitted points. Set
	 *        this parameter to <code>null</code> if r-squared is not computed.
	 * @param aHelpURL URL of the help page (or section) about the fitted function.
	 */
	public FitData(String aMessage, Point2D.Double aCoefs, Double aCorrelation, Double aRSquared, String aHelpURL) {
		this(aMessage, aCoefs, aCorrelation, aRSquared, aHelpURL, null);
	}

	/**
	 * Initializes a new instance of <code>FitData</code>.
	 * 
	 * @param aMessage Message indicating the fitting function that was used.
	 * @param aCoefs Coefficients of the fitted function.
	 * @param aCorrelation Correlation between the original and fitted data points. Set this
	 *        parameter to <code>null</code> if correlation is not computed.
	 * @param aRSquared Coefficient of determination (R-squared value) of the fitted points. Set
	 *        this parameter to <code>null</code> if r-squared is not computed.
	 * @param aHelpURL URL of the help page (or section) about the fitted function.
	 * @param aNote Additional note to be displayed, if any.
	 */
	public FitData(String aMessage, Point2D.Double aCoefs, Double aCorrelation, Double aRSquared, String aHelpURL, String aNote) {
		message = aMessage;
		coefs = aCoefs;
		correlation = aCorrelation;
		rSquared = aRSquared;
		helpURL = aHelpURL;
		note = aNote;
	}

	/**
	 * Gets a human-readable message about the fitted function.
	 * 
	 * @return Message indicating the fitting function that was used.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Gets the coefficients of the fitted function.
	 * 
	 * @return Coefficients of the fitted function. Interpretation of these coefficients depends on
	 *         the type of fitted function.
	 */
	public Point2D.Double getCoefs() {
		return coefs;
	}

	/**
	 * Get the value for the correlation.
	 * 
	 * @return Correlation between the original and fitted data points; <code>null</code> if
	 *         correlation is not computed.
	 */
	public Double getCorrelation() {
		return correlation;
	}

	/**
	 * Gets the r-squared value.
	 * 
	 * @return Coefficient of determination (R-squared value) of the fitted points;
	 *         <code>null</code> if r-squared is not computed.
	 */
	public Double getRSquared() {
		return rSquared;
	}

	/**
	 * Gets the help URL about the fitted function.
	 * 
	 * @return URL of the help page (or section) about the fitted function.
	 */
	public String getHelpURL() {
		return helpURL;
	}

	/**
	 * Gets the note to displayed.
	 * 
	 * @return Additional note to be displayed; <code>null</code> if no additional note should be
	 *         displayed.
	 */
	public String getNote() {
		return note;
	}

	/**
	 * Message indicating the fitting function that was used.
	 */
	private String message;

	/**
	 * Coefficients of the fitted function.
	 */
	private Point2D.Double coefs;

	/**
	 * Correlation between the original and fitted data points.
	 */
	private Double correlation;

	/**
	 * Coefficient of determination.
	 */
	private Double rSquared;

	/**
	 * URL of the help page (or section) about the fitted function.
	 */
	private String helpURL;

	/**
	 * Notes to be given, if any.
	 */
	private String note;
}
