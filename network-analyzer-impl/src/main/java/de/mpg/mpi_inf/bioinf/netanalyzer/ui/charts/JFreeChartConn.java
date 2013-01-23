package de.mpg.mpi_inf.bioinf.netanalyzer.ui.charts;

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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;

import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsio.svg.SVGGraphics2D;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.IntHistogram;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.IntRange;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.LongHistogram;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Points2D;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.AxesSettings;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.BarsSettings;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.GeneralVisSettings;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.GridSettings;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.IntHistogramGroup;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.LongHistogramGroup;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.Points2DGroup;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.ScatterSettings;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.ui.Cross;

/**
 * Connector to the JFreeChart chart drawing library.
 * <p>
 * Note to developers: In order to switch to or add support for another chart library, extract all
 * public methods of this class to an interface and implement the interface.
 * </p>
 * 
 * @author Yassen Assenov
 * @author Sven-Eric Schelhorn
 */
public abstract class JFreeChartConn {

	/**
	 * Creates a chart that displays the given integer histogram data.
	 * <p>
	 * This is a convenience method, it calls
	 * {@link #createHistogram(IntHistogram, IntHistogramGroup)} or
	 * {@link #createScatter(IntHistogram, IntHistogramGroup)} based on the value of the given
	 * settings group.
	 * </p>
	 * 
	 * @param aHistogram
	 *            Complex parameter that stores the data to be visualized.
	 * @param aSettings
	 *            Settings group for integer histogram.
	 * @return Newly created chart control.
	 * @see IntHistogramGroup#useScatter()
	 * @see #createHistogram(IntHistogram, IntHistogramGroup)
	 * @see #createScatter(IntHistogram, IntHistogramGroup)
	 */
	public static JFreeChart createChart(IntHistogram aHistogram, IntHistogramGroup aSettings) {
		if (aSettings.useScatter()) {
			return createScatter(aHistogram, aSettings);
		}
		return createHistogram(aHistogram, aSettings);
	}

	/**
	 * Creates a chart that displays the given long histogram data.
	 * <p>
	 * This is a convenience method, it calls
	 * {@link #createHistogram(LongHistogram, LongHistogramGroup)} or
	 * {@link #createScatter(LongHistogram, LongHistogramGroup)} based on the value of the given
	 * settings group.
	 * </p>
	 * 
	 * @param aHistogram
	 *            Complex parameter that stores the data to be visualized.
	 * @param aSettings
	 *            Settings group for integer histogram.
	 * @return Newly created chart control.
	 * @see LongHistogramGroup#useScatter()
	 * @see #createHistogram(LongHistogram, LongHistogramGroup)
	 * @see #createScatter(LongHistogram, LongHistogramGroup)
	 */
	public static JFreeChart createChart(LongHistogram aHistogram, LongHistogramGroup aSettings) {
		if (aSettings.useScatter()) {
			return createScatter(aHistogram, aSettings);
		}
		return createHistogram(aHistogram, aSettings);
	}

	/**
	 * Creates a histogram chart that displays the given integer histogram data.
	 * 
	 * @param aHistogram
	 *            Complex parameter that stores the data to be visualized.
	 * @param aSettings
	 *            Settings group for integer histogram.
	 * @return Newly created chart control.
	 */
	public static JFreeChart createHistogram(IntHistogram aHistogram, IntHistogramGroup aSettings) {

		XYSeriesCollection collection = fromIntHistogram(aHistogram);
		JFreeChart chart = ChartFactory.createHistogram(null, // title
				convertLabel(aSettings.axes.getDomainAxisLabel()), // label of X axis
				convertLabel(aSettings.axes.getRangeAxisLabel()), // label of Y axis
				collection, // dataset
				PlotOrientation.VERTICAL, // orientation
				false, // create legend
				false, // display tooltips
				false); // generate urls
		XYPlot plot = chart.getXYPlot();
		NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		updateGeneral(plot, aSettings.general);
		updateAxes(chart, aSettings.axes, aSettings.grid);
		updateBars(plot, aSettings.bars);
		chart.setBackgroundPaint(null);
		return chart;
	}

