package org.cytoscape.view.vizmap.internal.mappings.interpolators;

/*
 * #%L
 * Cytoscape VizMap Impl (vizmap-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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
