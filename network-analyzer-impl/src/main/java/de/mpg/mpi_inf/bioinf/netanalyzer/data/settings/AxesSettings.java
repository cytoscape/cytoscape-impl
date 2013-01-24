package de.mpg.mpi_inf.bioinf.netanalyzer.data.settings;

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

import org.jdom.Element;
import org.w3c.dom.DOMException;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.IntRange;

/**
 * Storage class for axis-related visual settings of a chart.
 * 
 * @author Yassen Assenov
 * @author Sven-Eric Schelhorn
 */
public final class AxesSettings extends Settings {

	/**
	 * Tag name used in XML settings file to identify axis-related settings.
	 */
	public static final String tag = "axes";

	/**
	 * Initializes a new instance of <code>AxesSettings</code> based on the given XML node.
	 * 
	 * @param aElement Node in XML settings file that identifies axis-related settings.
	 * @throws DOMException When the given element is not an element node with the expected name ({@link #tag})
	 *         or when the subtree rooted at <code>aElement</code> does not have the expected
	 *         structure.
	 */
	public AxesSettings(Element aElement) {
		super(aElement);
	}

	/**
	 * Gets the label of the domain axis (the &quot;x&quot;-axis).
	 * 
	 * @return Label of the domain axis.
	 */
	public String getDomainAxisLabel() {
		return domainAxisLabel;
	}

	/**
	 * Sets the label of the domain axis (the &quot;x&quot;-axis).
	 * 
	 * @param aDomainAxisLabel New label of the domain axis.
	 */
	public void setDomainAxisLabel(String aDomainAxisLabel) {
		domainAxisLabel = aDomainAxisLabel;
	}

	/**
	 * Gets the label of the range axis (the &quot;y&quot;-axis).
	 * 
	 * @return Label of the range axis.
	 */
	public String getRangeAxisLabel() {
		return rangeAxisLabel;
	}

	/**
	 * Sets the label of the range axis (the &quot;y&quot;-axis).
	 * 
	 * @param aRangeAxisLabel New label of the range axis.
	 */
	public void setRangeAxisLabel(String aRangeAxisLabel) {
		rangeAxisLabel = aRangeAxisLabel;
	}

	/**
	 * Gets the value restrictions to be imposed on the domain axis.
	 * 
	 * @return Value restrictions for the domain axis.
	 */
	public IntRange getDomainRange() {
		return domainRange;
	}

	/**
	 * Gets the value restrictions to be imposed on the range axis.
	 * 
	 * @return Value restrictions for the range axis.
	 */
	public IntRange getRangeRange() {
		return rangeRange;
	}

	/**
	 * Gets logarithmic range axis flag.
	 * 
	 * @return aLogRangeAxis Logarithmic range axis flag.
	 */
	public boolean getLogarithmicRangeAxis() {
		return logarithmicRangeAxis;
	}

	/**
	 * Sets logarithmic range axis flag.
	 * 
	 * @param aLogRangeAxis New logarithmic range axis flag.
	 */
	public void setLogarithmicRangeAxis(boolean aLogRangeAxis) {
		logarithmicRangeAxis = aLogRangeAxis;
	}

	/**
	 * Gets logarithmic domain axis flag.
	 * 
	 * @return aLogDomainAxis Logarithmic domain axis flag.
	 */
	public boolean getLogarithmicDomainAxis() {
		return logarithmicDomainAxis;
	}

	/**
	 * Sets logarithmic domain axis flag.
	 * 
	 * @param aLogDomainAxis New logarithmic domain axis flag.
	 */
	public void setLogarithmicDomainAxis(boolean aLogDomainAxis) {
		logarithmicDomainAxis = aLogDomainAxis;
	}

	/**
	 * Gets force integer ticks flag for domain axis.
	 * 
	 * @return <code>true</code> if the domain axis should have integer ticks only;
	 *         <code>false</code> otherwise.
	 */
	public boolean getIntegerDomainAxisTick() {
		return integerDomainAxisTick;
	}

	/**
	 * Gets force integer ticks flag for range axis.
	 * 
	 * @return <code>true</code> if the domain axis should have integer ticks only;
	 *         <code>false</code> otherwise.
	 */
	public boolean getIntegerRangeAxisTick() {
		return integerRangeAxisTick;
	}

	/**
	 * Name of the tag identifying the domain axis label.
	 */
	static final String domainAxisLabelTag = "catlabel";

	/**
	 * Name of the tag identifying the range axis label.
	 */
	static final String rangeAxisLabelTag = "rangelabel";

	/**
	 * Name of the tag identifying value restrictions for the domain axis.
	 */
	static final String domainRangeTag = "domrange";

	/**
	 * Name of the tag identifying value restrictions for the range axis.
	 */
	static final String rangeRangeTag = "ranrange";

	/**
	 * Name of the tag identifying the logarithmic range axis.
	 */
	static final String logarithmicRangeAxisTag = "lograngeaxis";

	/**
	 * Name of the tag identifying the logarithmic domain axis.
	 */
	static final String logarithmicDomainAxisTag = "logdomainaxis";

	/**
	 * Name of the tag identifying the forced integer domain axis ticks.
	 */
	static final String integerDomainAxisTickTag = "intdomainaxis";

	/**
	 * Name of the tag identifying the forced integer range axis ticks.
	 */
	static final String integerRangeAxisTickTag = "intrangeaxis";

	/**
	 * Initializes a new instance of <code>AxesSettings</code>.
	 * <p>
	 * The initialized instance contains no data and therefore this constructor is used only by the
	 * {@link Settings#clone()} method.
	 * </p>
	 */
	AxesSettings() {
		super();
	}

	/**
	 * Label of the domain axis.
	 */
	String domainAxisLabel;

	/**
	 * Label of the range axis.
	 */
	String rangeAxisLabel;

	/**
	 * Value restrictions for the domain axis.
	 */
	IntRange domainRange;

	/**
	 * Value restrictions for the range axis.
	 */
	IntRange rangeRange;

	/**
	 * Flag for displaying the range axis logarithmically.
	 */
	boolean logarithmicRangeAxis;

	/**
	 * Flag for displaying the domain axis logarithmically.
	 */
	boolean logarithmicDomainAxis;

	/**
	 * Flag for forcing domain axis ticks to be integers.
	 */
	boolean integerDomainAxisTick;

	/**
	 * Flag for forcing range axis ticks to be integers.
	 */
	boolean integerRangeAxisTick;
}