	/**
	 * Creates a histogram chart that displays the given long histogram data.
	 * 
	 * @param aHistogram
	 *            Complex parameter that stores the data to be visualized.
	 * @param aSettings
	 *            Settings group for long histogram.
	 * @return Newly created chart control.
	 */
	public static JFreeChart createHistogram(LongHistogram aHistogram, LongHistogramGroup aSettings) {
		XYSeriesCollection collection = fromLongHistogram(aHistogram);
		JFreeChart chart = ChartFactory.createHistogram(null, // title
				convertLabel(aSettings.axes.getDomainAxisLabel()), // label of X axis
				convertLabel(aSettings.axes.getRangeAxisLabel()), // label of Y axis
				collection, // dataset
				PlotOrientation.VERTICAL, // orientation
				false, // create legend
				false, // display tooltips
				false); // generate urls
		XYPlot plot = chart.getXYPlot();
		NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		updateGeneral(plot, aSettings.general);
		updateAxes(chart, aSettings.axes, aSettings.grid);
		updateBars(plot, aSettings.bars);
		chart.setBackgroundPaint(null);
		return chart;
	}

	/**
	 * Encloses the given chart into a panel.
	 * 
	 * @param aChart
	 *            Chart to be enclosed in a panel.
	 * @return Panel that contains <code>aChart</code>.
	 */
	public static JPanel createPanel(JFreeChart aChart) {
		return new ChartPanel(aChart);
	}

	/**
	 * Creates a scatter plot that displays the given integer histogram data.
	 * 
	 * @param aHistogram
	 *            Complex parameter that stores the data to be visualized.
	 * @param aSettings
	 *            Settings group for integer histogram.
	 * @return Newly created chart control.
	 */
	public static JFreeChart createScatter(IntHistogram aHistogram, IntHistogramGroup aSettings) {
		return createScatter(fromIntHistogram(aHistogram), aSettings.general, aSettings.axes,
				aSettings.grid, aSettings.scatter);
	}

	/**
	 * Creates a scatter plot that displays the given long histogram data.
	 * 
	 * @param aHistogram
	 *            Complex parameter that stores the data to be visualized.
	 * @param aSettings
	 *            Settings group for long histogram.
	 * @return Newly created chart control.
	 */
	public static JFreeChart createScatter(LongHistogram aHistogram, LongHistogramGroup aSettings) {
		return createScatter(fromLongHistogram(aHistogram), aSettings.general, aSettings.axes,
				aSettings.grid, aSettings.scatter);
	}

	/**
	 * Creates a scatter plot that displays the given data points.
	 * 
	 * @param aPoints
	 *            Complex parameter that stores the data to be visualized.
	 * @param aSettings
	 *            Settings group for data points.
	 * @return Newly created chart control.
	 */
	public static JFreeChart createScatter(Points2D aPoints, Points2DGroup aSettings) {
		return createScatter(fromPoints2D(aPoints), aSettings.general, aSettings.axes,
				aSettings.grid, aSettings.scatter);
	}

	/**
	 * Extracts the visualized data from a given chart instance.
	 * <p>
	 * This methods extracts the default data series from the default dataset of the given chart.
	 * </p>
	 * 
	 * @param aChart
	 *            Chart to extract the data from.
	 * @return Visualized data in the form of an array of points.
	 */
	public static Point2D.Double[] extractData(JFreeChart aChart) {
		XYDataset dataColl = aChart.getXYPlot().getDataset();
		final int n = dataColl.getItemCount(0);
		Point2D.Double[] dataPoints = new Point2D.Double[n];
		for (int i = 0; i < n; ++i) {
			dataPoints[i] = new Point2D.Double(dataColl.getXValue(0, i), dataColl.getYValue(0, i));
		}
		return dataPoints;
	}

	/**
	 * Saves the given chart to a JPEG image file.
	 * 
	 * @param aFile
	 *            File to be saved to.
	 * @param aChart
	 *            Chart to be saved.
	 * @param aWidth
	 *            Desired width of the image.
	 * @param aHeight
	 *            Desired height of the image.
	 * @throws IOException
	 *             If I/O error occurs.
	 */
	public static void saveAsJpeg(File aFile, JFreeChart aChart, int aWidth, int aHeight)
			throws IOException {
		// Force white background because JPEG does not support transparency
		aChart.setBackgroundPaint(Color.WHITE);
		ChartUtilities.saveChartAsJPEG(aFile, aChart, aWidth, aHeight);
		aChart.setBackgroundPaint(null);
	}

	/**
	 * Saves the given chart to a PNG image file.
	 * 
	 * @param aFile
	 *            File to be saved to.
	 * @param aChart
	 *            Chart to be saved.
	 * @param aWidth
	 *            Desired width of the image.
	 * @param aHeight
	 *            Desired height of the image.
	 * @throws IOException
	 *             If I/O error occurs.
	 */
	public static void saveAsPng(File aFile, JFreeChart aChart, int aWidth, int aHeight)
			throws IOException {
		ChartUtilities.saveChartAsPNG(aFile, aChart, aWidth, aHeight);
	}

