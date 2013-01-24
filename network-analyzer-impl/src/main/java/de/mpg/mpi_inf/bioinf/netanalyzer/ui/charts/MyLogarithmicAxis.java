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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.axis.Tick;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

/**
 * This class is based on the original <code>com.jrefinery.chart.axlibrary</code> with additional functions.
 * It supports correct logarithmic scaling and ticks for numbers &lt; 1.
 * 
 * @see <a
 *      href="http://www.jfree.org/jfreechart/javadoc/org/jfree/chart/axis/LogarithmicAxis.html">com.jrefinery.chart.axis.LogarithmicAxis</a>
 * 
 * @author Sven-Eric Schelhorn
 */
public class MyLogarithmicAxis extends LogarithmicAxis {

	public static final double LOGBASE = 10.0;

	protected Range dataRange;

	protected int offset = 0;

	protected final double[] allticks = { 0.0, 0.0001, 0.001, 0.01, 0.1, 1.0, 10.0, 100.0, 1000.0, 10000.0,
			100000.0, 1000000.0, 10000000.0, 100000000.0, 1000000000.0, 10000000000.0, 100000000000.0,
			1000000000000.0, 10000000000000.0, 100000000000000.0, 1000000000000000.0, 10000000000000000.0,
			100000000000000000.0, 1000000000000000000.0, 10000000000000000000.0 };

	/**
	 * Constructor. Constructs a VerticalLogarithmicAxis with default log base of 10 a default tick label
	 * factor of 1.
	 * 
	 * @param label the axis label (null permitted).
	 */
	public MyLogarithmicAxis(String label) {
		super(label);
		this.allowNegativesFlag = false;
	}

	/**
	 * Constructor.
	 * 
	 * @param label Axis label or null.
	 * @param aDataRange range of the data.
	 */
	public MyLogarithmicAxis(String label, Range aDataRange) {
		this(label);
		setDataRange(aDataRange);
		this.offset = computeOffset(aDataRange);
	}

	/**
	 * Computes the offset for the method {@link #adjustedLog(double)} to display the logarithmic scale
	 * correctly.
	 * 
	 * @param aDataRange Range of the data.
	 * @return Integer denoting the correct offset for the data.
	 */
	protected int computeOffset(Range aDataRange) {
		final double lowerBound = aDataRange.getLowerBound();
		for (int i = 1; i < allticks.length; ++i) {
			if (lowerBound <= allticks[i]) {
				return 1 - i;
			}
		}
		return 0;
	}

	/**
	 * Sets the data range of the data to be shown. A range value of zero has to be excluded before to avoid
	 * strange behavior of the log axis.
	 * 
	 * @param aDataRange the range.
	 */
	public void setDataRange(Range aDataRange) {
		dataRange = aDataRange;
	}

	/**
	 * Overridden version that calls original and then sets up flag for log axis processing.
	 * 
	 * @param range the range.
	 */
	@Override
	public void setRange(Range range) {
		super.setRange(range, true, true);
		setupSmallLogFlag(); // setup flag based on bounds values
	}

	/**
	 * Sets up flag for log axis processing.
	 */
	@Override
	protected void setupSmallLogFlag() {
		// set flag true if negative values not allowed and the
		// lower bound is between 0 and logBase:
		final double lowerVal = getRange().getLowerBound();
		smallLogFlag = (!allowNegativesFlag && lowerVal < LOGBASE && lowerVal > 0.0);
	}

	/**
	 * Re-scales the axis to ensure that all data is visible.
	 */
	@Override
	public void autoAdjustRange() {
		setupSmallLogFlag(); // setup flag based on bounds values
		double lower = computeLogFloor(dataRange.getLowerBound());
		double upper = computeCeiling(dataRange.getUpperBound());
		if (lower > upper) {
			lower = upper;
		}
		setRange(new Range(lower, upper));
	}

	/**
	 * Zooms in on the current range (currently not implemented).
	 * 
	 * @param lowerPercent the new lower bound.
	 * @param upperPercent the new upper bound.
	 */
	@Override
	public void zoomRange(double lowerPercent, double upperPercent) {
		// Zoom into logarithmic axis is disabled.
	}

