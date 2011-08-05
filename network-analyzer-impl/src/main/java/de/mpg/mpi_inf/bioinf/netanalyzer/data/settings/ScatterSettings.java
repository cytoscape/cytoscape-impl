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
 * Storage class for visual settings of scatter plots.
 * 
 * @author Yassen Assenov
 */
public final class ScatterSettings extends Settings {

	/**
	 * Initializes a new instance of <code>ScatterSettings</code>.
	 * 
	 * @param aElement Node in the XML settings file that identifies scatter plot related settings.
	 * @throws DOMException When the given element is not an element node with the expected name ({@link #tag})
	 *         or when the subtree rooted at <code>aElement</code> does not have the expected
	 *         structure.
	 */
	public ScatterSettings(Element aElement) {
		super(aElement);
	}

	/**
	 * Gets the color of points.
	 * 
	 * @return Color of points in the scatter plot.
	 */
	public Color getPointColor() {
		return pointColor;
	}

	/**
	 * Gets the shape for points.
	 * 
	 * @return Shape of each point in the scatter plot.
	 */
	public PointShape getPointShape() {
		return pointShape;
	}

	/**
	 * Sets the color of points.
	 * 
	 * @param aPointColor Color of points in the scatter plot.
	 */
	public void setPointColor(Color aPointColor) {
		pointColor = aPointColor;
	}

	/**
	 * Sets the shape for points.
	 * 
	 * @param aPointShape Shape of each point in the scatter plot.
	 */
	public void setPointShape(PointShape aPointShape) {
		pointShape = aPointShape;
	}

	/**
	 * Tag name used in XML settings file to identify scatter plot settings.
	 */
	static final String tag = "scatter";

	/**
	 * Tag name used in XML settings file to identify color of points in the scatter plot.
	 */
	static final String pointColorTag = "pointcolor";

	/**
	 * Tag name used in XML settings file to identify point shape in the scatter plot.
	 */
	static final String pointShapeTag = "pointshape";

	/**
	 * Initializes a new instance of <code>ScatterSettings</code>.
	 * <p>
	 * The initialized instance contains no data and therefore this constructor is used only by the
	 * {@link Settings#clone()} method.
	 * </p>
	 */
	ScatterSettings() {
		super();
	}

	/**
	 * Color of points in the scatter plot.
	 */
	Color pointColor;

	/**
	 * Shape of each point in the scatter plot.
	 */
	PointShape pointShape;
}
