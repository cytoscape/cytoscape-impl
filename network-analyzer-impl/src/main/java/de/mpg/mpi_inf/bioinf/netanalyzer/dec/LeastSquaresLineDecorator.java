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

import java.awt.Window;
import java.awt.geom.Point2D;

import org.jdom.Element;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.ArrayUtils;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.sconnect.HelpConnector;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.ComplexParamVisualizer;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.Utils;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.charts.JFreeChartConn;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.dec.FitData;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.dec.FittingReportDialog;

/**
 * Decorator for fitting a line with the least squares methods.
 * <p>
 * The power law has the form f(x) = &alpha; + &beta; x. The coefficients are stored in a
 * <code>Point2D.Double</code> instance as a couple (&alpha;, &beta;).
 * </p>
 * 
 * @author Yassen Assenov
 */
public class LeastSquaresLineDecorator extends TwoCoefsDecorator {

	/**
	 * Initializes a new instance of <code>LeastSquaresLineDecorator</code>.
	 * 
	 * @param aElement Node in XML settings file that defines a least squares linear fitter.
	 */
	public LeastSquaresLineDecorator(Element aElement) {
		super(null);
	}

	@Override
	public Object clone() {
		return new LeastSquaresLineDecorator(coefs);
	}

	@Override
	public void decorate(Window aOwner, JFreeChart aChart, ComplexParamVisualizer aVisualizer,
			boolean aVerbose) {
		clearDataset(aChart, LeastSquaresLineDecorator.seriesName);

		final Point2D.Double[] dataPoints = JFreeChartConn.extractData(aChart);
		if (dataPoints.length < 2) {
			// Error - not enough data points
			if (aVerbose) {
				Utils.showErrorBox(aOwner, Messages.DT_FIT, Messages.SM_FITLINENODATA);
			}
			return;
		}
		coefs = Fitter.leastSquaresLineFit(dataPoints);

		if (coefs != null) {
			final XYSeries newData = createFittingData(dataPoints, isLinear(aVisualizer.getSettings()));
			final XYPlot plot = aChart.getXYPlot();
			int i = getDatasetIndex(plot, LeastSquaresLineDecorator.seriesName);
			if (i == -1) {
				i = createDataset(plot);
			}
			plot.setDataset(i, new XYSeriesCollection(newData));

			if (aVerbose) {
				// Compute correlation
				final int count = dataPoints.length;
				final double[] s1 = new double[count];
				final double[] s2 = new double[count];
				for (int j = 0; j < count; ++j) {
					s1[j] = dataPoints[j].y;
					s2[j] = valueAt(dataPoints[j].x);
				}
				Double corr = null;
				Double rsquared = null;
				try {
					corr = new Double(Fitter.computeCorr(s1, s2));
				} catch (ArithmeticException ex) {
					// Correlation could not be computed; ignore
				}
				try {
					rsquared = new Double(Fitter.computeRSquared(s1, s2));
				} catch (ArithmeticException ex) {
					// R-Squared could not be computed; ignore
				}

				// Inform the user what the coefficients are
				showReport(aOwner, corr, rsquared);
			}
		} else {
			// Could not fit a line
			if (aVerbose) {
				Utils.showErrorBox(aOwner, Messages.DT_FIT, Messages.SM_FITLINEERROR);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.dec.Decorator#getButtonLabel()
	 */
	@Override
	public String getButtonLabel() {
		return isActive() ? Messages.DI_REMOVELINE : Messages.DI_FITLINE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.dec.Decorator#getButtonToolTip()
	 */
	@Override
	public String getButtonToolTip() {
		return isActive() ? null : Messages.TT_FITLINE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.dec.Decorator#undecorate(org.jfree.chart.JFreeChart)
	 */
	@Override
	public void undecorate(JFreeChart aChart) {
		clearDataset(aChart, LeastSquaresLineDecorator.seriesName);
		coefs = null;
	}

	/**
	 * Initializes a new instance of <code>LeastSquaresLineaDecorator</code>.
	 * <p>
	 * This constructor is used only by the {@link #clone()} method.
	 * </p>
	 * 
	 * @param aCoefs Coefficients of the fitted line. Set this parameter to <code>null</code> if
	 *        coefficients are not computed.
	 */
	protected LeastSquaresLineDecorator(Point2D.Double aCoefs) {
		super(aCoefs);
	}

	/**
	 * Name of the data series this decorator adds to the chart.
	 */
	private static final String seriesName = "LS Linear";

	/**
	 * Creates a collection of points lying on the line determined by the computed coefficients.
	 * 
	 * @param aPoints Data points to which the line was fitted. They are used to determine the range
	 *        in which the newly created data series will lie.
	 * @param aIsLinear Flag indicating if the points will be drawn on a plot with linear axes only.
	 *        If this parameter is <code>true</code>, this method creates only two points.
	 * @return Newly created collection of points that lie on the line determined by the
	 *         coefficients in {@link #coefs}.
	 */
	private XYSeries createFittingData(Point2D.Double[] aPoints, boolean aIsLinear) {
		XYSeries series = new XYSeries(LeastSquaresLineDecorator.seriesName, false, false);

		final Point2D.Double min = new Point2D.Double();
		final Point2D.Double max = new Point2D.Double();
		ArrayUtils.minMax(aPoints, min, max);
		final double xmin = min.x;
		final double xmax = max.x;
		final double ymin = min.y;
		final double ymax = max.y;
		final int resolution = ChartPanel.DEFAULT_WIDTH;
		final double step = (xmax - xmin) / resolution;

		if (aIsLinear) {
			double x = xmin;
			double y = valueAt(x);
			if (y < ymin) {
				y = valueAt(x = pointAt(ymin));
			} else if (ymax < y) {
				y = valueAt(x = pointAt(ymax));
			}
			series.add(x, y);

			x = xmax;
			y = valueAt(x);
			if (y < ymin) {
				y = valueAt(x = pointAt(ymin));
			} else if (ymax < y) {
				y = valueAt(x = pointAt(ymax));
			}
			series.add(x, y);
		} else {
			double x = xmin;
			for (int i = 0; i < resolution; ++i) {
				final double y = valueAt(x);
				if (ymin <= y && y <= ymax) {
					series.add(x, y);
				}
				x += step;
			}
			final double y = valueAt(xmax);
			if (ymin <= y && y <= ymax) {
				series.add(xmax, y);
			}
		}
		return series;
	}

	/**
	 * Displays a dialog to report the result of fitting a line.
	 * 
	 * @param aOwner Dialog from which the report dialog is to be displayed.
	 * @param aCorr Correlation between the original and the fitted data points.
	 * @param aRSquared R-Squared value for goodness of the fit.
	 */
	private void showReport(Window aOwner, Double aCorr, Double aRSquared) {
		final String linearSection = "#linear";
		final String helpURL = HelpConnector.getFittingURL() + linearSection;
		final FitData data = new FitData(Messages.SM_FITLINE, coefs, aCorr, aRSquared, helpURL);
		final FittingReportDialog d = new FittingReportDialog(aOwner, Messages.DT_FITTED, data);
		d.setVisible(true);
	}

	/**
	 * Computes the fitted value at a given point.
	 * 
	 * @param aPoint Point to compute the fitted value at.
	 * @return Fitted value at <code>aPoint</code>, computed according to the formula <i>f</i>(<i>x</i>) =
	 *         <i>&alpha;</i> + <i>x &beta;</i>.
	 */
	private double valueAt(double aPoint) {
		return coefs.x + aPoint * coefs.y;
	}

	/**
	 * Computes the inverted function of the fitted line.
	 * 
	 * @param aValue Value of the fitted line.
	 * @return Point <code>x</code> such that the value of the line at <code>x</code>
	 *         (approximately) equals <code>aValue</code>.
	 */
	private double pointAt(double aValue) {
		if (-EPSILON <= coefs.y && coefs.y <= EPSILON) {
			return 0;
		}
		return (aValue - coefs.x) / coefs.y;
	}
}
