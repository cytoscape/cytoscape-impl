package org.cytoscape.ding.internal.charts.box;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.cytoscape.ding.customgraphics.Orientation;
import org.cytoscape.ding.impl.strokes.EqualDashStroke;
import org.cytoscape.ding.internal.charts.AbstractChartLayer;
import org.cytoscape.ding.internal.charts.LabelPosition;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
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

public class BoxLayer extends AbstractChartLayer<BoxAndWhiskerCategoryDataset> {
	
	private final boolean showRangeZeroBaseline;
	private final Orientation orientation;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	@SuppressWarnings("unchecked")
	public BoxLayer(final Map<String/*series*/, List<Double>/*values*/> data,
					final boolean showRangeAxis,
					final boolean showRangeZeroBaseline,
					final List<Color> colors,
					final float axisWidth,
					final Color axisColor,
					final float axisFontSize,
					final float borderWidth,
					final Color borderColor,
					final List<Double> range,
					final Orientation orientation,
					final Rectangle2D bounds) {
        super(data, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, false, false,
				showRangeAxis, 0.0f, LabelPosition.STANDARD, colors, axisWidth, axisColor, axisFontSize,
				borderWidth, borderColor, range, bounds);
        this.showRangeZeroBaseline = showRangeZeroBaseline;
        this.orientation = orientation;
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected BoxAndWhiskerCategoryDataset createDataset() {
		final DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
		
		for (String series : data.keySet()) {
			final List<Double> values = data.get(series);
			dataset.add(values, series, "1"); // switch series and category name so labels are displayed for series
		}
		
		return dataset;
	}
    
	@Override
	protected JFreeChart createChart(final BoxAndWhiskerCategoryDataset dataset) {
		final JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(
					null, // chart title
					null, // domain axis label
					null, // range axis label
					dataset, // data
					false); // include legend
		
        chart.setAntiAlias(true);
        chart.setBorderVisible(false);
        chart.setBackgroundPaint(TRANSPARENT_COLOR);
        chart.setBackgroundImageAlpha(0.0f);
        chart.setPadding(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
        
        final CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setOutlineVisible(false);
		plot.setInsets(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
		plot.setAxisOffset(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
		plot.setDomainGridlinesVisible(false);
	    plot.setRangeGridlinesVisible(false);
		plot.setBackgroundPaint(TRANSPARENT_COLOR);
		plot.setBackgroundAlpha(0.0f);
		plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
		
		if (showRangeZeroBaseline) {
			plot.setRangeZeroBaselineVisible(true);
			plot.setRangeZeroBaselinePaint(axisColor);
			plot.setRangeZeroBaselineStroke(new EqualDashStroke(axisWidth));
		}
		
		final PlotOrientation plotOrientation = 
				orientation == Orientation.HORIZONTAL ? PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL;
		plot.setOrientation(plotOrientation);
		
		final BasicStroke axisStroke = new BasicStroke(axisWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		
		final CategoryAxis domainAxis = (CategoryAxis) plot.getDomainAxis();
        domainAxis.setVisible(showDomainAxis);
        domainAxis.setAxisLineStroke(axisStroke);
        domainAxis.setAxisLinePaint(axisColor);
        domainAxis.setTickMarksVisible(false);
        domainAxis.setTickMarkStroke(axisStroke);
        domainAxis.setTickMarkPaint(axisColor);
        domainAxis.setTickLabelsVisible(false);
        domainAxis.setCategoryMargin(.1);
        domainAxis.setLowerMargin(.025);
        domainAxis.setUpperMargin(.025);
        
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setVisible(showRangeAxis);
		rangeAxis.setAxisLineStroke(axisStroke);
		rangeAxis.setAxisLinePaint(axisColor);
		rangeAxis.setTickMarkStroke(axisStroke);
		rangeAxis.setTickMarkPaint(axisColor);
		rangeAxis.setTickLabelFont(rangeAxis.getLabelFont().deriveFont(axisFontSize).deriveFont(Font.PLAIN));
		rangeAxis.setTickLabelPaint(axisColor);
		rangeAxis.setLowerMargin(0.1);
		rangeAxis.setUpperMargin(0.1);
        
		// Set axis range		
		if (range != null && range.size() >= 2 && range.get(0) != null && range.get(1) != null) {
			rangeAxis.setLowerBound(range.get(0));
			rangeAxis.setUpperBound(range.get(1));
		}
		
		final BoxAndWhiskerRenderer renderer = (BoxAndWhiskerRenderer) plot.getRenderer();
		renderer.setFillBox(true);
		renderer.setMeanVisible(false);
		renderer.setBaseItemLabelsVisible(false); // Box chart does not support item labels, anyway
		
		final BasicStroke stroke = new BasicStroke(borderWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		
		final List<?> keys = dataset.getRowKeys();
		
		for (int i = 0; i < keys.size(); i++) {
			renderer.setSeriesStroke(i, stroke);
			renderer.setSeriesOutlineStroke(i, stroke);
			renderer.setSeriesOutlinePaint(i, borderWidth > 0 ? borderColor : TRANSPARENT_COLOR);
			
			if (colors != null && colors.size() >= keys.size())
				renderer.setSeriesPaint(i, colors.get(i));
		}
		
		return chart;
	}
}
