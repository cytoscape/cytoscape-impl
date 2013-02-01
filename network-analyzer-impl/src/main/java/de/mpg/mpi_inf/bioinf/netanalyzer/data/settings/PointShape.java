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

import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;

/**
 * Enumeration on supported shapes for points in a scatter plot.
 * 
 * @author Yassen Assenov
 */
public enum PointShape {

	/**
	 * Single pixel.
	 */
	POINT,

	/**
	 * Circle.
	 */
	CIRCLE,

	/**
	 * Filled circle.
	 */
	FILLED_CIRCLE,

	/**
	 * Square.
	 */
	SQUARE,

	/**
	 * Filled square.
	 */
	FILLED_SQUARE,

	/**
	 * An x-like shape.
	 */
	CROSS;

	/**
	 * Text encodings of all shapes. These encodings are returned by the method {@link #toString()}
	 * of instances of this enumeration.
	 */
	public static String[] Texts = new String[] { "point", "circle", "filled circle", "square",
			"filled square", "cross" };

	/**
	 * Loads the point shape as encoded in the given text.
	 * 
	 * @param text
	 *            Text that specifies a point shape.
	 * @return The shape specified in the text, in the form of a {@link PointShape} instance.
	 * 
	 * @throws IllegalArgumentException
	 *             If <code>text</code> is <code>null</code> or if it does not encode a supported
	 *             shape.
	 */
	public static PointShape parse(String text) {
		if (Texts[0].equals(text)) {
			return POINT;
		}
		if (Texts[1].equals(text)) {
			return CIRCLE;
		}
		if (Texts[2].equals(text)) {
			return FILLED_CIRCLE;
		}
		if (Texts[3].equals(text)) {
			return SQUARE;
		}
		if (Texts[4].equals(text)) {
			return FILLED_SQUARE;
		}
		if (Texts[5].equals(text)) {
			return CROSS;
		}
		throw new IllegalArgumentException();
	}

	/**
	 * Gets the description of this shape.
	 * 
	 * @return Description, in human-readable form, of this shape.
	 */
	public String getDescription() {
		switch (this) {
			case POINT:
				return Messages.SET_SPOINT;
			case CIRCLE:
				return Messages.SET_SCIRCLE;
			case FILLED_CIRCLE:
				return Messages.SET_SFILLCIRCLE;
			case SQUARE:
				return Messages.SET_SSQUARE;
			case FILLED_SQUARE:
				return Messages.SET_SFILLSQUARE;
			default: // CROSS:
				return Messages.SET_SCROSS;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		switch (this) {
			case POINT:
				return Texts[0];
			case CIRCLE:
				return Texts[1];
			case FILLED_CIRCLE:
				return Texts[2];
			case SQUARE:
				return Texts[3];
			case FILLED_SQUARE:
				return Texts[4];
			default: // CROSS:
				return Texts[5];
		}
	}
}
