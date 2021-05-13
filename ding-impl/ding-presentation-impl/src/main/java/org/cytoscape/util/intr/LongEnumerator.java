package org.cytoscape.util.intr;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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


/**
 * An enumeration over a list of 64 bit integers.
 */
public interface LongEnumerator {
	/**
	 * Returns a non-negative integer I such that nextLong() will successfully
	 * return a value no more and no less than I times.
	 */
	public int numRemaining();

	/**
	 * Returns the next 64 bit integer in the enumeration.
	 * If numRemaining() returns a non-positive quantity before
	 * nextLong() is called, the behavior of this enumeration becomes undefined.
	 */
	public long nextLong();
}
