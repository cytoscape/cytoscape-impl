/*
  File: LinearNumberToNumberInterpolator.java

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

//LinearNumberToNumberInterpolator.java
//----------------------------------------------------------------------------
// $Revision: 10005 $
// $Date: 2007-04-17 19:50:13 -0700 (Tue, 17 Apr 2007) $
// $Author: kono $
//----------------------------------------------------------------------------
package org.cytoscape.view.vizmap.internal.mappings.interpolators;


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
