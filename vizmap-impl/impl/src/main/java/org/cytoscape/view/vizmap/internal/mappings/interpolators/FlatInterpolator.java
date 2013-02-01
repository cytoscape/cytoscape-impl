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
 * This simple Interpolator returns the value at either the lower or upper
 * boundary of the domain. Note that no check is made whether the supplied
 * domainValue is actually within the boundaries.
 */
public class FlatInterpolator<V, R> implements Interpolator<V, R> {
	/**
	 *
	 */
	public static final Integer LOWER = Integer.valueOf(0);

	/**
	 *
	 */
	public static final Integer UPPER = Integer.valueOf(1);
	private boolean useLower;

	/**
	 * The default FlatInterpolator returns the range value at the lower
	 * boundary.
	 */
	public FlatInterpolator() {
		useLower = true;
	}

	/**
	 * Constructs a FlatInterpolator which returns the range value at the lower
	 * boundary unless the argument 'mode' is equal to FlatInterpolator.UPPER.
	 */
	public FlatInterpolator(Integer mode) {
		if (mode.equals(UPPER))
			useLower = false;
		else
			useLower = true;
	}
//
//	/**
//	 * DOCUMENT ME!
//	 *
//	 * @param lowerDomain
//	 *            DOCUMENT ME!
//	 * @param lowerRange
//	 *            DOCUMENT ME!
//	 * @param upperDomain
//	 *            DOCUMENT ME!
//	 * @param upperRange
//	 *            DOCUMENT ME!
//	 * @param domainValue
//	 *            DOCUMENT ME!
//	 *
//	 * @return DOCUMENT ME!
//	 */
//	public R getRangeValue(V lowerDomain, R lowerRange, V upperDomain, R upperRange, V domainValue) {
//		return ((useLower) ? lowerRange : upperRange);
//	}

	public <T extends V> R getRangeValue(T lowerDomain, R lowerRange,
			T upperDomain, R upperRange, T domainValue) {
		return ((useLower) ? lowerRange : upperRange);
	}
}
