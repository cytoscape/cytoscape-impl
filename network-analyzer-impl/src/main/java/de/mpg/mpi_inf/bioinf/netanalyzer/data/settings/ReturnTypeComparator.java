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

package de.mpg.mpi_inf.bioinf.netanalyzer.data.settings;

import java.lang.reflect.Method;
import java.util.Comparator;

/**
 * Comparator of methods based on the name of their return types.
 * 
 * @author Yassen Assenov
 */
public class ReturnTypeComparator implements Comparator<Method> {

	/**
	 * Initializes a new instance of <code>ReturnTypeComparator</code>.
	 */
	public ReturnTypeComparator() {
		// No specific initialization is required.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Method o1, Method o2) {
		final String r1 = o1.getReturnType().getSimpleName().toLowerCase();
		final String r2 = o2.getReturnType().getSimpleName().toLowerCase();
		return r1.compareTo(r2);
	}
}
