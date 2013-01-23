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

import java.net.URL;

import javax.swing.ImageIcon;

import de.mpg.mpi_inf.bioinf.netanalyzer.Plugin;

/**
 * Utility class providing helper methods for data manipulation.
 * 
 * @author Yassen Assenov
 */
public abstract class Utils {

	/**
	 * Loads an image stored in the given file.
	 * 
	 * @param aFileName Name of file storing the image.
	 * @param aDescription Human-readable description of the image.
	 * @return The <code>ImageIcon</code> instance representing the loaded image, or an empty
	 *         image if the file specified does not exist.
	 */
	public static ImageIcon getImage(String aFileName, String aDescription) {
		URL imageURL = Plugin.class.getResource("data/images/" + aFileName);
		return new ImageIcon(imageURL, aDescription);
	}

	/**
	 * Computes a relaxed power function.
	 * 
	 * @param a Base number.
	 * @param b Power number.
	 * @return <code>a<sup>b</sup></code> if <code>a</code> &ne; <code>0</code>; <code>0</code>
	 *         otherwise.
	 */
	public static double pow(double a, double b) {
		if (a != 0) {
			return Math.pow(a, b);
		}
		return 0;
	}

	/**
	 * Rounds a double <code>value</code> to <code>digit</code> digits after the point.
	 * 
	 * @param aValue Value to be rounded. 
	 * @param aDigits Digits the value should have after the point after the rounding.
	 * @return <code>Double</code> object that contains the rounded <code>value</code>.       
	 */
	public static Double roundTo(double aValue, int aDigits) {
		double d = Math.pow(10, aDigits);
		return new Double(Math.round(aValue*d)/d);
	}
}
