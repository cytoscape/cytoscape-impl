package de.mpg.mpi_inf.bioinf.netanalyzer.data.filter;

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

import de.mpg.mpi_inf.bioinf.netanalyzer.data.ComplexParam;

/**
 * Interface implemented by all filters for complex parameters.
 * 
 * @author Yassen Assenov
 */
public interface ComplexParamFilter {

	/**
	 * Performs filtering on the given complex parameter.
	 * 
	 * @param aParam Complex parameter instance whose data is to be filtered.
	 * @return New instance of the same complex parameter type whose data is the result of applying
	 *         filtering criteria on <code>aParam</code>'s data.
	 * @throws UnsupportedOperationException If the complex parameter is not of the expected type.
	 */
	public ComplexParam filter(ComplexParam aParam);
}
