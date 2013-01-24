package org.cytoscape.filter.internal.widgets.autocomplete.index;

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

import java.util.List;


/**
 * Number index interface.
 * <p/>
 * This is a core data structure for indexing arbitrary Objects, based on a
 * numeric value, e.g. Integer or Double.
 *
 * @author Ethan Cerami.
 */
public interface NumberIndex extends GenericIndex {
	/**
	 * Gets a closed range of indexed values between lower and upper.
	 *
	 * Returns a view of the portion of this set whose elements range from
	 * lower, inclusive, to upper, inclusive.
	 *
	 * @param lower lower bound.
	 * @param upper upper bound.
	 * @return List of Objects.
	 */
	List getRange(Number lower, Number upper);

	/**
	 * Gets minimum value in index.
	 * @return min value.
	 */
	Number getMinimumValue();

	/**
	 * Gets maximum value in index.
	 * @return max value.
	 */
	Number getMaximumValue();
}
