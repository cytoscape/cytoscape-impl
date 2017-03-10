package org.cytoscape.ding.internal.charts.heatmap;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cytoscape.ding.customgraphics.Orientation;
import org.cytoscape.ding.internal.charts.AbstractChartLayer;
import org.cytoscape.ding.internal.charts.LabelPosition;
import org.cytoscape.ding.internal.charts.util.ColorScale;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleInsets;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class HeatMapLayer extends AbstractChartLayer<XYZDataset> {
	
	private final Orientation orientation;
	private final String[] xLabels;
	private final String[] yLabels;
	private int maxYSize;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public HeatMapLayer(final Map<String/*key*/, List<Double>/*z-values*/> data,
						final List<String> itemLabels,
						final List<String> domainLabels,
						final List<String> rangeLabels,
						final boolean showDomainAxis,
						final boolean showRangeAxis,
						final LabelPosition domainLabelPosition,
						final List<Color> colors,
						final Color axisColor,
						final float axisFontSize,
						final List<Double> range,
						final Orientation orientation,
						final Rectangle2D bounds) {
		super(data, itemLabels, domainLabels, rangeLabels, false, showDomainAxis, showRangeAxis, 0.0f,
				domainLabelPosition, colors, 0.0f, axisColor, axisFontSize, 0.0f, TRANSPARENT_COLOR, range, bounds);
        this.orientation = orientation;
        
        // Range cannot be null
        if (this.range == null)
        	this.range = calculateRange(data.values(), false);
        
        // x/y Labels are mandatory
        xLabels = new String[data.size()];
        int x = 0;
        
        for (final Entry<String, List<Double>> series : data.entrySet()) {
        	final String k = series.getKey();
			final List<Double> zValues = series.getValue();
			
			maxYSize = Math.max(maxYSize, zValues.size());
			xLabels[x] = (domainLabels != null && domainLabels.size() > x) ? domainLabels.get(x) : k;
			x++;
        }
        
        yLabels = new String[maxYSize];
        
        for (int i = 0; i < maxYSize; i++) {
        	yLabels[i] = (rangeLabels != null && rangeLabels.size() > i) ? rangeLabels.get(i) : ""+(i+1);
        }
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected XYZDataset createDataset() {
		final DefaultXYZDataset dataset = new DefaultXYZDataset();
		int x = 0;
		
		for (final Entry<String, List<Double>> series : data.entrySet()) {
			final String k = series.getKey();
			final List<Double> zValues = series.getValue();
			
			// Must be an array with length 3, containing three arrays of equal length,
			// - the first containing the x-values,
			// - the second containing the y-values
			// - and the third containing the z-values
			// { { x1, x2 }, { y1, y2 }, { z1, z2 } }
			
			final double[][] seriesData = new double[3][maxYSize];
			Arrays.fill(seriesData[0], x);
			
			for (int y = 0; y < maxYSize; y++) {
				double z = Double.NaN;
				
				if (zValues.size() > y && zValues.get(y) != null)
					 z = zValues.get(y);
				
				seriesData[1][y] = y;
				seriesData[2][y] = z;
			}
			
			dataset.addSeries(k, seriesData);
			x++;
		}
		
		return dataset;
	}
    
	@Override
	protected JFreeChart createChart(final XYZDataset dataset) {
		final PlotOrientation plotOrientation = 
				orientation == Orientation.HORIZONTAL ? PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL;
		
		final SymbolAxis xAxis = new SymbolAxis(null, xLabels);
		xAxis.setVisible(showDomainAxis);
		xAxis.setAxisLineVisible(false);
		xAxis.setTickMarksVisible(false);
		xAxis.setTickLabelFont(xAxis.getLabelFont().deriveFont(axisFontSize));
		xAxis.setTickLabelPaint(axisColor);
		xAxis.setVerticalTickLabels(domainLabelPosition != LabelPosition.STANDARD);
		xAxis.setLowerMargin(0.0);
		xAxis.setUpperMargin(0.0);

		final SymbolAxis yAxis = new SymbolAxis(null, yLabels);
		yAxis.setVisible(showRangeAxis);
		yAxis.setAxisLineVisible(false);
		yAxis.setTickMarksVisible(false);
		yAxis.setTickLabelFont(yAxis.getLabelFont().deriveFont(axisFontSize));
		yAxis.setTickLabelPaint(axisColor);
		yAxis.setLowerMargin(0.0);
		yAxis.setUpperMargin(0.0);
		yAxis.setInverted(true);

		final XYBlockRenderer renderer = new XYBlockRenderer();
		
		if (range != null && range.size() >= 2 && range.get(0) != null && range.get(1) != null) {
			final int colorsSize = colors != null ? colors.size() : 0;
			
			Color upperColor = colorsSize > 0 ? colors.get(0) : Color.BLUE;
			Color zeroColor  = colorsSize > 1 ? colors.get(1) : Color.WHITE;
			Color lowerColor = colorsSize > 2 ? colors.get(2) : Color.RED;
			Color nanColor   = colorsSize > 3 ? colors.get(3) : Color.GRAY;
			
			final ColorScale scale = new ColorScale(range.get(0), range.get(1), lowerColor, zeroColor, upperColor, nanColor);
			renderer.setPaintScale(scale);
		}

		final XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
		plot.setDomainAxis(xAxis);
        plot.setDomainAxisLocation(AxisLocation.TOP_OR_LEFT);
        plot.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);
        plot.setOutlineVisible(false);
		plot.setDomainGridlinesVisible(false);
		plot.setRangeGridlinesVisible(false);
		plot.setBackgroundPaint(TRANSPARENT_COLOR);
		plot.setInsets(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
		plot.setAxisOffset(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
		plot.setOrientation(plotOrientation);

		final JFreeChart chart = new JFreeChart(null, plot);
		chart.removeLegend();
		chart.setBorderVisible(false);
		chart.setBackgroundPaint(TRANSPARENT_COLOR);
		chart.setPadding(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
		
		return chart;
	}
}
