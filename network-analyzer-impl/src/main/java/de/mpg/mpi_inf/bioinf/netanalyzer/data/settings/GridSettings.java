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

import java.awt.Color;

import org.jdom.Element;
import org.w3c.dom.DOMException;

/**
 * Storage class for gridline-related visual settings of a chart.
 * 
 * @author Yassen Assenov
 * @author Sven-Eric Schelhorn
 */
public final class GridSettings extends Settings {

	/**
	 * Tag name used in XML settings file to identify axis-related settings.
	 */
	public static final String tag = "grid";

	/**
	 * Initializes a new instance of <code>GridSettings</code> based on the given XML node.
	 * 
	 * @param aElement Node in XML settings file that identifies gridline-related settings.
	 * @throws DOMException When the given element is not an element node with the expected name ({@link #tag})
	 *         or when the subtree rooted at <code>aElement</code> does not have the expected
	 *         structure.
	 */
	public GridSettings(Element aElement) {
		super(aElement);
	}

	/**
	 * Gets the boolean flag signifying if we want to display horizontal gridlines.
	 * 
	 * @return Flag if we display gridlines.
	 */
	public boolean getHorizontalGridLines() {
		return horizontalGridLines;
	}

	/**
	 * Sets the boolean flag signifying if we want to display horizontal gridlines.
	 * 
	 * @param aGridLine New gridline flag.
	 */
	public void setHorizontalGridLines(boolean aGridLine) {
		horizontalGridLines = aGridLine;
	}

	/**
	 * Gets the boolean flag signifying if we want to display vertical gridlines.
	 * 
	 * @return Flag if we display gridlines.
	 */
	public boolean getVerticalGridLines() {
		return verticalGridLines;
	}

	/**
	 * Sets the boolean flag signifying if we want to display vertical gridlines.
	 * 
	 * @param aGridLine New gridline flag.
	 */
	public void setVerticalGridLines(boolean aGridLine) {
		verticalGridLines = aGridLine;
	}

	/**
	 * Gets the color of the gridlines.
	 * 
	 * @return Color the background is filled with.
	 */
	public Color getGridLinesColor() {
		return gridLinesColor;
	}

	/**
	 * Sets the color of the gridlines.
	 * 
	 * @param aBgColor New color to be used for drawing the gridlines.
	 */
	public void setGridLinesColor(Color aBgColor) {
		gridLinesColor = aBgColor;
	}

	/**
	 * Name of the tag identifying the gridlines flag.
	 */
	static final String horizontalGridLinesTag = "horizontal";

	/**
	 * Name of the tag identifying the gridlines flag.
	 */
	static final String verticalGridLinesTag = "vertical";

	/**
	 * Name of the tag identifying the gridlines color.
	 */
	static final String gridLinesColorTag = "cgridlines";

	/**
	 * Initializes a new instance of <code>GridSettings</code>.
	 * <p>
	 * The initialized instance contains no data and therefore this constructor is used only by the
	 * {@link Settings#clone()} method.
	 * </p>
	 */
	GridSettings() {
		super();
	}

	/**
	 * Flag for displaying horizontal gridlines.
	 */
	boolean horizontalGridLines;

	/**
	 * Flag for displaying vertical gridlines.
	 */
	boolean verticalGridLines;

	/**
	 * Color of the gridlines.
	 */
	Color gridLinesColor;	
}
