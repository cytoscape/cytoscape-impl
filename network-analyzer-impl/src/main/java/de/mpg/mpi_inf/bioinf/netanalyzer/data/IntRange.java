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
 * Integer range.
 * 
 * @author Yassen Assenov
 */
public final class IntRange {

	/**
	 * Initializes a new instance of <code>IntRange</code>.
	 * 
	 * @param aMin Minimum value in the integer range.
	 * @param aMax Maximum value in the integer range.
	 */
	public IntRange(Integer aMin, Integer aMax) {
		min = aMin;
		max = aMax;
	}

	/**
	 * Gets the maximum value of this integer range.
	 * 
	 * @return Maximum value of the range; <code>null</code> if no maximum value is present.
	 */
	public Integer getMax() {
		return max;
	}

	/**
	 * Gets the minimum value of this integer range.
	 * 
	 * @return Minimum value of the range; <code>null</code> if no minimum value is present.
	 */
	public Integer getMin() {
		return min;
	}

	/**
	 * Checks if a maximum value for this range is defined.
	 * <p>
	 * This is a convenience method only. Calling this method is equivalent to calling:<br/>
	 * <code>getMax() != null</code>
	 * </p>
	 * 
	 * @return <code>true</code> if this range has a maximum value; <code>false</code> otherwise.
	 */
	public boolean hasMax() {
		return max != null;
	}

	/**
	 * Checks if a minimum value for this range is defined.
	 * <p>
	 * This is a convenience method only. Calling this method is equivalent to calling:<br/>
	 * <code>getMin() != null</code>
	 * </p>
	 * 
	 * @return <code>true</code> if this range has a minimum value; <code>false</code> otherwise.
	 */
	public boolean hasMin() {
		return min != null;
	}

	/**
	 * Checks if this range is fully defined, that is, if it minimum and maximum value are defined.
	 * 
	 * @return <code>true</code> if this range has a minimum and a maximum value; <code>false</code>
	 *         otherwise.
	 */
	public boolean isFullyDefined() {
		return max != null && min != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + (min != null ? String.valueOf(min) : "") + ", "
				+ (max != null ? String.valueOf(max) : "") + "]";
	}

	/**
	 * Maximum value of the range.
	 */
	private Integer max;

	/**
	 * Minimum value of the range.
	 */
	private Integer min;
}
