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
