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

package de.mpg.mpi_inf.bioinf.netanalyzer.data.settings;

import org.jdom.Element;
import org.w3c.dom.DOMException;

/**
 * Settings for filters on {@link de.mpg.mpi_inf.bioinf.netanalyzer.data.IntHistogram} instances.
 * 
 * @see de.mpg.mpi_inf.bioinf.netanalyzer.data.filter.IntHistogramFilter
 * @author Yassen Assenov
 */
public class IntHistogramFilterSettings extends Settings {

	/**
	 * Initializes a new instance of <code>IntHistogramFilterSettings</code> based on the given
	 * XML node.
	 * 
	 * @param aElement Node in the XML settings file that identifies integer histogram filter
	 *        settings.
	 * @throws DOMException When the given element is not an element node with the expected name ({@link #tag})
	 *         or when the subtree rooted at <code>aElement</code> does not have the expected
	 *         structure.
	 */
	public IntHistogramFilterSettings(Element aElement) {
		super(aElement);
	}

	/**
	 * Gets the label to be displayed for entering a value for minimal observation.
	 * 
	 * @return Human-readable message explaining the semantics of the minimal observation.
	 */
	public String getMinObservationLabel() {
		return minObservationLabel;
	}

	/**
	 * Gets the label to be displayed for entering a value for maximal observation.
	 * 
	 * @return Human-readable message explaining the semantics of the maximal observation.
	 */
	public String getMaxObservationLabel() {
		return maxObservationLabel;
	}

	/**
	 * Tag name used in XML settings file to identify <code>IntHistogram</code> filter settings.
	 */
	public static final String tag = "filter";

	/**
	 * Name of the tag identifying the minimal observation label.
	 */
	static final String minObservationLabelTag = "minobslabel";

	/**
	 * Name of the tag identifying the maximal observation label.
	 */
	static final String maxObservationLabelTag = "maxobslabel";

	/**
	 * Initializes a new instance of <code>IntHistogramFilterSettings</code>.
	 * <p>
	 * The initialized instance contains no data and therefore this constructor is used only by the
	 * {@link Settings#clone()} method.
	 * </p>
	 */
	IntHistogramFilterSettings() {
		super();
	}

	/**
	 * Label for entering value for the minimal observation.
	 */
	String minObservationLabel;

	/**
	 * Label for entering value for the maximal observation.
	 */
	String maxObservationLabel;
}
