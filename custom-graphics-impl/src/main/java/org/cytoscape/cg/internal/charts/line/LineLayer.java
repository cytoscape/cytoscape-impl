package org.cytoscape.cg.internal.charts.line;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.util.List;
import java.util.Map;

import org.cytoscape.cg.internal.charts.AbstractChartLayer;
import org.cytoscape.cg.internal.charts.CustomCategoryItemLabelGenerator;
import org.cytoscape.cg.internal.charts.LabelPosition;
import org.cytoscape.cg.internal.util.EqualDashStroke;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleInsets;

public class LineLayer extends AbstractChartLayer<CategoryDataset> {
	
	private final boolean showRangeZeroBaseline;
	private final float lineWidth;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public LineLayer(
			Map<String/* series */, List<Double>/* values */> data, 
			List<String> itemLabels,
			List<String> domainLabels, 
			List<String> rangeLabels, 
			boolean showItemLabels, 
			boolean showDomainAxis,
			boolean showRangeAxis, 
			boolean showRangeZeroBaseline, 
			float itemFontSize, 
			LabelPosition domainLabelPosition,
			List<Color> colors, 
			float axisWidth, 
			Color axisColor, 
			float axisFontSize, 
			List<Double> range,
			float lineWidth
	) {
        super(data, itemLabels, domainLabels, rangeLabels, showItemLabels, showDomainAxis, showRangeAxis, itemFontSize,
        		domainLabelPosition, colors, axisWidth, axisColor, axisFontSize, 0.0f, TRANSPARENT_COLOR, range);
        this.showRangeZeroBaseline = showRangeZeroBaseline;
        this.lineWidth = lineWidth >= 0 ? lineWidth : 1.0f;
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected CategoryDataset createDataset() {
		return createCategoryDataset(data, true, domainLabels);
	}
    
	@Override
	protected JFreeChart createChart(CategoryDataset dataset) {
		var chart = ChartFactory.createLineChart(
				null, // chart title
				null, // domain axis label
				null, // range axis label
				dataset, // data
				PlotOrientation.VERTICAL,
				false, // include legend
				false, // tooltips
				false // urls
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
		
		var axisStroke = new BasicStroke(axisWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		var gridLineStroke = new BasicStroke(axisWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0.5f,
				new float[] { 0.5f }, 0.0f);
		
		plot.setRangeGridlineStroke(gridLineStroke);
		
		var domainAxis = (CategoryAxis) plot.getDomainAxis();
        domainAxis.setVisible(showDomainAxis);
        domainAxis.setAxisLineStroke(axisStroke);
        domainAxis.setAxisLinePaint(axisColor);
        domainAxis.setTickMarksVisible(true);
        domainAxis.setTickMarkStroke(axisStroke);
        domainAxis.setTickMarkPaint(axisColor);
        domainAxis.setTickLabelsVisible(true);
        domainAxis.setTickLabelFont(domainAxis.getTickLabelFont().deriveFont(axisFontSize).deriveFont(Font.PLAIN));
        domainAxis.setTickLabelPaint(axisColor);
        domainAxis.setCategoryLabelPositions(getCategoryLabelPosition());
        domainAxis.setCategoryMargin(.1);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);
        
        var rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setVisible(showRangeAxis);
		rangeAxis.setAxisLineStroke(axisStroke);
		rangeAxis.setAxisLinePaint(axisColor);
		rangeAxis.setTickMarkStroke(axisStroke);
		rangeAxis.setTickMarkPaint(axisColor);
		rangeAxis.setTickLabelFont(rangeAxis.getLabelFont().deriveFont(axisFontSize).deriveFont(Font.PLAIN));
		rangeAxis.setTickLabelPaint(axisColor);
		rangeAxis.setLowerMargin(0.0);
		rangeAxis.setUpperMargin(0.0);
        
		// Set axis range		
		if (range != null && range.size() >= 2 && range.get(0) != null && range.get(1) != null) {
			rangeAxis.setLowerBound(range.get(0) * 1.1);
			rangeAxis.setUpperBound(range.get(1) * 1.1);
		}
		
		var renderer = (LineAndShapeRenderer) plot.getRenderer();
		renderer.setBaseItemLabelGenerator(showItemLabels ? new CustomCategoryItemLabelGenerator(itemLabels) : null);
		renderer.setBaseItemLabelsVisible(showItemLabels);
		renderer.setBaseItemLabelFont(renderer.getBaseItemLabelFont().deriveFont(itemFontSize));
		renderer.setBaseItemLabelPaint(labelColor);
		
		var seriesStroke = new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		
		var keys = dataset.getRowKeys();
		
		if (colors != null && colors.size() >= keys.size()) {
			for (int i = 0; i < keys.size(); i++) {
				var c = colors.get(i);
				renderer.setSeriesPaint(i, c);
				renderer.setSeriesStroke(i, seriesStroke);
			}
		}
		
		return chart;
	}
}
