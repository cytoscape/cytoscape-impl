package org.cytoscape.cg.internal.charts.box;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.cytoscape.cg.internal.charts.AbstractChartLayer;
import org.cytoscape.cg.internal.charts.LabelPosition;
import org.cytoscape.cg.internal.util.EqualDashStroke;
import org.cytoscape.cg.model.Orientation;
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

public class BoxLayer extends AbstractChartLayer<BoxAndWhiskerCategoryDataset> {
	
	private final boolean showRangeZeroBaseline;
	private final Orientation orientation;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	@SuppressWarnings("unchecked")
	public BoxLayer(Map<String/*series*/, List<Double>/*values*/> data,
					boolean showRangeAxis,
					boolean showRangeZeroBaseline,
					List<Color> colors,
					float axisWidth,
					Color axisColor,
					float axisFontSize,
					float borderWidth,
					Color borderColor,
					List<Double> range,
					Orientation orientation,
					Rectangle2D bounds) {
        super(data, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, false, false,
				showRangeAxis, 0.0f, LabelPosition.STANDARD, colors, axisWidth, axisColor, axisFontSize,
				borderWidth, borderColor, range, bounds);
        this.showRangeZeroBaseline = showRangeZeroBaseline;
        this.orientation = orientation;
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected BoxAndWhiskerCategoryDataset createDataset() {
		var dataset = new DefaultBoxAndWhiskerCategoryDataset();
		
		for (String series : data.keySet()) {
			var values = data.get(series);
			dataset.add(values, series, "1"); // switch series and category name so labels are displayed for series
		}
		
		return dataset;
	}
    
	@Override
	protected JFreeChart createChart(BoxAndWhiskerCategoryDataset dataset) {
		var chart = ChartFactory.createBoxAndWhiskerChart(
					null, // chart title
					null, // domain axis label
					null, // range axis label
					dataset, // data
					false // include legend
		);
		
        chart.setAntiAlias(true);
        chart.setBorderVisible(false);
        chart.setBackgroundPaint(TRANSPARENT_COLOR);
        chart.setBackgroundImageAlpha(0.0f);
        chart.setPadding(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
        
        var plot = (CategoryPlot) chart.getPlot();
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
		
		var plotOrientation = 
				orientation == Orientation.HORIZONTAL ? PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL;
		plot.setOrientation(plotOrientation);
		
		var axisStroke = new BasicStroke(axisWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		
		var domainAxis = (CategoryAxis) plot.getDomainAxis();
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
        
		var rangeAxis = (NumberAxis) plot.getRangeAxis();
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
		
		var renderer = (BoxAndWhiskerRenderer) plot.getRenderer();
		renderer.setFillBox(true);
		renderer.setMeanVisible(false);
		renderer.setBaseItemLabelsVisible(false); // Box chart does not support item labels, anyway
		
		var stroke = new BasicStroke(borderWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		
		var keys = dataset.getRowKeys();
		
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
