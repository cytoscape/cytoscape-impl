/*
  File: Interpolator.java

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

//Interpolator.java

//----------------------------------------------------------------------------
// $Revision: 9736 $
// $Date: 2007-03-19 17:25:45 -0700 (Mon, 19 Mar 2007) $
// $Author: mes $
//----------------------------------------------------------------------------
package org.cytoscape.view.vizmap.internal.mappings.interpolators;


//----------------------------------------------------------------------------
/**
 * This interface defines an interpolation function that takes two pairs
 * of (domain,range) values plus a target domain value, and calculates an
 * associated range value via some kind of interpolation.
 *
 * The behavior of this function is undefined if the target domain value
 * is not equal to one of the boundaries or between them.
 * 
 * @param V domain values.
 * @param R range values.  These are Color, Number, etc.
 * 
 * @author mes
 * @author kono
 */
public interface Interpolator<V, R> {
	/**
	 *  DOCUMENT ME!
	 *
	 * @param lowerDomain DOCUMENT ME!
	 * @param lowerRange DOCUMENT ME!
	 * @param upperDomain DOCUMENT ME!
	 * @param upperRange DOCUMENT ME!
	 * @param domainValue DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	 public <T extends V> R getRangeValue( T lowerDomain, R lowerRange, T upperDomain,
	                            R  upperRange, T domainValue);
}
