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
