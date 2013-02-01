package de.mpg.mpi_inf.bioinf.netanalyzer.data;

/*
 * #%L
 * Cytoscape NetworkAnalyzer Impl (network-analyzer-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013
 *   Max Planck Institute for Informatics, Saarbruecken, Germany
 *   The Cytoscape Consortium
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

import java.util.Comparator;

/**
 * Comparator of connected components ({@link de.mpg.mpi_inf.bioinf.netanalyzer.data.CCInfo} instances)
 * based on their sizes.
 * <p>
 * Note that this is an inverted comparator - a component of size <code>A</code> is reported as
 * larger than component of size <code>B</code> if and only if <code>A &lt; B</code>.
 * </p>
 * 
 * @author Yassen Assenov
 */
public class CCInfoInvComparator implements Comparator<CCInfo> {

	/**
	 * Initializes a new instance of <code>CCInfoComparator</code>.
	 */
	public CCInfoInvComparator() {
		// No specific initialization required
	}

	/**
	 * Compares the two given connected components.
	 * 
	 * @param o1 First connected component.
	 * @param o2 Second connected component.
	 * @return An integer which reflects the difference in the sizes of the two connected components -
	 *         positive integer if the first component is smaller than the second, negative if the
	 *         first component is larger, and <code>0</code> if both components are of the same
	 *         size.
	 */
	public int compare(CCInfo o1, CCInfo o2) {
		return o2.getSize() - o1.getSize();
	}
}
