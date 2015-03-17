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

import javax.swing.JOptionPane;

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
 * Decorator for fitting a power law with the least squares methods.
 * <p>
 * The power law has the form f(x) = &alpha; x<sup>&beta;</sup>. The coefficients are stored in a
 * <code>Point2D.Double</code> instance as a couple (&alpha;, &beta;).
 * </p>
 * 
 * @author Yassen Assenov
 */
public class LeastSquaresPowerLawDecorator extends TwoCoefsDecorator {

	/**
	 * Initializes a new instance of <code>LeastSquaresPowerLawDecorator</code>.
	 * 
	 * @param aElement Node in XML settings file that defines a least squares power law fitter.
	 */
	public LeastSquaresPowerLawDecorator(Element aElement) {
		super(null);
	}

	@Override
	public Object clone() {
		return new LeastSquaresPowerLawDecorator(coefs);
	}

	@Override
	public void decorate(Window aOwner, JFreeChart aChart, ComplexParamVisualizer aVisualizer, boolean aVerbose) {
		clearDataset(aChart, seriesName);
		final Point2D.Double[] dataPoints = JFreeChartConn.extractData(aChart);
		final Point2D.Double[] posPoints = keepPositive(dataPoints);
		if (posPoints != dataPoints && aVerbose) {
			// Display a warning that not all points will be included in the fit
			if (aVerbose) {
				JOptionPane.showMessageDialog(aOwner, Messages.SM_FITNONPOSITIVE, Messages.DT_FIT,
						JOptionPane.WARNING_MESSAGE);
			}
		}
		if (posPoints.length < 2) {
			// Error - not enough data points
			if (aVerbose) {
				Utils.showErrorBox(aOwner, Messages.DT_FIT, Messages.SM_FITPLNODATA);
			}
			return;
		}
		coefs = Fitter.leastSquaresPowerLawFit(posPoints);

		if (coefs != null) {
			final XYSeries newData = createFittingData(posPoints, isLogLog(aVisualizer.getSettings()));
			final XYPlot plot = aChart.getXYPlot();
			int i = getDatasetIndex(plot, seriesName);
			if (i == -1) {
				i = createDataset(plot);
			}
			plot.setDataset(i, new XYSeriesCollection(newData));

			if (aVerbose) {
				// Compute correlation
				final int count = posPoints.length;
				final double[] s1 = new double[count];
				final double[] s2 = new double[count];
				for (int j = 0; j < count; ++j) {
					s1[j] = posPoints[j].y;
					s2[j] = valueAt(posPoints[j].x);
				}
				Double corr = null;
				Double rsquared = null;
				try {
					corr = new Double(Fitter.computeCorr(s1, s2));
				} catch (ArithmeticException ex) {
					// Correlation could not be computed; ignore
				}
				try {
					ArrayUtils.log(s1, true);
					ArrayUtils.log(s2, true);
					rsquared = new Double(Fitter.computeRSquared(s1, s2));
				} catch (ArithmeticException ex) {
					// R-Squared could not be computed; ignore
				}

				// Inform the user what the coefficients are
				showReport(aOwner, corr, rsquared);
			}
		} else {
			// Could not fit power law
			if (aVerbose) {
				Utils.showErrorBox(aOwner, Messages.DT_FIT, Messages.SM_FITPLERROR);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.dec.Decorator#undecorate(org.jfree.chart.JFreeChart)
	 */
	@Override
	public void undecorate(JFreeChart aChart) {
		clearDataset(aChart, seriesName);
		coefs = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.dec.Decorator#getButtonLabel()
	 */
	@Override
	public String getButtonLabel() {
		return isActive() ? Messages.DI_REMOVEPL : Messages.DI_FITPL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.dec.Decorator#getButtonToolTip()
	 */
	@Override
	public String getButtonToolTip() {
		return isActive() ? null : Messages.TT_FITPL;
	}

	/**
	 * Name of the data series this decorator adds to the chart.
	 */
	private static final String seriesName = "LS Power Law";

	/**
	 * Initializes a new instance of <code>LeastSquaresPowerLawDecorator</code>.
	 * <p>
	 * This constructor is used only by the {@link #clone()} method.
	 * </p>
	 * 
	 * @param aCoefs Coefficients of the fitted power law. Set this parameter to <code>null</code>
	 *        if coefficients are not computed.
	 */
	protected LeastSquaresPowerLawDecorator(Point2D.Double aCoefs) {
		super(aCoefs);
	}

	/**
	 * Creates a collection of points that adhere to the power law with the computed coefficients.
	 * 
	 * @param aPoints Data points to which the power law was fitted. They are used to determine the
	 *        range in which the newly created data series will lie.
	 * @param aIsLogLog Flag indicating if the points will be drawn on a log-log plot. If this
	 *        parameter is <code>true</code>, this method creates only two points.
	 * @return Newly created collection of points that follow the power law with coefficients in
	 *         {@link #coefs}.
	 */
	protected XYSeries createFittingData(Point2D.Double[] aPoints, boolean aIsLogLog) {
		XYSeries series = new XYSeries(seriesName, false, false);

		final Point2D.Double min = new Point2D.Double();
		final Point2D.Double max = new Point2D.Double();
		ArrayUtils.minMax(aPoints, min, max);
		final double xmin = min.x;
		final double xmax = max.x;
		final double ymin = min.y;
		final double ymax = max.y;
		if (xmin != 0) {
			final int resolution = ChartPanel.DEFAULT_WIDTH;
			final double step = (xmax - xmin) / resolution;

			if (aIsLogLog) {
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
		}
		return series;
	}

	/**
	 * Displays a dialog to report the result of fitting the power law.
	 * 
	 * @param aOwner Dialog from which the report dialog is to be displayed.
	 * @param aCorr Correlation between the original and the fitted data points.
	 * @param aRSquared R-Squared value, as computed on a logarithmized data.
	 */
	private void showReport(Window aOwner, Double aCorr, Double aRSquared) {
		final String powerLawSection = "#powerlaw";
		final String helpURL = HelpConnector.getFittingURL() + powerLawSection;
		final String note = "R-squared is computed on logarithmized values.";
		final FitData data = new FitData(Messages.SM_FITPL, coefs, aCorr, aRSquared, helpURL, note);
		FittingReportDialog d = new FittingReportDialog(aOwner, Messages.DT_FITTED, data);
		d.setVisible(true);
	}

	/**
	 * Computes the fitted value at a given point.
	 * 
	 * @param aPoint Point to compute the fitted value at.
	 * @return Fitted value at <code>aPoint</code>, computed according to the power law f(x) =
	 *         &alpha;x<sup>&beta;</sup>.
	 */
	private double valueAt(double aPoint) {
		return coefs.x * de.mpg.mpi_inf.bioinf.netanalyzer.data.Utils.pow(aPoint, coefs.y);
	}

	/**
	 * Computes the inverted function of the fitted power law.
	 * 
	 * @param aValue Value of the fitted power law.
	 * @return Point <code>x</code> such that the value of the power law at <code>x</code>
	 *         (approximately) equals <code>aValue</code>.
	 */
	private double pointAt(double aValue) {
		if (-EPSILON <= coefs.x && coefs.x <= EPSILON) {
			return 0;
		}
		final double power = coefs.y != 0 ? 1 / coefs.y : 1;
		return de.mpg.mpi_inf.bioinf.netanalyzer.data.Utils.pow(aValue / coefs.x, power);
	}
}