	/**
	 * Saves the given chart to a SVG image file.
	 * <p>
	 * This method uses the FreeHEP VectorGraphics library.
	 * </p>
	 * 
	 * @param aFile
	 *            File to be saved to.
	 * @param aChart
	 *            Chart to be saved.
	 * @param aWidth
	 *            Desired width of the image.
	 * @param aHeight
	 *            Desired height of the image.
	 * @throws IOException
	 *             If I/O error occurs.
	 */
	public static void saveAsSvg(File aFile, JFreeChart aChart, int aWidth, int aHeight)
			throws IOException {
		final VectorGraphics graphics = new SVGGraphics2D(aFile, new Dimension(aWidth, aHeight));
		graphics.startExport();
		aChart.draw(graphics, new Rectangle2D.Double(0, 0, aWidth, aHeight));
		graphics.endExport();
	}

	/**
	 * Changes the chart displayed in the given panel.
	 * 
	 * @param aPanel
	 *            Panel to be used for storing the chart.
	 * @param aChart
	 *            Chart to be set to <code>aPanel</code>.
	 */
	public static void setChart(JPanel aPanel, JFreeChart aChart) {
		((ChartPanel) aPanel).setChart(aChart);
	}

	/**
	 * Updates the axis-related properties of a chart.
	 * 
	 * @param aControl
	 *            Chart control to be updated.
	 * @param aAxes
	 *            Axis-related visual settings to be applied.
	 * @param aGrid
	 *            Grid-related visual settings to be applied.
	 */
	public static void updateAxes(JFreeChart aControl, AxesSettings aAxes, GridSettings aGrid) {
		XYPlot plot = aControl.getXYPlot();
		Range domainDataRange = aAxes.getLogarithmicDomainAxis() ? new Range(logLowerBound(plot
				.getDataset(), true), plot.getDataRange(plot.getDomainAxis()).getUpperBound())
				: plot.getDataRange(plot.getDomainAxis());
		Range rangeDataRange = aAxes.getLogarithmicRangeAxis() ? new Range(logLowerBound(plot
				.getDataset(), false), plot.getDataRange(plot.getRangeAxis()).getUpperBound())
				: plot.getDataRange(plot.getRangeAxis());
		updateAxes(plot, aAxes, aGrid, domainDataRange, rangeDataRange);
	}

	/**
	 * Updates the bar properties of a chart.
	 * 
	 * @param aControl
	 *            Chart control to be updated.
	 * @param aBars
	 *            Bar visual settings to be applied.
	 */
	public static void updateBars(JFreeChart aControl, BarsSettings aBars) {
		updateBars(aControl.getXYPlot(), aBars);
	}

	/**
	 * Updates the general properties of a chart.
	 * 
	 * @param aControl
	 *            Chart control to be updated.
	 * @param aGeneral
	 *            General visual settings to be applied.
	 */
	public static void updateGeneral(JFreeChart aControl, GeneralVisSettings aGeneral) {
		updateGeneral(aControl.getXYPlot(), aGeneral);
	}

	/**
	 * Updates the point-related properties of a scatter plot.
	 * 
	 * @param aControl
	 *            Chart control to be updated.
	 * @param aScatter
	 *            Visual settings to be applied.
	 */
	public static void updateScatter(JFreeChart aControl, ScatterSettings aScatter) {
		updateScatter(aControl.getXYPlot(), aScatter);
	}

	/**
	 * Converts the <code>String</code> value to be used for label.
	 * 
	 * @param aLabel
	 *            Label text.
	 * @return <code>aLabel</code> if the parameter is not the empty string; <code>null</code>
	 *         otherwise.
	 */
	private static String convertLabel(String aLabel) {
		if ("".equals(aLabel)) {
			return null;
		}
		return aLabel;
	}

	private static double logLowerBound(XYDataset aDataset, boolean isDomainAxis) {
		double lowerBound = 0.0;
		for (int i = 0; i < aDataset.getItemCount(0); i++) {
			double tmp = isDomainAxis ? aDataset.getXValue(0, i) : aDataset.getYValue(0, i);
			if (lowerBound == 0.0 || tmp < lowerBound)
				lowerBound = tmp;
		}

		return lowerBound;
	}