	/**
	 * Returns the smallest (closest to negative infinity) double value that is not less than the argument, is
	 * equal to a mathematical integer and satisfying the condition that log base of the value is an integer
	 * (i.e., the value returned will be a power of 10 for logBase=10: 1, 10, 100, 1000, etc.).
	 * 
	 * @param upper a double value above which a ceiling will be calculated.
	 * @return logBase<sup>N</sup> with N .. { 1 ... }
	 */
	@Override
	protected double computeLogCeil(double upper) {
		double logCeil = allticks[allticks.length - 1];

		for (int i = allticks.length - 1; i >= 0; i--) {
			if (allticks[i] >= upper)
				logCeil = allticks[i];
		}
		return logCeil;
	}

	// TODO: Add Javadoc
	private double computeCeiling(double aUpper) {
		double ceiling = 1;
		int i = allticks.length - 1;
		while (i >= 0 && allticks[i] >= aUpper) {
			ceiling = allticks[i];
			i--;
		}
		if (i > 0 && ceiling > 1) {
			double step = (allticks[i] != 0) ? allticks[i] : allticks[i + 1] / 10;
			double c = 2 * step;
			for (int j = 2; j < 10; ++j, c += step) {
				if (c >= aUpper) {
					ceiling = c;
					break;
				}
			}
		}
		return ceiling;
	}

	/**
	 * Returns the largest (closest to positive infinity) double value that is not greater than the argument,
	 * is equal to a mathematical integer and satisfying the condition that log base of the value is an
	 * integer (i.e., the value returned will be a power of 10 for logBase=10: 1, 10, 100, 1000, etc.).
	 * 
	 * @param lower a double value below which a floor will be calculated.
	 * @return logBase<sup>N</sup> with N .. { 1 ... }
	 */
	@Override
	protected double computeLogFloor(double lower) {
		double logFloor = allticks[1];

		for (int i = 1; i < allticks.length; i++) {
			if (allticks[i] <= lower)
				logFloor = allticks[i];
		}
		return logFloor;
	}

