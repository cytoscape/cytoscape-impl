/*
  File: NumberInterpolator.java

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

//NumberInterpolator.java
//----------------------------------------------------------------------------
// $Revision: 9736 $
// $Date: 2007-03-19 17:25:45 -0700 (Mon, 19 Mar 2007) $
// $Author: mes $
//----------------------------------------------------------------------------
package org.cytoscape.view.vizmap.internal.mappings.interpolators;


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