	/**
	 * Delegates the creation of either a domain- or a range-axis according to its
	 * <code>AxesSettings</code>
	 * 
	 * @param aAxes
	 *            AxesSettings according to which the axis is build
	 * @param domain
	 *            Flag indicating if the axis to be created is a domain-axis or not. If this flag is
	 *            set to <code>false</code>, a range-axis is created.
	 * @return NumberAxis
	 */
	private static NumberAxis createAxis(AxesSettings aAxes, boolean domain, Range aDataRange) {
		if (domain) {
			return createAxis(convertLabel(aAxes.getDomainAxisLabel()), aAxes
					.getIntegerDomainAxisTick(), aAxes.getLogarithmicDomainAxis(), aAxes
					.getDomainRange(), aDataRange);
		}
		return createAxis(convertLabel(aAxes.getRangeAxisLabel()), aAxes.getIntegerRangeAxisTick(),
				aAxes.getLogarithmicRangeAxis(), aAxes.getRangeRange(), aDataRange);
	}

	/**
	 * Creates either a domain- or a range-axis according to its <code>AxesSettings</code>
	 * 
	 * @param aLabel
	 *            Axis label.
	 * @param aTick
	 *            Flag indicating if IntegerTicks are to be created.
	 * @param aLog
	 *            Flag indicating if axis must be logarithmic.
	 * @param aRange
	 *            Suggested data as specified in the settings. Currently ignored.
	 * @param aDataRange
	 *            Range of the data, as computed from the point coordinates.
	 * @return Newly initialized <code>NumberAxis</code>.
	 */
	private static NumberAxis createAxis(String aLabel, boolean aTick, boolean aLog,
			IntRange aRange, Range aDataRange) {
		NumberAxis axis = null;
		if (aLog) {
			MyLogarithmicAxis logAxis = new MyLogarithmicAxis(aLabel, aDataRange);
			// LogarithmicAxis logAxis = new LogarithmicAxis(aLabel);
			axis = logAxis;
			logAxis.autoAdjustRange();
			logAxis.setAllowNegativesFlag(true);
			// logAxis.setAutoTickUnitSelection(false);
			logAxis.setAutoRange(false);
		} else {
			axis = new NumberAxis(aLabel);
			if (aTick)
				axis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		}
		return axis;
	}

	/**
	 * Creates a scatter plot that visualizes the given data collection.
	 * 
	 * @param aCollection
	 *            Data to be visualized.
	 * @param aGeneral
	 *            General visual settings to be applied.
	 * @param aAxes
	 *            Axis-related visual settings to be applied.
	 * @param aGrid
	 *            Grid-related visual settings to be applied.
	 * @param aScatter
	 *            Point-related visual settings to be applied.
	 * @return Newly created chart control.
	 */
	private static JFreeChart createScatter(XYSeriesCollection aCollection,
			GeneralVisSettings aGeneral, AxesSettings aAxes, GridSettings aGrid,
			ScatterSettings aScatter) {

		JFreeChart chart = ChartFactory.createScatterPlot(null, // title
				convertLabel(aAxes.getDomainAxisLabel()), // label of X axis
				convertLabel(aAxes.getRangeAxisLabel()), // label of Y axis
				aCollection, // dataset
				PlotOrientation.VERTICAL, // orientation
				false, // create legend
				true, // display tooltips
				false); // generate urls
		XYPlot plot = chart.getXYPlot();
		Range domainDataRange = aAxes.getLogarithmicDomainAxis() ? new Range(logLowerBound(plot
				.getDataset(), true), plot.getDataRange(plot.getDomainAxis()).getUpperBound())
				: plot.getDataRange(plot.getDomainAxis());
		Range rangeDataRange = aAxes.getLogarithmicRangeAxis() ? new Range(logLowerBound(plot
				.getDataset(), false), plot.getDataRange(plot.getRangeAxis()).getUpperBound())
				: plot.getDataRange(plot.getRangeAxis());
		updateGeneral(plot, aGeneral);
		updateAxes(plot, aAxes, aGrid, domainDataRange, rangeDataRange);
		updateScatter(plot, aScatter);
		chart.setBackgroundPaint(null);
		return chart;
	}

	/**
	 * Converts the given integer histogram data to a data collection.
	 * 
	 * @param aHistogram
	 *            IntHistgogram data to be converted.
	 * @return Data collection to be used in the creation of a chart.
	 */
	private static XYSeriesCollection fromIntHistogram(IntHistogram aHistogram) {
		int[][] bars = aHistogram.getBins();
		XYSeries dataSeries = new XYSeries("", true, true);
		for (int i = 0; i < bars[0].length; ++i) {
			dataSeries.add(bars[0][i], bars[1][i], false);
		}
		return new XYSeriesCollection(dataSeries);
	}

