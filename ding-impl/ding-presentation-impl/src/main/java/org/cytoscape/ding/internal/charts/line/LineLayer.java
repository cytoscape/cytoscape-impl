package org.cytoscape.ding.internal.charts.line;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;

import org.cytoscape.ding.internal.charts.AbstractChartLayer;
import org.cytoscape.ding.internal.charts.ViewUtils.DoubleRange;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;


public class LineLayer extends AbstractChartLayer<CategoryDataset> {
	
	private final boolean showCategoryAxis;
	private final boolean showRangeAxis;
	private final int lineWidth;

	public LineLayer(final Map<String/*series*/, List<Double>/*values*/> data,
					final List<String> labels,
					final boolean showLabels,
					final List<Color> colors,
					final DoubleRange range,
					final boolean showCategoryAxis,
					final boolean showRangeAxis,
					final int lineWidth,
					final Rectangle2D bounds) {
        super(data, labels, showLabels, colors, range, bounds);
        this.showCategoryAxis = showCategoryAxis;
        this.showRangeAxis = showRangeAxis;
        this.lineWidth = lineWidth >= 0 ? lineWidth : 0;
	}
	
	@Override
	protected CategoryDataset createDataset() {
		return createCategoryDataset(data, true, labels);
	}
    
	@Override
	protected JFreeChart createChart(final CategoryDataset dataset) {
		final JFreeChart chart = ChartFactory.createLineChart(
				null, // chart title
				null, // domain axis label
				null, // range axis label
				dataset, // data
				PlotOrientation.VERTICAL,
				false, // include legend
				false, // tooltips
				false); // urls
		
        chart.setAntiAlias(true);
        chart.setBorderVisible(false);
        chart.setBorderPaint(TRANSPARENT_COLOR);
        chart.setBackgroundPaint(TRANSPARENT_COLOR);
        chart.setBackgroundImageAlpha(0.0f);
        
        final CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setOutlineVisible(false);
		plot.setOutlinePaint(TRANSPARENT_COLOR);
		plot.setDomainGridlinePaint(TRANSPARENT_COLOR);
		plot.setDomainGridlinesVisible(false);
	    plot.setRangeGridlinesVisible(showRangeAxis);
		plot.setBackgroundPaint(TRANSPARENT_COLOR);
		plot.setBackgroundAlpha(0.0f);
		plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
		
		final CategoryAxis domainAxis = (CategoryAxis) plot.getDomainAxis();
        domainAxis.setVisible(showCategoryAxis);
        domainAxis.setAxisLineVisible(showCategoryAxis);
        domainAxis.setTickMarksVisible(true);
        domainAxis.setTickLabelsVisible(true);
        domainAxis.setCategoryMargin(.1);
        
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setVisible(showRangeAxis);
        
		// Set axis range		
		if (range != null) {
			rangeAxis.setLowerBound(range.min * 1.1); // TODO add tick size???
			rangeAxis.setUpperBound(range.max * 1.1);
		}
		
		final LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
		renderer.setBaseItemLabelGenerator(showLabels ? new StandardCategoryItemLabelGenerator() : null);
		renderer.setBaseItemLabelsVisible(showLabels);
		renderer.setBaseItemLabelPaint(domainAxis.getLabelPaint());
		
		final List<?> keys = dataset.getRowKeys();
		System.out.println(lineWidth);
		if (colors != null && colors.size() >= keys.size()) {
			for (int i = 0; i < keys.size(); i++) {
				final Color c = colors.get(i);
				renderer.setSeriesFillPaint(i, c);
				renderer.setSeriesStroke(i, new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			}
		}
		
		return chart;
	}
}
