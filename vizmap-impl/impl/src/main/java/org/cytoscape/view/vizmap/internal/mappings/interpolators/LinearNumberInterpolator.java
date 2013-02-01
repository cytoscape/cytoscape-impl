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
 * This subclass of NumberInterpolator further assumes a linear interpolation,
 * and calculates the fractional distance of the target domain value from the
 * lower boundary value for the convenience of subclasses.
 */
abstract public class LinearNumberInterpolator<R> extends NumberInterpolator<R> {

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
	 * @return DOCUMENT ME!
	 */
	abstract public R getRangeValue(double frac, R lowerRange, R upperRange);

	@Override
	public R getRangeValue(double lowerDomain, R lowerRange,
			double upperDomain, R upperRange, double domainValue) {

		if (lowerDomain == upperDomain)
			return lowerRange;

		double frac = (domainValue - lowerDomain) / (upperDomain - lowerDomain);

		return getRangeValue(frac, lowerRange, upperRange);
	}
}