	/**
	 * Converts the given long histogram data to a data collection.
	 * 
	 * @param aHistogram
	 *            LongHistgogram data to be converted.
	 * @return Data collection to be used in the creation of a chart.
	 */
	private static XYSeriesCollection fromLongHistogram(LongHistogram aHistogram) {
		long[][] bars = aHistogram.getBins();
		XYSeries dataSeries = new XYSeries("", true, true);
		for (int i = 0; i < bars[0].length; ++i) {
			dataSeries.add(bars[0][i], bars[1][i], false);
		}
		return new XYSeriesCollection(dataSeries);
	}

	/**
	 * Converts the given point set to a data collection.
	 * 
	 * @param aPoints
	 *            Set of points to be converted.
	 * @return Data collection to be used in the creation of a chart.
	 */
	private static XYSeriesCollection fromPoints2D(Points2D aPoints) {
		Point2D.Double[] points = aPoints.getPoints();
		XYSeries dataSeries = new XYSeries("", true, true);
		for (int i = 0; i < points.length; ++i) {
			dataSeries.add(points[i].x, points[i].y, false);
		}
		return new XYSeriesCollection(dataSeries);
	}

	/**
	 * Updates the axis-related properties of a plot.
	 * 
	 * @param aPlot
	 *            Plot to be updated.
	 * @param aAxes
	 *            Axis-related visual settings to be applied.
	 * @param aGrid
	 *            Grid-related visual settings to be applied.
	 */
	private static void updateAxes(XYPlot aPlot, AxesSettings aAxes, GridSettings aGrid,
			Range aDomainDataRange, Range aRangeDataRange) {

		aPlot.setDomainAxis(createAxis(aAxes, true, aDomainDataRange));
		aPlot.setRangeAxis(createAxis(aAxes, false, aRangeDataRange));

		aPlot.setDomainGridlinesVisible(aGrid.getVerticalGridLines()); // set gridlines for X axis
		aPlot.setDomainGridlinePaint(aGrid.getGridLinesColor()); // set color of domain gridline

		aPlot.setRangeGridlinesVisible(aGrid.getHorizontalGridLines()); // set gridlines for Y axis
		aPlot.setRangeGridlinePaint(aGrid.getGridLinesColor()); // set color of range gridline
	}

	/**
	 * Updates the bar properties of a plot.
	 * 
	 * @param aPlot
	 *            Plot to be updated.
	 * @param aBars
	 *            Bar visual settings to be applied.
	 */
	private static void updateBars(XYPlot aPlot, BarsSettings aBars) {
		XYBarRenderer renderer = (XYBarRenderer) aPlot.getRenderer();
		renderer.setSeriesPaint(0, aBars.getBarColor());
	}

	/**
	 * Updates the general properties of a plot.
	 * 
	 * @param aPlot
	 *            Plot to be updated.
	 * @param aGeneral
	 *            General visual settings to be applied.
	 */
	private static void updateGeneral(XYPlot aPlot, GeneralVisSettings aGeneral) {
		aPlot.setBackgroundPaint(aGeneral.getBgColor());
	}

	/**
	 * Updates the point-related properties of a plot.
	 * 
	 * @param aPlot
	 *            Plot to be updated.
	 * @param aScatter
	 *            Visual settings to be applied.
	 */
	private static void updateScatter(XYPlot aPlot, ScatterSettings aScatter) {
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) aPlot.getRenderer();
		renderer.setSeriesPaint(0, aScatter.getPointColor());
		final Rectangle2D ds = AbstractRenderer.DEFAULT_SHAPE.getBounds2D();
		final double x = ds.getX();
		final double y = ds.getY();
		final double w = ds.getWidth();
		final double h = ds.getHeight();
		Shape shape = null;
		switch (aScatter.getPointShape()) {
			case POINT:
				shape = new Rectangle2D.Double(x + w / 2, y + h / 2, 1, 1);
				renderer.setBaseShapesFilled(true);
				break;
			case CIRCLE:
				shape = new Ellipse2D.Double(x, y, w, h);
				renderer.setBaseShapesFilled(false);
				break;
			case FILLED_CIRCLE:
				shape = new Ellipse2D.Double(x, y, w, h);
				renderer.setBaseShapesFilled(true);
				break;
			case SQUARE:
				shape = new Rectangle2D.Double(x, y, w, h);
				renderer.setBaseShapesFilled(false);
				break;
			case FILLED_SQUARE:
				shape = new Rectangle2D.Double(x, y, w, h);
				renderer.setBaseShapesFilled(true);
				break;
			case CROSS:
				shape = new Cross(x, y, w, h);
				renderer.setBaseShapesFilled(false);
		}
		renderer.setSeriesShape(0, shape);
	}
}
