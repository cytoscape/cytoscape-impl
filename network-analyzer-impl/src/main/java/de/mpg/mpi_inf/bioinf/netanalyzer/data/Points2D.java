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

import java.awt.geom.Point2D;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.io.LineReader;

/**
 * Complex parameter that stores a set of points in the 2D space. Every point is stored as a pair of its
 * coordinates - two <code>Double</code> values.
 * 
 * @author Yassen Assenov
 */
public class Points2D implements ComplexParam {

	/**
	 * Initializes a new instance of <code>Points2D</code> by loading it from the given stream.
	 * 
	 * @param aArgs View the documentation of {@link #load(String[], LineReader)} for a description of this
	 *        parameter.
	 * @param aReader View the documentation of {@link #load(String[], LineReader)} for a description of this
	 *        parameter.
	 * @throws IOException View the documentation of {@link #load(String[], LineReader)} for details.
	 * @see #load(String[], LineReader)
	 * @see ComplexParam#load(String[], LineReader)
	 */
	public Points2D(String[] aArgs, LineReader aReader) throws IOException {
		load(aArgs, aReader);
	}

	/**
	 * Initializes a new instance of <code>Points2D</code> based on the given list of points.
	 * 
	 * @param aPoints List of 2D points. The order of the points in preserved.
	 * @throws NullPointerException If <code>aPoints</code> is <code>null</code>.
	 */
	public Points2D(List<Point2D.Double> aPoints) {
		points = new Point2D.Double[aPoints.size()];
		aPoints.toArray(points);
	}

	/**
	 * Initializes a new instance of <code>Points2D</code> based on the given point set.
	 * 
	 * @param aPoints Set of 2D points.
	 * @throws NullPointerException If <code>aPoints</code> is <code>null</code>.
	 */
	public Points2D(Set<Point2D.Double> aPoints) {
		points = new Point2D.Double[aPoints.size()];
		aPoints.toArray(points);
	}

	/**
	 * Initializes a new instance of <code>Points2D</code> based on the given point set.
	 * 
	 * @param aPoints Set of 2D points in the form of {@link java.awt.geom.Point2D.Double} array.
	 */
	public Points2D(Point2D.Double[] aPoints) {
		points = (aPoints != null) ? aPoints : new Point2D.Double[0];
	}

	/**
	 * Gets the points in this point set.
	 * 
	 * @return All the points in this point set in the form of an array of
	 *         {@link java.awt.geom.Point2D.Double} instances.
	 */
	public Point2D.Double[] getPoints() {
		return points;
	}

	/**
	 * Gets the range of <i>x</i> values for this histogram.
	 * 
	 * @return Array of two elements - the minimum and the maximum value of the <i>x</i> coordinates in this
	 *         point set.
	 * 
	 * @throws IllegalStateException If this point set is empty, that is, if it contains no data points.
	 */
	public double[] getRangeX() {
		if (points.length == 0) {
			throw new IllegalStateException();
		}
		double[] range = new double[] { Double.MAX_VALUE, Double.MIN_VALUE };
		for (final Point2D.Double point : points) {
			if (point.x < range[0]) {
				range[0] = point.x;
			}
			if (point.x > range[1]) {
				range[1] = point.x;
			}
		}
		return range;
	}

	/**
	 * Loads the data of the point set from the given stream.
	 * 
	 * @param aArgs One-element array representing the argument passed to this type. This argument must be the
	 *        <code>String</code> representation of an integer and it specifies the cardinality of the point
	 *        set (the number of points).
	 * @param aReader Reader from a text stream. The reader must be open and positioned in the stream such
	 *        that the data for the points follows.
	 * @throws IOException If I/O error occurs.
	 * @throws NumberFormatException If the stream contains invalid data.
	 * @throws NullPointerException If at least one of the parameters is <code>null</code>.
	 */
	public void load(String[] aArgs, LineReader aReader) throws IOException {
		final int itemCount = Integer.parseInt(aArgs[0]);
		points = new Point2D.Double[itemCount];
		for (int i = 0; i < itemCount; ++i) {
			String[] coords = aReader.readLine().split(SEPREGEX);
			double x = Double.parseDouble(coords[0]);
			double y = Double.parseDouble(coords[1]);
			points[i] = new Point2D.Double(x, y);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.data.ComplexParam#save(java.io.FileWriter)
	 */
	public void save(FileWriter aWriter, boolean aSaveArgs) throws IOException {
		final int itemCount = points.length;
		if (aSaveArgs) {
			aWriter.write(String.valueOf(itemCount) + "\n");
		}
		for (int i = 0; i < itemCount; ++i) {
			double x = points[i].x;
			double y = points[i].y;
			String xStr = x < Long.MAX_VALUE && Math.floor(x) != x ? String.valueOf(x) : String.valueOf((long) x);
			String yStr = y < Long.MAX_VALUE && Math.floor(y) != y ? String.valueOf(y) : String.valueOf((long) y);
			aWriter.write(xStr + SEP + yStr + "\n");
		}
	}

	/**
	 * The set of 2D points.
	 */
	private Point2D.Double[] points;
}
