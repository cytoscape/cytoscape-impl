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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.ComplexParam;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Points2D;

/**
 * Filter for complex parameters of type <code>Points2D</code>.
 * 
 * @author Yassen Assenov
 */
public class Points2DFilter implements ComplexParamFilter {

	/**
	 * Initializes a new instance of <code>Points2DFilter</code> based on the given range.
	 * 
	 * @param aXMin Minimal value of the x coordinate to be considered.
	 * @param aXMax Maximal value of the x coordinate to be considered.
	 */
	public Points2DFilter(double aXMin, double aXMax) {
		xMin = aXMin;
		xMax = aXMax;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.data.filter.ComplexParamFilter#filter(de.mpg.mpi_inf.bioinf.netanalyzer.data.ComplexParam)
	 */
	public ComplexParam filter(ComplexParam aParam) {
		if (!(aParam instanceof Points2D)) {
			throw new UnsupportedOperationException();
		}
		final Point2D.Double[] original = ((Points2D) aParam).getPoints();
		final List<Point2D.Double> filtered = new ArrayList<Point2D.Double>(original.length);
		for (final Point2D.Double point : original) {
			if (xMin <= point.x && point.x <= xMax) {
				filtered.add(point);
			}
		}

		return new Points2D(filtered);
	}

	/**
	 * Minimal observed value considered in filtering.
	 */
	private double xMin;

	/**
	 * Maximal observed value considered in filtering.
	 */
	private double xMax;

}
