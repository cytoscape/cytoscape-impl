package de.mpg.mpi_inf.bioinf.netanalyzer.data.ui;

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

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Implementer of the {@link Shape} interface for an x-like shape.
 * <p>
 * Instances of this shape have no area and, therefore, must not be rendered filled.
 * </p>
 * 
 * @author Yassen Assenov
 */
public class Cross implements Shape {

	/**
	 * Initializes new instance of the <code>Cross</code> shape.
	 * 
	 * @param aX
	 *            X coordinate of the beginning of the cross.
	 * @param aY
	 *            Y coordinate of the beginning of the cross.
	 * @param aWidth
	 *            Width of the cross.
	 * @param aHeight
	 *            Height of the cross.
	 */
	public Cross(double aX, double aY, double aWidth, double aHeight) {
		x = aX;
		y = aY;
		width = aWidth;
		height = aHeight;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Shape#contains(java.awt.geom.Point2D)
	 */
	public boolean contains(Point2D p) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Shape#contains(java.awt.geom.Rectangle2D)
	 */
	public boolean contains(Rectangle2D r) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Shape#contains(double, double)
	 */
	public boolean contains(double x, double y) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Shape#contains(double, double, double, double)
	 */
	public boolean contains(double x, double y, double w, double h) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Shape#getBounds()
	 */
	public Rectangle getBounds() {
		return new Rectangle((int) x, (int) y, (int) Math.round(width), (int) Math.round(height));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Shape#getBounds2D()
	 */
	public Rectangle2D getBounds2D() {
		return new Rectangle2D.Double(x, y, width, height);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Shape#getPathIterator(java.awt.geom.AffineTransform)
	 */
	public PathIterator getPathIterator(AffineTransform at) {
		final double[] points = new double[] { x, y, x + width, y + height };
		if (at != null) {
			at.transform(points, 0, points, 0, 2);
		}
		return new Iterator(points);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Shape#getPathIterator(java.awt.geom.AffineTransform, double)
	 */
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return getPathIterator(at);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Shape#intersects(java.awt.geom.Rectangle2D)
	 */
	public boolean intersects(Rectangle2D r) {
		return r.contains(x, y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Shape#intersects(double, double, double, double)
	 */
	public boolean intersects(double x, double y, double w, double h) {
		return new Rectangle2D.Double(x, y, w, h).intersects(getBounds2D());
	}

	/**
	 * X coordinate of the beginning of the cross.
	 */
	private double x;

	/**
	 * Y coordinate of the beginning of the cross.
	 */
	private double y;

	/**
	 * Height of the cross.
	 */
	private double height;

	/**
	 * Width of the cross.
	 */
	private double width;

	/**
	 * Path iterator for a (transformed) cross.
	 */
	private class Iterator implements PathIterator {

		/**
		 * Initializes a new instance of <code>Iterator</code>.
		 * 
		 * @param aPoints
		 *            Four-element array containing two diagonal points of the cross shape to be
		 *            iterated.
		 */
		public Iterator(double[] aPoints) {
			points = aPoints;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.geom.PathIterator#currentSegment(float[])
		 */
		public int currentSegment(float[] aCoords) {
			switch (segment) {
				case 0:
				case 4:
					aCoords[0] = (float) points[0];
					aCoords[1] = (float) points[1];
					return PathIterator.SEG_MOVETO;
				case 1:
					aCoords[0] = (float) points[2];
					aCoords[1] = (float) points[3];
					return PathIterator.SEG_LINETO;
				case 2:
					aCoords[0] = (float) points[0];
					aCoords[1] = (float) points[3];
					return PathIterator.SEG_MOVETO;
				case 3:
					aCoords[0] = (float) points[2];
					aCoords[1] = (float) points[1];
					return PathIterator.SEG_LINETO;
				default:
					return PathIterator.SEG_CLOSE;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.geom.PathIterator#currentSegment(double[])
		 */
		public int currentSegment(double[] aCoords) {
			switch (segment) {
				case 0:
				case 4:
					aCoords[0] = points[0];
					aCoords[1] = points[1];
					return PathIterator.SEG_MOVETO;
				case 1:
					aCoords[0] = points[2];
					aCoords[1] = points[3];
					return PathIterator.SEG_LINETO;
				case 2:
					aCoords[0] = points[0];
					aCoords[1] = points[3];
					return PathIterator.SEG_MOVETO;
				case 3:
					aCoords[0] = points[2];
					aCoords[1] = points[1];
					return PathIterator.SEG_LINETO;
				default:
					return PathIterator.SEG_CLOSE;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.geom.PathIterator#getWindingRule()
		 */
		public int getWindingRule() {
			return WIND_EVEN_ODD;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.geom.PathIterator#isDone()
		 */
		public boolean isDone() {
			return (segment == 6);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.geom.PathIterator#next()
		 */
		public void next() {
			if (segment < 6) {
				++segment;
			}
		}

		/**
		 * Four-element array containing two diagonal points of the cross shape being iterated.
		 */
		private double[] points;

		/**
		 * Current segment in the iteration.
		 */
		private int segment = 0;
	}
}
