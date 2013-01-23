package de.mpg.mpi_inf.bioinf.netanalyzer.dec;

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

import org.jdom.Element;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.AxesSettings;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.IntHistogramGroup;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.LongHistogramGroup;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.Points2DGroup;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.SettingsGroup;

/**
 * Base class for all decorators which are uniquely identified by a pair of coefficients.
 * 
 * @author Yassen Assenov
 */
public abstract class TwoCoefsDecorator extends Decorator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.dec.Decorator#isActive()
	 */
	@Override
	public boolean isActive() {
		return coefs != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.XMLSerializable#toXmlNode()
	 */
	public Element toXmlNode() {
		return new Element(this.getClass().getSimpleName());
	}

	/**
	 * Checks if the given settings group specifies a plot with two linear axes.
	 * 
	 * @param aSettings Settings group to be checked.
	 * @return <code>false</code> if the settings group and contains a logarithm domain axis flag
	 *         which is enabled or a logarithm range axis flags which is enabled; <code>true</code>
	 *         otherwise.
	 */
	protected static boolean isLinear(SettingsGroup aSettings) {
		AxesSettings s = null;
		if (aSettings instanceof IntHistogramGroup) {
			s = ((IntHistogramGroup) aSettings).axes;
		} else if (aSettings instanceof LongHistogramGroup) {
			s = ((LongHistogramGroup) aSettings).axes;
		} else if (aSettings instanceof Points2DGroup) {
			s = ((Points2DGroup) aSettings).axes;
		}
		if (s != null) {
			return !(s.getLogarithmicDomainAxis() || s.getLogarithmicRangeAxis());
		}
		return true;
	}

	/**
	 * Checks if the given settings group specifies a log-log plot.
	 * 
	 * @param aSettings Settings group to be checked.
	 * @return <code>true</code> the settings group contains a logarithmin domain axis flag and a
	 *         logarithmic range flags and they are both enabled; <code>false</code> otherwise.
	 */
	protected static boolean isLogLog(SettingsGroup aSettings) {
		AxesSettings s = null;
		if (aSettings instanceof IntHistogramGroup) {
			s = ((IntHistogramGroup) aSettings).axes;
		} else if (aSettings instanceof LongHistogramGroup) {
			s = ((LongHistogramGroup) aSettings).axes;
		} else if (aSettings instanceof Points2DGroup) {
			s = ((Points2DGroup) aSettings).axes;
		}
		if (s != null) {
			return s.getLogarithmicDomainAxis() && s.getLogarithmicRangeAxis();
		}
		return false;
	}

	/**
	 * Keeps the points with positive coordinates in the given collection.
	 * 
	 * @param aPoints Collection of points to be scanned.
	 * @return <code>aPoints</code> if all points in the collection are with positive coordinates.
	 *         Otherwise, the returned value is a newly created array of points containing those
	 *         elements in <code>aPoints</code> that have positive coordinates.
	 */
	protected static Point2D.Double[] keepPositive(Point2D.Double[] aPoints) {
		int nonZeroCount = 0;
		for (Point2D.Double p : aPoints) {
			if (p.x > 0 && p.y > 0) {
				nonZeroCount++;
			}
		}
		Point2D.Double[] result = aPoints;
		if (nonZeroCount != aPoints.length) {
			int i = 0;
			result = new Point2D.Double[nonZeroCount];
			for (Point2D.Double p : aPoints) {
				if (p.x > 0 && p.y > 0) {
					result[i++] = p;
				}
			}
		}
		return result;
	}

	/**
	 * Clears the dataset containing the data used to visualizes the fitted function.
	 * <p>
	 * If the chart does not contain a decorating dataset, calling this method has no effect.
	 * </p>
	 * 
	 * @param aChart Chart to be cleared from the decorating dataset.
	 * @param aSeriesName Name of data series to be used for locating the decorating dataset.
	 */
	protected static void clearDataset(JFreeChart aChart, String aSeriesName) {
		XYPlot plot = aChart.getXYPlot();
		final int i = getDatasetIndex(plot, aSeriesName);
		if (i != -1) {
			plot.setDataset(i, new XYSeriesCollection(new XYSeries(aSeriesName)));
		}
	}

	/**
	 * Creates a new dataset for visualizing the fitted function.
	 * <p>
	 * The new dataset is added to the end of the dataset list of the given plot.
	 * </p>
	 * 
	 * @param aPlot Plot to which the new dataset must be added.
	 * @return Index of the newly created dataset in the dataset list of <code>aPlot</code>.
	 */
	protected static int createDataset(XYPlot aPlot) {
		int i = aPlot.getDatasetCount();
		aPlot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		XYLineAndShapeRenderer rend = new XYLineAndShapeRenderer();
		rend.setSeriesLinesVisible(0, true);
		rend.setSeriesShapesVisible(0, false);
		aPlot.setRenderer(i, rend);
		return i;
	}

	/**
	 * Gets the index of the first dataset containing the specified series.
	 * 
	 * @param aPlot Plot to be searched for the dataset of the fitted function.
	 * @param aSeriesName Name of series the dataset should contain.
	 * @return Index of the dataset which contains series named <code>aSeriesName</code>,
	 *         <code>-1</code> if such dataset does not exist.
	 */
	protected static int getDatasetIndex(XYPlot aPlot, String aSeriesName) {
		final int datasetCount = aPlot.getDatasetCount();
		for (int i = 0; i < datasetCount; ++i) {
			if (aSeriesName.equals(aPlot.getDataset(i).getSeriesKey(0))) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Smallest value to be considered different than 0.
	 */
	protected static double EPSILON = 0.00001;

	/**
	 * Initializes the fields of a new <code>TwoCoefsDecorator</code>.
	 * 
	 * @param aCoefs Coefficients of the decorator. Set this parameter to <code>null</code> if
	 *        coefficients are not computed.
	 */
	protected TwoCoefsDecorator(Point2D.Double aCoefs) {
		coefs = aCoefs != null ? new Point2D.Double(aCoefs.x, aCoefs.y) : null;
	}

	/**
	 * Coefficients of the decorator.
	 * <p>
	 * This field is <code>null</code> when the coefficients are not yet computed.
	 * </p>
	 */
	protected Point2D.Double coefs;
}
