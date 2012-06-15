/*
  File: LinearNumberToColorInterpolator.java

  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/

//LinearNumberToColorInterpolator.java
package org.cytoscape.view.vizmap.internal.mappings.interpolators;

import java.awt.Color;



/**
 * The class provides a linear interpolation between color values. The
 * (red,green,blue,alpha) values of the returned color are linearly
 * interpolated from the associated values of the lower and upper colors,
 * according the the fractional distance frac from the lower value.
 *
 * If either object argument is not a Color, null is returned.
 */
public class LinearNumberToColorInterpolator extends LinearNumberInterpolator<Color> {

    /**
     *  DOCUMENT ME!
     *
     * @param frac DOCUMENT ME!
     * @param lowerRange DOCUMENT ME!
     * @param upperRange DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
	@Override
	public Color getRangeValue(double frac, Color lowerRange,
        Color upperRange) {

        double red = lowerRange.getRed() +
            (frac * (upperRange.getRed() - lowerRange.getRed()));
        double green = lowerRange.getGreen() +
            (frac * (upperRange.getGreen() - lowerRange.getGreen()));
        double blue = lowerRange.getBlue() +
            (frac * (upperRange.getBlue() - lowerRange.getBlue()));
        double alpha = lowerRange.getAlpha() +
            (frac * (upperRange.getAlpha() - lowerRange.getAlpha()));

        return new Color((int) Math.round(red), (int) Math.round(green),
            (int) Math.round(blue), (int) Math.round(alpha));
    }
}
