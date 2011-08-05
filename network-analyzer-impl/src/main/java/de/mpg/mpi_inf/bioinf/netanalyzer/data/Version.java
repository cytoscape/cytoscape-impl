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
 * Storage class for plugin and Cytoscape version.
 * <p>
 * In the plugin definition files, a version is defined as the string
 * <code>&lt;MAJOR&gt; . &gt;MINOR&lt;</code>. This class supports a more flexible version format of unlimited
 * granularity, for example <code>2.7.1.1</code>.
 * </p>
 * 
 * @author Yassen Assenov
 */
public class Version implements Comparable<Version> {

	/**
	 * Initializes a new instance of <code>Version</code> based on the given version string.
	 * <p>
	 * The version string must be a non-empty sequence of integers separated by the dot character (
	 * <code>.</code>). At least one of this integers must be non-zero.
	 * </p>
	 * 
	 * @param aVersionString
	 *            Text representation of the version.
	 * 
	 * @throws NullPointerException
	 *             If <code>aVersionString</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             If <code>aVersionString</code> is not in the expected format.
	 */
	public Version(String aVersionString) {
		try {
			// Read the sequence of integers
			if (aVersionString.endsWith(".")) {
				throw new IllegalArgumentException();
			}
			String[] elements = aVersionString.split("\\.");
			int elemCount = elements.length;
			mVersion = new int[elemCount];
			for (int i = 0; i < elemCount; ++i) {
				mVersion[i] = Integer.parseInt(elements[i]);
			}
			// Remove trailing zeroes, if any
			int nonTrailZeroCount = elemCount;
			for (; nonTrailZeroCount > 0; --nonTrailZeroCount) {
				if (mVersion[nonTrailZeroCount - 1] != 0) {
					break;
				}
			}
			if (nonTrailZeroCount == 0) {
				throw new IllegalArgumentException();
			}
			if (nonTrailZeroCount != elemCount) {
				// TODO: [Java 1.6] Use Arrays.copyOf( ... );
				int[] copied = new int[nonTrailZeroCount];
				for (int i = 0; i < nonTrailZeroCount; ++i) {
					copied[i] = mVersion[i];
				}
				mVersion = copied;
			}
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Version aVersion) {
		final int[] version2 = aVersion.mVersion;
		final int length1 = mVersion.length;
		final int length2 = version2.length;
		for (int i = 0; i < length1 && i < length2; ++i) {
			if (mVersion[i] < version2[i]) {
				return -1;
			}
			if (mVersion[i] > version2[i]) {
				return 1;
			}
		}
		return length1 - length2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hashCode = 0;
		int mult = 1;
		for (int i = 0; i < mVersion.length; mult *= 10, ++i) {
			hashCode += mVersion[i] * mult;
		}
		return hashCode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		try {
			return this == o || compareTo((Version) o) == 0;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * Checks if this version is newer than the given one.
	 * 
	 * @param aVersion
	 *            Version to be compared to.
	 * @return <code>true</code> if this instance represents a later version than <code>aVersion</code>;
	 *         <code>false</code> otherwise.
	 */
	public boolean isNewer(Version aVersion) {
		return (compareTo(aVersion) > 0);
	}

	/**
	 * Internal representation of a version - list of the version numbers.
	 * <p>
	 * The elements in this array are ordered by priority. The element at index <code>0</code> is usually the
	 * major version number.
	 * </p>
	 */
	private int[] mVersion;
}
