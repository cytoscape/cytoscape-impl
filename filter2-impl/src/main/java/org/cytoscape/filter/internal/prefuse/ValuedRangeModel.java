package org.cytoscape.filter.internal.prefuse;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
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

import javax.swing.BoundedRangeModel;


/**
 * BoundedRangeModel that additionally supports a mapping between the integer
 * range used by interface components and a richer range of values, such
 * as numbers or arbitrary objects.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 * @see javax.swing.BoundedRangeModel
 */
public interface ValuedRangeModel extends BoundedRangeModel {
	/**
	 * Get the minimum value backing the range model. This is
	 * the absolute minimum value possible for the range span.
	 * @return the minimum value
	 */
	public Object getMinValue();

	/**
	 * Get the maximum value backing the range model. This is
	 * the absolute maximum value possible for the range span.
	 * @return the maximum value
	 */
	public Object getMaxValue();

	/**
	 * Get the value at the low point of the range span.
	 * @return the lowest value of the current range
	 */
	public Object getLowValue();

	/**
	 * Get the value at the high point of the range span.
	 * @return the highest value of the current range
	 */
	public Object getHighValue();
} // end of interface ValuedRangeModel
