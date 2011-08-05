/*
 * Copyright (c) 2006, 2007, 2008, 2010, Max Planck Institute for Informatics, Saarbruecken, Germany.
 *
 * This file is part of NetworkAnalyzer.
 * 
 * NetworkAnalyzer is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 * 
 * NetworkAnalyzer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with NetworkAnalyzer. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package de.mpg.mpi_inf.bioinf.netanalyzer.data;

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