	/**
	 * Main change of Logarithmic class that overwrites super method. Calculates the values of the
	 * System.out.println("Upper = " + aUpper + "\tCeiling = " + ceiling); tick labels for the axis, storing
	 * the results in the tick label list (ready for drawing).
	 * 
	 * @param g2 the graphics device.
	 * @param dataArea the area in which the plot should be drawn.
	 * @param edge Rectangle edge for axis location.
	 * @return A list of ticks.
	 */
	@Override
	public List<?> refreshTicksVertical(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {

		// Ticklist that has to be filled with sensible values
		List<Tick> ticks = new ArrayList<Tick>();

		// Formatting of the tick labels
		TextAnchor anchor = null;
		TextAnchor rotationAnchor = null;
		double angle = 0.0;
		if (isVerticalTickLabels()) {
			if (edge == RectangleEdge.LEFT) {
				anchor = TextAnchor.BOTTOM_CENTER;
				rotationAnchor = TextAnchor.BOTTOM_CENTER;
				angle = -Math.PI / 2.0;
			} else {
				anchor = TextAnchor.BOTTOM_CENTER;
				rotationAnchor = TextAnchor.BOTTOM_CENTER;
				angle = Math.PI / 2.0;
			}
		} else {
			if (edge == RectangleEdge.LEFT) {
				anchor = TextAnchor.CENTER_RIGHT;
				rotationAnchor = TextAnchor.CENTER_RIGHT;
			} else {
				anchor = TextAnchor.TOP_CENTER;
				rotationAnchor = TextAnchor.TOP_CENTER;
			}
		}

		List<Double> myticks = new ArrayList<Double>();

		double lowerBound = dataRange.getLowerBound();
		double upperBound = dataRange.getUpperBound();

		// Select minimal tick set that fully contains the data range
		for (int i = 0; i < allticks.length - 1; i++) {
			if (allticks[i + 1] > lowerBound && allticks[i] <= upperBound) {
				myticks.add(new Double(allticks[i]));
			}
		}

		boolean isLastTickAdded = false;
		final boolean sci = (myticks.size() > 4);

		// Add all ticks that were selected before and their eight following unlabeled subticks
		for (final Double tick : myticks) {
			final double t = tick.doubleValue();
			ticks.add(new NumberTick(tick, toString(t, sci), anchor, rotationAnchor, angle));
			if (t < upperBound) {
				for (int j = 2; j < 10; j++) {
					double s = (t == 0.0 ? allticks[1] / 10.0 : t) * j; // to allows subticks from 0
					String label = "";
					if (s >= upperBound) {
						isLastTickAdded = true;
						label = toString(s, sci);
					}
					ticks.add(new NumberTick(new Double(s), label, anchor, rotationAnchor, angle));
					if (isLastTickAdded) {
						break;
					}
				}
			}
		}

		// Add the very last tick value
		if (!isLastTickAdded) {
			Double t = new Double(computeLogCeil(upperBound));
			ticks.add(new NumberTick(t, toString(t, sci), anchor, rotationAnchor, angle));
		}

		return ticks;
	}

	/**
	 * Main change of Logarithmic class that overwrites super method. Calculates the positions of the tick
	 * labels for the axis, storing the results in the tick label list (ready for drawing).
	 * 
	 * @param g2 the graphics device.
	 * @param dataArea the area in which the plot should be drawn.
	 * @param edge Rectangle edge for axis location.
	 * @return A list of ticks.
	 */
	@Override
	public List<?> refreshTicksHorizontal(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
		return refreshTicksVertical(g2, dataArea, edge);
	}

	/**
	 * Returns the log value, depending on if values between 0 and 1 are being plotted.
	 * 
	 * @param val the value.
	 * 
	 * @return adjusted log value
	 */
	protected double switchedLog(double val) {
		return adjustedLog(val);
	}

	/**
	 * Returns the log value, depending on if values between 0 and 1 are being plotted.
	 * 
	 * @param val the value.
	 * 
	 * @return adjusted log value
	 */
	@Override
	protected double switchedLog10(double val) {
		return adjustedLog(val);
	}

	/**
	 * Returns an adjusted log value for graphing purposes. The first adjustment is that negative values are
	 * changed to positive during the calculations, and then the answer is negated at the end. The second is
	 * that, for values less than the log base, an increasingly large (0 to 1) scaling factor is added such
	 * that at 0 the value is adjusted to 1, resulting in a returned result of 0.
	 * 
	 * @param val the value.
	 * @return the adjusted value.
	 */
	public double adjustedLog(double val) {
		double value = 0.0;
		if (val == 0.0)
			value = 0.0;
		else {
			for (int i = 1; i < allticks.length; ++i) {
				if (val <= allticks[i]) {
					value = log(LOGBASE, ((val * 10) / allticks[i])) + offset + i - 1;
				}
			}
		}
		return value;
	}

	/**
	 * Compute logarithm of <i>power</i> with base <i>base</i>.
	 * 
	 * @param base Base for logarithm.
	 * @param power Value to compute logarithm of.
	 * @return Exponent. The logarithm with base <i>base</i> of <i>power</i>.
	 * @see <a href="http://www.math.utah.edu/~alfeld/math/log.html">What on Earth is a Logarithm?</a>
	 */
	protected double log(double base, double power) {
		return Math.log(power) / Math.log(base);
	}

	/**
	 * Gives the <code>String</code> representation of the given number.
	 * 
	 * @param aValue Number to be converted to <code>String</code>.
	 * @param aScientific Flag indicating if scientific notation is preferred.
	 * @return String representation of <code>aValue</code>.
	 */
	private static String toString(Double aValue, boolean aScientific) {
		return toString(aValue.doubleValue(), aScientific);
	}

	/**
	 * Gives the <code>String</code> representation of the given number.
	 * 
	 * @param aValue Number to be converted to <code>String</code>.
	 * @param aScientific Flag indicating if scientific notation is preferred.
	 * @return String representation of <code>aValue</code>.
	 */
	private static String toString(double aValue, boolean aScientific) {
		final int intValue = (int) aValue;
		if (intValue == aValue) {
			if (intValue == 0 || intValue == 1 || aScientific == false) {
				return String.valueOf(intValue);
			}
		}
		if (aScientific) {
			return new DecimalFormat("0.#####E0#").format(aValue);
		}
		return String.valueOf(aValue);
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = -3172819531427016894L;
}
