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

import java.awt.Color;

import org.jdom.Element;
import org.w3c.dom.DOMException;

/**
 * Storage class for general visual settings of a chart.
 * 
 * @author Yassen Assenov
 * @author Sven-Eric Schelhorn
 */
public final class GeneralVisSettings extends Settings {

	/**
	 * Initializes a new instance of <code>GeneralVisSettings</code>.
	 * 
	 * @param aElement Node in the XML settings file that identifies general settings.
	 * @throws DOMException When the given element is not an element node with the expected name ({@link #tag})
	 *         or when the subtree rooted at <code>aElement</code> does not have the expected
	 *         structure.
	 */
	public GeneralVisSettings(Element aElement) {
		super(aElement);
	}

	/**
	 * Gets the chart title.
	 * 
	 * @return Title of the chart.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the chart title.
	 * 
	 * @param aTitle New title of the chart.
	 */
	public void setTitle(String aTitle) {
		title = aTitle;
	}

	/**
	 * Gets the color of the background.
	 * 
	 * @return Color the background is filled with.
	 */
	public Color getBgColor() {
		return bgColor;
	}

	/**
	 * Sets the color of the background.
	 * 
	 * @param aBgColor New color to be used for filling the background.
	 */
	public void setBgColor(Color aBgColor) {
		bgColor = aBgColor;
	}
		
	
	/**
	 * Tag name used in XML settings file to identify general settings.
	 */
	public static final String tag = "general";

	/**
	 * Initializes a new instance of <code>GeneralVisSettings</code>.
	 * <p>
	 * The initialized instance contains no data and therefore this constructor is used only by the
	 * {@link Settings#clone()} method.
	 * </p>
	 */
	GeneralVisSettings() {
		super();
	}

	/**
	 * Name of the tag identifying the title.
	 */
	static final String titleTag = "title";

	/**
	 * Name of the tag identifying the background color.
	 */
	static final String bgColorTag = "background";	
	
	/**
	 * Title of the chart.
	 */
	String title;
	
	/**
	 * Color of the background.
	 */
	Color bgColor;	
}
