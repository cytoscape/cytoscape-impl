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

/**
 * Mutable integer.
 * 
 * @author Yassen Assenov
 */
public class MutInteger {

	/**
	 * Initializes a new instance of <code>MutInteger</code> and sets the value to <code>0</code>.
	 */
	public MutInteger() {
		value = 0;
	}

	/**
	 * Initializes a new instance of <code>MutInteger</code>.
	 * 
	 * @param aValue Initial value of this integer.
	 */
	public MutInteger(int aValue) {
		value = aValue;
	}

	/**
	 * Value of this mutable integer.
	 */
	public int value;
}
