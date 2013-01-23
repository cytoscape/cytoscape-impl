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


//----------------------------------------------------------------------------
/**
 * The class assumes that the supplied range objects are Numbers, and returns a
 * linearly interplated value according to the value of frac.
 * 
 * If either object argument is not a Number, null is returned.
 */
public class LinearNumberToNumberInterpolator extends
		LinearNumberInterpolator<Number> {

	/**
	 * DOCUMENT ME!
	 * 
	 * @param frac
	 *            DOCUMENT ME!
	 * @param lowerRange
	 *            DOCUMENT ME!
	 * @param upperRange
	 *            DOCUMENT ME!
	 * 
	 * @return Value for the given fraction point.
	 */
	@Override
	public Number getRangeValue(double frac, Number lowerRange,
			Number upperRange) {

		double lowerVal = lowerRange.doubleValue();
		double upperVal = upperRange.doubleValue();

		return (frac * upperVal) + ((1.0 - frac) * lowerVal);
	}
}
