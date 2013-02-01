package de.mpg.mpi_inf.bioinf.netanalyzer.data.settings;

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
