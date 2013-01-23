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
 * This partial implementation of Interpolator assumes that the domain values
 * are some kind of number, and extracts the values into ordinary doubles for
 * the convenience of subclasses. If any argument is null, or if any of the
 * domain values is not an instance of Number, null is returned.
 */
abstract public class NumberInterpolator<R> implements Interpolator<Number, R> {

	@Override
	public <T extends Number> R getRangeValue(T lowerDomain, R lowerRange,
			T upperDomain, R upperRange, T domainValue) {

		if (lowerDomain == null || lowerRange == null || upperDomain == null
				|| upperRange == null || domainValue == null)
			return null;

		return getRangeValue(lowerDomain.doubleValue(), lowerRange, upperDomain
				.doubleValue(), upperRange, domainValue.doubleValue());
	}

	// /**
	// * DOCUMENT ME!
	// *
	// * @param lowerDomain DOCUMENT ME!
	// * @param lowerRange DOCUMENT ME!
	// * @param upperDomain DOCUMENT ME!
	// * @param upperRange DOCUMENT ME!
	// * @param domainValue DOCUMENT ME!
	// *
	// * @return DOCUMENT ME!
	// */
	// public R getRangeValue(Number lowerDomain, Object lowerRange, Object
	// upperDomain,
	// Object upperRange, Object domainValue) {
	// if ((lowerRange == null) || (upperRange == null)) {
	// return null;
	// }
	//
	// if ((lowerDomain == null) || !(lowerDomain instanceof Number)) {
	// return null;
	// }
	//
	// if ((upperDomain == null) || !(upperDomain instanceof Number)) {
	// return null;
	// }
	//
	// if ((domainValue == null) || !(domainValue instanceof Number)) {
	// return null;
	// }
	//
	// return getRangeValue(((Number) lowerDomain).doubleValue(), lowerRange,
	// ((Number) upperDomain).doubleValue(), upperRange,
	// ((Number) domainValue).doubleValue());
	// }
	//
	// /**
	// * DOCUMENT ME!
	// *
	// * @param lowerDomain DOCUMENT ME!
	// * @param lowerRange DOCUMENT ME!
	// * @param upperDomain DOCUMENT ME!
	// * @param upperRange DOCUMENT ME!
	// * @param domainValue DOCUMENT ME!
	// *
	// * @return DOCUMENT ME!
	// */
	abstract public R getRangeValue(double lowerDomain, R lowerRange,
			double upperDomain, R upperRange, double domainValue);
}
